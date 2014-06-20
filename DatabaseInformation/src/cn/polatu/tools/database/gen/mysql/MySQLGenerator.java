package cn.polatu.tools.database.gen.mysql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import cn.polatu.tools.database.common.BObject;
import cn.polatu.tools.database.common.Util;
import cn.polatu.tools.database.gen.CompileUint;
import cn.polatu.tools.database.gen.Context;
import cn.polatu.tools.database.gen.GenBase;
import cn.polatu.tools.database.gen.Method;
import cn.polatu.tools.database.gen.Parameter;
import cn.polatu.tools.database.module.Column;
import cn.polatu.tools.database.module.Schema;
import cn.polatu.tools.database.module.Table;

/**
 * POSTGRES generator
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
class MySQLGenerator extends GenBase {

	Random mRandom;

	public MySQLGenerator(Context context) {
		super(context);
		mRandom = new Random(Calendar.getInstance().getTimeInMillis());
	}

	public long randomLong() {
		return mRandom.nextLong();
	}

	public void export(Schema schema) {
		for (int i = 0; i < schema.tables.size(); i++) {
			log("export " + schema.tables.get(i).getName());
			Table t = schema.tables.get(i);
			genObj(t);
			genObjs(t);
			genDao(t);
		}

		genXmlFiles(schema);
	}

	/**
	 * 生成配置文件 GWT模块 DWR转换器
	 * 
	 * @param schema
	 */
	private void genXmlFiles(Schema schema) {
		// generator database properties
		try {
			String build = Util
					.readResource("cn/polatu/tools/database/resource/gwt.txt");
			saveData(build, "", schema.name + "Data.gwt.xml");

		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE dwr PUBLIC \"-//GetAhead Limited//DTD Direct Web Remoting 3.0//EN\" \"http://directwebremoting.org/schema/dwr30.dtd\">\r\n");
		sb.append("<dwr>\r\n");
		sb.append("\t<allow>\r\n");
		for (Table t : schema.tables) {
			String obj = getPackage("module") + "." + t.getObjName();
			sb.append("\t\t<convert converter=\"bean\" match=\"" + obj
					+ "\"/>\r\n");
			obj = getPackage("module") + "." + t.getObjsName();
			sb.append("\t\t<convert converter=\"bean\" match=\"" + obj
					+ "\"/>\r\n");
		}
		sb.append("\t</allow>\r\n");
		sb.append("</dwr>\r\n");

		try {
			saveBasePathData(sb.toString(), "dwr_objs.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存文本到制定的目录
	 * 
	 * @param data
	 * @param subpackage
	 * @param fileName
	 * @throws IOException
	 */
	public void saveData(String data, String subpackage, String fileName)
			throws IOException {
		String packagePath = getPackage(subpackage);
		packagePath = packagePath.replace('.', File.separatorChar);
		String path = mContext.getBasePath() + File.separator + packagePath;
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		FileOutputStream out = new FileOutputStream(new File(path
				+ File.separator + fileName));
		out.write(data.getBytes("UTF-8"));
		out.close();
	}

	/**
	 * 保存文本到根目录 src
	 * 
	 * @param data
	 * @param subpackage
	 * @param fileName
	 * @throws IOException
	 */
	public void saveBasePathData(String data, String fileName)
			throws IOException {
		String path = mContext.getBasePath();
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		FileOutputStream out = new FileOutputStream(new File(path
				+ File.separator + fileName));
		out.write(data.getBytes("UTF-8"));
		out.close();
	}

	/**
	 * 获取单元的package
	 * 
	 * @return
	 */
	public String getPackage(String relativePackage) {
		String packagePath = mContext.getPackageName();

		if (relativePackage != null && relativePackage.length() > 0) {
			packagePath += "." + relativePackage;
		}

		return packagePath;
	}

	/**
	 * @param t
	 */
	private void genObjs(Table t) {
		CompileUint unit = new CompileUint(mContext);
		unit.getComment().addLine(
				"数据库表[" + t.getName() + "]集合" + t.getComment());

		unit.setUnitName(t.getObjsName());
		unit.setRelativePackage("module");
		unit.addImport("java.util.ArrayList");
		unit.addImplement("java.io.Serializable");
		unit.setRecordChanged(false);
		unit.setExtend("ArrayList<" + t.getObjName() + ">");

		Column c = new Column();
		c.access = Column.ACCESS_PRIVATE;
		c.isChaned = false;
		c.isFinal = true;
		c.isRead = false;
		c.isWrite = false;
		c.isStatic = true;
		c.setDbType("custom_long");
		c.defaultValue = t.getObjsName().hashCode() + "L";
		c.setName("serialVersionUID", false);
		unit.getFields().add(c);

		unit.getComment().addLine(CompileUint.AUTHOR);

		try {
			unit.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	/**
	 * @param t
	 */
	private void genDao(Table t) {
		CompileUint unit = new CompileUint(mContext);
		unit.getComment().addLine(
				"数据库表[" + t.getName() + "]访问代码" + t.getComment());

		unit.setUnitName(t.getName().toUpperCase() + "DAOImpl");
		unit.setRelativePackage("dao");
		unit.addImport("java.sql.Connection");
		unit.addImport("java.sql.ResultSet");
		unit.addImport("java.sql.SQLException");
		unit.addImport("java.sql.Statement");
		unit.addImport("java.sql.PreparedStatement");

		unit.addImport("cn.mapway.tools.database.ExecuteResult");
		unit.addImport("cn.mapway.tools.database.IConnectionPool");
		unit.addImport("cn.mapway.tools.database.AccessBase");
		unit.addImport(mContext.getPackageName() + ".module." + t.getObjName());
		unit.addImport(mContext.getPackageName() + ".module." + t.getObjsName());
		unit.setRecordChanged(false);
		unit.setExtend("AccessBase");

		// 构造函数

		Method m;
		m = Method.createMethod(t.getName().toUpperCase() + "DAOImpl");
		m.returnValue.setType("");
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("IConnectionPool", "pool", "", "数据库连接池");
		m.addBody(1, "super(pool);");
		m.addBody(1, "resetSearched(true);");
		unit.getMethods().add(m);
		m.comments.add("构造函数");

		m = Method.createMethod(t.getName().toUpperCase() + "DAOImpl");
		m.returnValue.setType("");
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("Connection", "connection", "", "数据库连接");
		m.addBody(1, "super(connection);");
		m.addBody(1, "resetSearched(true);");
		unit.getMethods().add(m);
		m.comments.add("构造函数,用于事务处理，传递进一个事务连接Connection");

		// 重置查询字段
		m = Method.createMethod("resetSearched");
		m.comments.add("重置查询字段");
		unit.getMethods().add(m);

		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("boolean", "b", "", "是否查询字段");
		m.addBody(1, "for(int i=0;i<searched.length;i++){");
		m.addBody(2, "searched[i]=b;");
		m.addBody(1, "}");

		// 设置某个字段是否查询
		m = Method.createMethod("setSearched");
		m.comments.add("设置某个字段是否查询");
		unit.getMethods().add(m);

		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("int", "index", "", "是否查询字段索引");
		m.paras.addParam("boolean", "b", "", "是否查询字段");
		m.addBody(1, "searched[index]=b;");

		m = Method.createMethod("searchField");
		m.comments.add("构造搜索字段");
		unit.getMethods().add(m);
		m.returnValue.setType("String");
		m.setAccess(CompileUint.ACCESS_PRIVATE);

		m.addBody(1, "StringBuilder sb=new StringBuilder();");
		for (int i = 0; i < t.getColumns().size(); i++) {
			Column c = t.getColumns().get(i);
			m.addBody(1, "if( true == searched[" + (c.getPosition() - 1)
					+ "]){sb.append(\"`" + c.getName() + "`,\");}");
		}
		m.addBody(1, "sb.deleteCharAt(sb.length()-1);");
		m.addBody(1, "return sb.toString();");

		// 搜索ALL
		m = Method.createMethod("selectAll");
		m.comments.add("根据Where条件模板查询记录 可以提高查询效率,按照主键排序");
		unit.getMethods().add(m);
		m.returnValue.setType(t.getObjsName());
		m.setAccess(CompileUint.ACCESS_PUBLIC);

		m.addThrow("SQLException");

		StringBuilder where = new StringBuilder();
		for (Column c : t.getColumns()) {
			if (c.isKey()) {
				if (where.length() == 0) {
					where.append("order by ");
				} else {
					where.append(",");
				}
				where.append("`" + c.getName() + "`");
			}
		}
		m.addBody(
				1,
				t.getObjsName() + " os=selectByWhereTemplate(\""
						+ where.toString() + "\");");

		m.addBody(1, "return os;");

		ArrayList<Column> pks = t.getPrimaryKeys();

		// 根据主键查询

		if (pks.size() > 0) {
			Method method = Method.createMethod("selectByPk");
			method.comments.add("根据主键查询记录");
			method.addThrow("SQLException");
			String p1 = "";
			String p2 = "";
			for (Column c : pks) {
				method.paras.addParam(c.getJavaType(), c.getName()
						.toLowerCase(), "", c.getComment());
				if (p1.length() > 0) {
					p1 += " and `" + c.getName() + "`=?";
					p2 += "," + c.getName().toLowerCase();
				} else {
					p1 += "'" + c.getName() + "'" + "=?";
					p2 += c.getName().toLowerCase();
				}
			}
			String wherepk = "\"where " + p1 + "\"," + p2;
			method.returnValue.setType(t.getObjName());
			method.addBody(1, t.getObjsName() + " os=selectByWhereTemplate("
					+ wherepk + ");");
			method.addBody(1,
					"if(os.size()>0) return os.get(0);else return null;");
			unit.getMethods().add(method);

		}

		// 根据主键删除记录
		if (t.isView() == false) {
			if (pks.size() > 0) {
				Method method = Method.createMethod("deleteByPk");
				method.comments.add("根据主键删除记录");
				method.addThrow("SQLException");
				String p1 = "";
				String p2 = "";
				for (Column c : pks) {
					method.paras.addParam(c.getJavaType(), c.getName()
							.toLowerCase(), "", c.getComment());
					if (p1.length() > 0) {
						p1 += " and `" + c.getName() + "`=?";
						p2 += "," + c.getName().toLowerCase();
					} else {
						p1 += "`" + c.getName() + "`" + "=?";
						p2 += c.getName().toLowerCase();
					}
				}
				String wherepk = "\"where " + p1 + "\"," + p2;
				method.addBody(1, "deleteByWhereTemplate(" + wherepk + ");");

				unit.getMethods().add(method);
			}
		}
		// 搜索
		m = Method.createMethod("selectByWhereTemplate");
		m.comments.add("根据Where条件模板查询记录 可以提高查询效率");
		unit.getMethods().add(m);
		m.returnValue.setType(t.getObjsName());
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("String", "whereTemplate", "",
				" 查询模板 <code>whereTemplate=\"where a=? or b=?\";</code>");
		m.paras.addParam("Object...", "values", "", "根据查询模板中？号填充不定长参数");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"SELECT \");");
		m.addBody(1, "sql.append(searchField());");
		m.addBody(1, "sql.append(\" from `" + t.getName() + "` \");");
		m.addBody(1, "sql.append(whereTemplate);");
		m.addBody(1, t.getObjsName() + " os=new " + t.getObjsName() + "();");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2, "String s=sql.toString();");
		m.addBody(2, "statement = con.prepareStatement(s);");
		m.addBody(2, "int count=findCount(whereTemplate,'?');");
		m.addBody(2, "for(int i=0;i<count;i++){");
		m.addBody(3, "statement.setObject(i+1, values[i]);");
		m.addBody(2, "}");
		m.addBody(2, "ResultSet rs=statement.executeQuery();");
		m.addBody(2, "while(rs.next()){");
		m.addBody(3, t.getObjName() + " o=this.mapRecord2Obj(rs);");
		m.addBody(3, "os.add(o);");
		m.addBody(2, "}");
		m.addBody(2, "rs.close();");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");
		m.addBody(1, "return os;");

		// 获取分页数据
		m = Method.createMethod("getPageData");
		m.comments.add("根据Where条件模板查询记录 可以提高查询效率");
		unit.getMethods().add(m);
		m.returnValue.setType(t.getObjsName());
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("String", "whereTemplate", "",
				" 查询模板 <code>whereTemplate=\"where a=? or b=?\";</code>");
		m.paras.addParam("int", "start", "", "查询的开始记录");
		m.paras.addParam("int", "end", "", "查询结束记录");
		m.paras.addParam("Object...", "values", "", "根据查询模板中？号填充不定长参数");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"SELECT \");");
		m.addBody(1, "sql.append(searchField());");
		m.addBody(1, "sql.append(\" from `" + t.getName() + "` \");");
		m.addBody(1, "sql.append(whereTemplate);");
		m.addBody(1,
				"sql.append(\" limit \"+(end-start)+\" offset \"+start+\"\"); ");

		m.addBody(1, t.getObjsName() + " os=new " + t.getObjsName() + "();");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2, "String s=sql.toString();");
		m.addBody(2, "statement = con.prepareStatement(s);");
		m.addBody(2, "int count=findCount(whereTemplate,'?');");
		m.addBody(2, "for(int i=0;i<count;i++){");
		m.addBody(3, "statement.setObject(i+1, values[i]);");
		m.addBody(2, "}");
		m.addBody(2, "ResultSet rs=statement.executeQuery();");
		m.addBody(2, "while(rs.next()){");
		m.addBody(3, t.getObjName() + " o=this.mapRecord2Obj(rs);");
		m.addBody(3, "os.add(o);");
		m.addBody(2, "}");
		m.addBody(2, "rs.close();");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");
		m.addBody(1, "return os;");

		// 搜索
		m = Method.createMethod("getCountByWhereTemplate");
		m.comments.add("根据Where条件模板查询记录数量 可以提高查询效率");
		unit.getMethods().add(m);
		m.returnValue.setType("long");
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("String", "whereTemplate", "",
				" 查询模板 <code>whereTemplate=\"where a=? or b=?\";</code>");
		m.paras.addParam("Object...", "values", "", "根据查询模板中？号填充不定长参数");
		m.addThrow("SQLException");
		m.addBody(1, "long count=0;");
		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"SELECT count(*)\");");
		m.addBody(1, "sql.append(\" from `" + t.getName() + "` \");");
		m.addBody(1, "sql.append(whereTemplate);");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2, "String s=sql.toString();");
		m.addBody(2, "statement = con.prepareStatement(s);");
		m.addBody(2, "int xcount=findCount(whereTemplate,'?');");
		m.addBody(2, "for(int i=0;i<xcount;i++){");
		m.addBody(3, "statement.setObject(i+1, values[i]);");
		m.addBody(2, "}");
		m.addBody(2, "ResultSet rs=statement.executeQuery();");
		m.addBody(2, "if(rs.next()){");
		m.addBody(3, "count=rs.getLong(1);");
		m.addBody(2, "}");
		m.addBody(2, "rs.close();");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");
		m.addBody(1, "return count;");

		// 搜索
		m = Method.createMethod("deleteByWhereTemplate");
		m.comments.add("根据Where条件模板删除记录");
		unit.getMethods().add(m);
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("String", "whereTemplate", "",
				" 查询模板 <code>whereTemplate=\"where a=? or b=?\";</code>");
		m.paras.addParam("Object...", "values", "", "根据查询模板中？号填充不定长参数");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"DELETE \");");
		m.addBody(1, "sql.append(\" from `" + t.getName() + "` \");");
		m.addBody(1, "sql.append(whereTemplate);");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2, "String s=sql.toString();");
		m.addBody(2, "statement = con.prepareStatement(s);");
		m.addBody(2, "int count=findCount(whereTemplate,'?');");
		m.addBody(2, "for(int i=0;i<count;i++){");
		m.addBody(3, "statement.setObject(i+1, values[i]);");
		m.addBody(2, "}");
		m.addBody(2, "statement.execute();");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");

		// 搜索
		m = Method.createMethod("selectByWhereTemplate2ExecuteResult");
		m.comments.add("根据Where条件模板查询记录 可以提高查询效率 返回的结果");
		m.comments.add("ExecuteResult  对于这个返回值切记要进行返回值检查，并释放资源");
		m.comments.add("<code>");
		m.comments.add("if(rs!=null){.... rs.dispose();");
		m.comments.add("</code>");
		unit.getMethods().add(m);
		m.returnValue.setType("ExecuteResult");
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("String", "whereTemplate", "",
				" 查询模板 <code>whereTemplate=\"where a=? or b=?\";</code>");
		m.paras.addParam("Object...", "values", "", "根据查询模板中？号填充不定长参数");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"SELECT \");");
		m.addBody(1, "sql.append(searchField());");
		m.addBody(1, "sql.append(\" from `" + t.getName() + "` \");");
		m.addBody(1, "sql.append(whereTemplate);");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2, "String s=sql.toString();");
		m.addBody(2, "statement = con.prepareStatement(s);");
		m.addBody(2, "int count=findCount(whereTemplate,'?');");
		m.addBody(2, "for(int i=0;i<count;i++){");
		m.addBody(3, "statement.setObject(i+1, values[i]);");
		m.addBody(2, "}");
		m.addBody(2, "ResultSet rs=statement.executeQuery();");

		m.addBody(2,
				"return new ExecuteResult(this.ConnectionPool,con,rs,statement);");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");
		m.addBody(1, "return null;");

		// 记录映射
		m = Method.createMethod("mapRecord2Obj");
		m.comments.add("将ResultSet中的一条记录转变为对象");
		unit.getMethods().add(m);

		m.returnValue.setType(t.getObjName());
		m.setAccess(CompileUint.ACCESS_PUBLIC);
		m.paras.addParam("ResultSet", "rs", "", " 查询结果集");
		m.addThrow("SQLException");
		m.addBody(1, "int i=1;");
		m.addBody(1, t.getObjName() + " o=new " + t.getObjName() + "();");
		m.addBody(1, "if(true==mapByIndex)");
		m.addBody(1, "{");
		for (int i = 0; i < t.getColumns().size(); i++) {
			Column c = t.getColumns().get(i);
			m.addBody(2, "if(true==searched[" + (c.getPosition() - 1) + "]){");
			String line = "rs.get" + c.getSQLType() + "(i)";
			m.addBody(3, line + ";");
			m.addBody(3, "o.set" + Util.toUpperCaseFirstOne(c.getFieldName())
					+ "(true==rs.wasNull()?null:" + line + ");");
			m.addBody(3, "i++;");
			m.addBody(2, "}");
		}
		m.addBody(1, "}else{");
		for (int i = 0; i < t.getColumns().size(); i++) {
			Column c = t.getColumns().get(i);
			m.addBody(2, "if(true==searched[" + (c.getPosition() - 1) + "]){");
			String line = "rs.get" + c.getSQLType() + "(\"" + c.getName()
					+ "\")";
			m.addBody(3, line + ";");
			m.addBody(3, "o.set" + Util.toUpperCaseFirstOne(c.getFieldName())
					+ "(true==rs.wasNull()?null:" + line + ");");
			m.addBody(2, "}");
		}
		m.addBody(1, "};");
		m.addBody(1, "o.resetChanged(false);");
		m.addBody(1, "return o;");

		for (int i = 0; i < t.getColumns().size(); i++) {
			Column c = t.getColumns().get(i);
			unit.addImport(c.getImport());
			Method method = Method.createMethod("findBy"
					+ c.getName().toUpperCase());
			method.paras.addParam(c.getJavaType(), c.getName().toLowerCase(),
					"", c.getComment());
			method.returnValue.setType(t.getObjName());
			method.addThrow("SQLException");
			method.addBody(1, t.getObjsName()
					+ " objs= selectByWhereTemplate(\"where " + c.getName()
					+ "=?\"," + c.getName().toLowerCase() + ");");
			method.addBody(1,
					"if(objs.size()>0)return objs.get(0);else return null;");
			method.comments.add("根据字段" + c.getName() + "(" + c.getComment()
					+ ")查找记录");
			unit.getMethods().add(method);
		}
		// 字段是否查询
		Column c = new Column();
		c.access = Column.ACCESS_PRIVATE;
		c.isChaned = false;
		c.isFinal = true;
		c.isRead = false;
		c.isWrite = false;
		c.isStatic = false;
		c.setDbType("custom_boolean[]");
		c.defaultValue = "new boolean[" + t.getColumns().size() + "];";
		c.setName("searched", false);
		unit.getFields().add(c);

		// 是否按照字段映射查询内容
		c = new Column();
		c.access = Column.ACCESS_PRIVATE;
		c.isChaned = false;
		c.isFinal = false;
		c.isRead = true;
		c.isWrite = true;
		c.isStatic = false;
		c.setDbType("custom_boolean");
		c.defaultValue = "true";
		c.setName("mapByIndex", false);
		unit.getFields().add(c);

		// 字段索引
		for (int i = 0; i < t.getColumns().size(); i++) {
			c = new Column();
			c.access = Column.ACCESS_PUBLIC;
			c.isChaned = false;
			c.isFinal = true;
			c.isRead = false;
			c.isWrite = false;
			c.isStatic = true;
			c.setDbType("custom_int");
			c.defaultValue = "" + (t.getColumns().get(i).getPosition() - 1);
			c.setName("INDEX_" + t.getColumns().get(i).getFieldName(), false);
			c.setComment("字段" + t.getColumns().get(i).getComment() + "索引");
			unit.getFields().add(c);

			c = new Column();
			c.access = Column.ACCESS_PUBLIC;
			c.isChaned = false;
			c.isFinal = true;
			c.isRead = false;
			c.isWrite = false;
			c.isStatic = true;
			c.setDbType("custom_string");
			c.defaultValue = "\"`" + (t.getColumns().get(i).getFieldName())
					+ "`\"";
			c.setName("FIELD_" + t.getColumns().get(i).getFieldName(), false);
			c.setComment("字段" + t.getColumns().get(i).getComment() + "名称");
			unit.getFields().add(c);

		}

		// //////////////////////////////////TABEL 插入数据库记录
		m = Method.createMethod("insertObject");
		m.comments.add("向数据库中插入一个对象");
		unit.getMethods().add(m);

		m.paras.addParam(t.getObjName(), "obj", "", "要插入的对象");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"INSERT INTO `" + t.getName() + "` (\");");
		m.addBody(1, "StringBuilder s1=new StringBuilder();");
		m.addBody(1, "StringBuilder s2=new StringBuilder();");
		m.addBody(1, "int index=1;");

		for (Column c1 : t.getColumns()) {
			m.addBody(1, "if(obj.isChanged(" + (c1.getPosition() - 1) + ")){");
			m.addBody(2, "s1.append(\"`" + c1.getName() + "`,\");");
			m.addBody(2, "s2.append(\"?,\");");
			m.addBody(1, "}");
		}
		m.addBody(1, "s1.deleteCharAt(s1.length()-1);");
		m.addBody(1, "s2.deleteCharAt(s2.length()-1);");
		m.addBody(1, "s2.append(\")\");");
		m.addBody(1, "sql.append(s1.toString());");
		m.addBody(1, "sql.append(\" ) VALUES ( \");");
		m.addBody(1, "sql.append(s2.toString());");
		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(
				2,
				"statement = con.prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);");
		for (Column c1 : t.getColumns()) {
			m.addBody(2, "if(obj.isChanged(" + (c1.getPosition() - 1) + ")){");
			m.addBody(
					3,
					"statement.setObject(index,obj.get"
							+ Util.toUpperCaseFirstOne(c1.getFieldName())
							+ "());index++;");
			m.addBody(2, "}");
		}
		m.addBody(2, "statement.execute();");

		m.addBody(2, "ResultSet rs=statement.getGeneratedKeys();");

		m.addBody(2, "if(rs.next()){");
		for (Column c1 : t.getColumns()) {
			if (c1.isGenerated) {
				m.addBody(3, "int index1=1;");
				m.addBody(3, "if(!obj.isChanged(" + (c1.getPosition() - 1)
						+ ")){");
				m.addBody(4,
						"obj.set" + Util.toUpperCaseFirstOne(c1.getFieldName())
								+ "(rs.get" + c1.getSQLType() + "(index1++));");
				m.addBody(3, "}");
			}
		}
		m.addBody(2, "}");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(2, "obj.resetChanged(false);");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");

		// //////////////////////////////////TABEL 根据主键更新记录
		m = Method.createMethod("updateObject");
		m.comments.add("根据主键更新记录");
		unit.getMethods().add(m);

		m.paras.addParam(t.getObjName(), "obj", "", "要更新的对象");
		m.addThrow("SQLException");

		m.addBody(1, "StringBuilder sql=new StringBuilder();");
		m.addBody(1, "sql.append(\"UPDATE `" + t.getName() + "` SET \");");
		m.addBody(1, "StringBuilder s1=new StringBuilder();");
		m.addBody(1, "int index=1;");
		StringBuilder sbWhere = new StringBuilder();
		sbWhere.append(" where ");
		for (Column c1 : t.getColumns()) {
			if (!c1.isKey()) {// 修改非主键字段
				m.addBody(1, "if(obj.isChanged(" + (c1.getPosition() - 1)
						+ ")){");
				m.addBody(2, "s1.append(\"`" + c1.getName() + "`=?,\");");
				m.addBody(1, "}");
			} else {
				sbWhere.append("`" + c1.getName() + "`=?,");
			}
		}
		if (sbWhere.length() > 0) {
			sbWhere.setLength(sbWhere.length() - 1);
		}

		m.addBody(1, "s1.deleteCharAt(s1.length()-1);");
		m.addBody(1, "sql.append(s1.toString());");
		m.addBody(1, "sql.append(\"" + sbWhere.toString() + "\");");

		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2,
				"statement = con.prepareStatement(sql.toString(),Statement.NO_GENERATED_KEYS);");

		for (Column c1 : t.getColumns()) {
			if (!c1.isKey()) {// 修改非主键字段
				m.addBody(2, "if(obj.isChanged(" + (c1.getPosition() - 1)
						+ ")){");
				m.addBody(
						3,
						"statement.setObject(index++,obj.get"
								+ Util.toUpperCaseFirstOne(c1.getFieldName())
								+ "());");
				m.addBody(2, "}");
			}
		}

		for (Column c1 : t.getColumns()) {
			if (c1.isKey()) {// 主键字段条件
				m.addBody(
						2,
						"statement.setObject(index++,obj.get"
								+ Util.toUpperCaseFirstOne(c1.getFieldName())
								+ "());");
			}
		}

		m.addBody(3, "statement.execute();");
		m.addBody(3, "obj.resetChanged(false);");
		m.addBody(2, "if(statement!=null) statement.close();");
		m.addBody(1, "}");
		m.addBody(1, "catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");

		// //////////////////////////////////TABEL 根据条件更新数据
		m = Method.createMethod("update");
		m.comments.add("根据模板SQL语句更新数据库的记录");
		m.comments
				.add(" <code>sqlTemplate=\"FIELDA = ? , FIELDB = ? where FIELDC = ? \";</code>");
		unit.getMethods().add(m);

		m.paras.addParam("String", "sqlTemplate", "", "搜索的Where条件语句");
		m.paras.addParam("Object...", "values", "", "不定长参数");
		m.addThrow("SQLException");

		m.addBody(1, "Connection con=getConnection();");
		m.addBody(1, "if(con==null)");
		m.addBody(1, "{");
		m.addBody(2, "con=this.ConnectionPool.getConnection();");
		m.addBody(1, "}");
		m.addBody(1, "PreparedStatement statement=null;");
		m.addBody(1, "try{");
		m.addBody(2,
				"statement = con.prepareStatement(\"UPDATE \\\"" + t.getName()
						+ "\\\" SET \"" + "+ sqlTemplate);");
		m.addBody(2, "int count = findCount(sqlTemplate, '?');");
		m.addBody(2, "for (int i = 0; i<count; i++){");
		m.addBody(3, "Object o = values[i];");
		m.addBody(3, "statement.setObject(i + 1, o);");
		m.addBody(2, "}");
		m.addBody(2, "if (statement != null) statement.close();");
		m.addBody(1, "}catch (SQLException e) {");
		m.addBody(2, "e.printStackTrace();");
		m.addBody(2, "throw e;");
		m.addBody(1, "}");
		m.addBody(1, "finally{");
		m.addBody(2, "if(getConnection()==null){");
		m.addBody(3, "this.ConnectionPool.releaseConnection(con);");
		m.addBody(2, "}");
		m.addBody(1, "}");

		unit.getComment().addLine(CompileUint.AUTHOR);
		try {
			unit.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
