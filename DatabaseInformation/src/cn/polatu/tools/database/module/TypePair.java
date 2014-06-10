package cn.polatu.tools.database.module;

/**
 * 类型对照
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class TypePair {

	public TypePair(String dbtype, String javatype, String sqltype,String importtype) {
		dbType = dbtype;
		javaType = javatype;
		sqlType = sqltype;
		importType=importtype;
	}

	/**
	 * 数据库设计类型
	 */
	public String dbType;
	/**
	 * java类型
	 */
	public String javaType;
	/**
	 * jdbc数据库类型
	 */
	public String sqlType;
	
	/**
	 * IMport 类型
	 */
	public String importType;
}
