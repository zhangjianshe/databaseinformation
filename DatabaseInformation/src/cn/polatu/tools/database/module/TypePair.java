package cn.polatu.tools.database.module;

/**
 * 类型对照
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class TypePair {

	public TypePair(String dbtype, String javatype, String sqltype,
			String importtype, String gwtType) {
		dbType = dbtype;
		javaType = javatype;
		sqlType = sqltype;
		importType = importtype;
		this.gwtType = gwtType;
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
	 * Gwt javascriptObject类型
	 */
	public String gwtType;
	/**
	 * IMport 类型
	 */
	public String importType;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(dbType).append(",");
		sb.append(javaType).append(",");
		sb.append(sqlType).append(",");
		sb.append(gwtType).append(",");
		sb.append(importType);
		return sb.toString();
	}
}
