package cn.polatu.tools.database.module;

import java.util.ArrayList;
import java.util.List;

/**
 * 表中的列
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class Column {

	public final static String ACCESS_PUBLIC = "public";
	public final static String ACCESS_PRIVATE = "private";
	public final static String ACCESS_PROTECTED = "protected";

	public Column() {
		isKey = false;
		isStatic = false;
		isFinal = false;
		access = ACCESS_PUBLIC;
		isRead = true;
		isWrite = true;
		isChaned = false;
		defaultValue = "";
	}

	public Column copy() {
		Column c = new Column();
		c.isKey = this.isKey;
		c.isStatic = this.isStatic;
		c.isFinal = this.isFinal;
		c.access = this.access;
		c.isRead = this.isRead;
		c.isWrite = this.isWrite;
		c.isChaned = this.isChaned;
		c.defaultValue = this.defaultValue;
		c.bChaneUpcase = this.bChaneUpcase;
		c.name = this.name;
		c.dbType = this.dbType;
		c.comment = this.comment;
		c.position = this.position;
		c.isGenerated = this.isGenerated;
		c.annos = new ArrayList<>();
		for (String s : this.annos) {
			c.annos.add(new String(s));
		}
		return c;
	}

	ArrayList<String> annos = new ArrayList<>();

	private boolean bChaneUpcase;
	private String name;
	private String dbType;
	private boolean isKey;
	private String comment;
	private int position;

	public boolean isStatic;
	public boolean isFinal;
	public String access;
	public boolean isRead;
	public boolean isWrite;
	public boolean isChaned;
	public String defaultValue;
	public boolean isGenerated;

	public void addAnnotation(String line) {
		annos.add(line);
	}

	public List<String> getAnnotation() {
		return annos;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		setName(name, true);
	}

	public void setName(String name, boolean b) {
		this.name = name;
		bChaneUpcase = b;
	}

	public String getFieldName() {
		if (bChaneUpcase == true)
			return this.name.toUpperCase();
		else
			return this.name;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	/**
	 * 获取字段的Java类型
	 * 
	 * @return
	 */
	public String getJavaType() {
		TypePair p = TypeMapper.TYPEMAPPER.findByDbType(dbType);
		if (p != null) {
			return p.javaType;
		} else {
			System.out.println(" could not find dbtype " + dbType);
			return "";
		}
	}

	/**
	 * 获取字段的Java类型
	 * 
	 * @return
	 */
	public String getGwtType() {
		TypePair p = TypeMapper.TYPEMAPPER.findByDbType(dbType);
		if (p != null) {
			return p.gwtType;
		} else {
			System.out.println(" could not find dbtype " + dbType);
			return "";
		}
	}

	/**
	 * 获取字段的SQL类型
	 * 
	 * @return
	 */
	public String getSQLType() {
		TypePair p = TypeMapper.TYPEMAPPER.findByDbType(dbType);
		if (p != null) {
			return p.sqlType;
		} else {
			System.out.println(" could not find dbtype " + dbType);
			return "";
		}
	}

	/**
	 * 获取字段的SQL类型
	 * 
	 * @return
	 */
	public String getImport() {
		TypePair p = TypeMapper.TYPEMAPPER.findByDbType(dbType);
		if (p != null) {
			return p.importType;
		} else {
			System.out.println(" could not find dbtype " + dbType);
			return "";
		}
	}

	public boolean isKey() {
		return isKey;
	}

	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}

	public String getComment() {
		return comment == null ? "" : comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String toString() {

		return String.format("%s %s [%s] %s %s\r\n", getComment(), getName(),
				getDbType(), getJavaType(), getSQLType());

	}
}
