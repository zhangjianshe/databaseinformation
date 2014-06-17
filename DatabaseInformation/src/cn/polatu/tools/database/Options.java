package cn.polatu.tools.database;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 命令行参数
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
@Parameters(separators = "=")
public class Options {

	public Options() {

	}

	@Parameter
	public List<String> parameters = new ArrayList<String>();

	/**
	 * 数据库配置文件
	 */
	@Parameter(names = "--configure", description = "configureName")
	public String configureFile = "";

	/**
	 * 数据库配置文件
	 */
	@Parameter(names = { "--databaseType", "-d" }, description = "数据库类型 mssql mysql oracle postgres")
	public String databaseType = "";

	/**
	 * 数据库用户名
	 */
	@Parameter(names = { "--user", "-u" }, description = "数据库用户名")
	public String userName = "";

	/**
	 * 数据库密码
	 */
	@Parameter(names = { "--password", "-p" }, description = "数据库密码")
	public String password = "";

	/**
	 * JDBCURL
	 */
	@Parameter(names = { "--jdbcurl", "-j" }, description = "数据库连接")
	public String jdbcURL = "";

	/**
	 * 输出包名
	 */
	@Parameter(names = { "--package", "-P" }, description = "Package Name")
	public String pacakgeName = "";

	/**
	 * 输出数据库中的模式
	 */
	@Parameter(names = { "--schema", "-s" }, description = "database schema")
	public String schema = "";

	/**
	 * 输出目录
	 */
	@Parameter(names = { "--out", "-o" }, description = "target jar file's storage directory")
	public String outDirectory = "";

	/**
	 * 帮助信息
	 */
	@Parameter(names = { "--help", "-?", "-h" }, description = "显示帮助信息")
	public Boolean help = false;
	

	/**
	 * 数据类型对照表
	 */
	@Parameter(names = { "--dataTypeMapperFile", }, description = "数据类型对照表文件")
	public String  mapFile = "";
	
	/**
	 * 数据类型对照
	 */
	@Parameter(names = { "--mapper", }, description = "数据类型对照表格式[{dbtype,javatype,sqltype,importtypr},{dbtype,javatype,sqltype,importtypr}]")
	public String  mapper = "";

}
