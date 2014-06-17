package cn.polatu.tools.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import cn.mapway.tools.database.ConnectionPool;
import cn.polatu.tools.database.common.BObject;
import cn.polatu.tools.database.common.Util;
import cn.polatu.tools.database.common.ZipUtility;
import cn.polatu.tools.database.gen.Context;
import cn.polatu.tools.database.gen.IGenerator;
import cn.polatu.tools.database.gen.mysql.MysqlInformation;
import cn.polatu.tools.database.gen.postgres.PostgresInformation;
import cn.polatu.tools.database.module.TypeMapper;

import com.beust.jcommander.JCommander;

/**
 * 代码生成主程序
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class DatabaseApi extends BObject {

	/**
	 * 数据库连接属性
	 */
	Properties mDatabaseProperties;

	public static void main(String[] args) {

		Options options = new Options();
		JCommander j = new JCommander(options, args);
		j.setProgramName("databaseapi");

		if (options.help) {
			j.usage();
			System.exit(0);
		}

		if (options.mapFile.length() > 0) {
			try {
				TypeMapper.TYPEMAPPER.appendFile(options.mapFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (options.mapper.length() > 0) {
			try {
				TypeMapper.TYPEMAPPER.append(options.mapper);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 从配置文件中读取
		DatabaseApi app = new DatabaseApi();
		if (options.configureFile.length() > 0) {

			Context context = genContext(options);
			if (context != null) {
				try {
					app.export(options.configureFile, context);
				} catch (Exception e) {
					System.out.println("ERROR:" + e.getMessage());
				}
			}
			System.exit(0);
		}

		if (options.databaseType.length() == 0) {
			app.log("请输入数据库类型>--databaseType=[mssql,mysql,oracle,postgres]");
			System.exit(0);
		}

		if (options.jdbcURL.length() == 0) {
			app.log("请输入JDBCURL");
			System.exit(0);
		}

		if (options.userName.length() == 0) {
			app.log("输入用户名--user");
			System.exit(0);
		}

		Context context = genContext(options);
		if (context == null) {
			System.exit(0);
		}

		try {
			app.mDatabaseProperties = new Properties();
			app.mDatabaseProperties.setProperty("database_type",
					options.databaseType);
			app.mDatabaseProperties.setProperty("jdbc_url", options.jdbcURL);
			app.mDatabaseProperties.setProperty("user", options.userName);
			app.mDatabaseProperties.setProperty("password", options.password);
			app.mDatabaseProperties.setProperty("max_connections", "20");

			app.export(context, options.databaseType, options.jdbcURL,
					options.userName, options.password, 20);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param config
	 * @return
	 */
	public static Context genContext(Options config) {
		Context context = new Context();
		String basePath = "out" + File.separator + config.schema
				+ File.separator + "src";
		File f = new File(basePath);
		if (f.exists()) {
			f.delete();
		}
		context.setBasePath(basePath);
		if (config.pacakgeName == null || config.pacakgeName.length() == 0) {
			System.out.println("没有 --package 参数");
			return null;
		} else {
			context.setPackageName(config.pacakgeName);
		}
		if (config.schema == null || config.schema.length() == 0) {
			System.out.println("没有 --schema 参数");
			return null;
		} else {
			context.setSchema(config.schema);
		}

		if (config.outDirectory.length() > 0) {
			String o = config.outDirectory;
			if (!o.endsWith(File.separator)) {
				o = o + File.separator;
			}
			context.setOutput(o);
		}
		return context;
	}

	/**
	 * 
	 * @param configureFile
	 * @param context
	 * @throws Exception
	 */
	private void export(String configureFile, Context context) throws Exception {
		mDatabaseProperties = new Properties();
		mDatabaseProperties.load(new FileReader(new File(configureFile)));
		String dt = mDatabaseProperties.getProperty("database_type");
		String url = mDatabaseProperties.getProperty("jdbc_url");
		String username = mDatabaseProperties.getProperty("user");
		String passwd = mDatabaseProperties.getProperty("password");
		String max_cons = mDatabaseProperties.getProperty("max_connections");
		int maxConnections = 20;
		if (max_cons != null && max_cons.length() > 0) {
			maxConnections = Integer.valueOf(max_cons);
		}

		export(context, dt, url, username, passwd, maxConnections);
	}

	/**
	 * @param context
	 * @param dt
	 * @param url
	 * @param username
	 * @param passwd
	 * @param maxConnections
	 * @throws Exception
	 * @throws SQLException
	 */
	private void export(Context context, String dt, String url,
			String username, String passwd, int maxConnections) {
		log("export " + dt + "," + username + "," + passwd);
		ConnectionPool pool = null;
		try {
			pool = new ConnectionPool(dt, url, username, passwd, maxConnections);
		} catch (Exception e) {
			log(e.getMessage());
			return;
		}
		try {
			if (dt.equalsIgnoreCase("postgres")) {
				IGenerator gen = new PostgresInformation();

				gen.export(pool, context);

			} else if (dt.equalsIgnoreCase("mysql")) {
				IGenerator gen = new MysqlInformation();
				gen.export(pool, context);
			} else {
				System.out.println("sorry ,we canot provider a implement for "
						+ dt);
			}
		} catch (SQLException e) {
			log(e.getMessage());
		}

		compile(context);
	}

	/**
	 * 编译并打包
	 * 
	 * @param context
	 */
	private void compile(Context context) {

		String ANT_HOME = System.getenv("ANT_HOME");
		if (ANT_HOME == null || ANT_HOME.equals("")) {
			System.out.println("请设置系统变量 ANT_HOME");
			return;
		}

		String basePath = "out" + File.separator + context.getSchema()
				+ File.separator + "build.xml";
		String databaseproperties = "out" + File.separator
				+ context.getSchema() + File.separator + "database.properties";

		String dir = "out" + File.separator + context.getSchema();
		try {
			String path = ZipUtility.getJarPath(DatabaseApi.class);
			basePath = path + File.separator + basePath;
			dir = path + File.separator + dir;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String command = ANT_HOME + File.separator + "bin" + File.separator;

		if (File.separator.equals("/")) {
			command += "ant";
		} else {
			command += "ant.bat";
		}
		// generator ant build xml
		try {
			String build = Util
					.readResource("cn/polatu/tools/database/resource/build.txt");
			String str = build.replace("{{SCHEMA}}", context.getSchema());
			str = str.replace("{{BASEPATH}}", context.getOutput());

			Util.writeToFile(str, basePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// generator database properties

		try {
			mDatabaseProperties.store(new FileOutputStream(new File(
					databaseproperties)),
					"database connection configure information");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Runtime run = Runtime.getRuntime();

		try {
			run.exec(new String[] { command }, null, new File(dir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
