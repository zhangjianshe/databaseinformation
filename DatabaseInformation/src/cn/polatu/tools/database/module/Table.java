package cn.polatu.tools.database.module;

import java.util.ArrayList;

/**
 * 数据库的表单元
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class Table {

	ArrayList<Column> columns;

	private String name;
	private String comment;
	private String catalog;
	private boolean isView;

	public boolean isView() {
		return isView;
	}

	public void setView(boolean isView) {
		this.isView = isView;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Table() {
		columns = new ArrayList<Column>();
		isView = false;
	}

	/**
	 * 类对象名称
	 * 
	 * @return
	 */
	public String getObjName() {
		return this.getName().toUpperCase() + "Obj";
	}

	/**
	 * 表的API输出类名
	 * 
	 * @return
	 */
	public String getApiName() {
		return this.getName().toUpperCase() + "Api";
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public ArrayList<Column> getPrimaryKeys() {
		ArrayList<Column> keys = new ArrayList<Column>();
		for (Column c : columns) {
			if (c.isKey()) {
				keys.add(c);
			}
		}
		return keys;
	}

	/**
	 * 类对象集合名称
	 * 
	 * @return
	 */
	public String getObjsName() {
		return getObjName() + "s";
	}

	public String getNuzName() {
		String name = toFirstUpper(getName());
		return name;
	}

	public String getTableName() {
		return getName().toLowerCase();
	}

	public String toFirstUpper(String str) {
		StringBuilder sb = new StringBuilder();
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		String s = sb.toString();
		return s;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		String comment = this.getComment() == null ? "" : ("("
				+ this.getComment() + ")");
		sb.append("TABLE " + catalog + "." + getName() + comment + " 信息\r\n");

		for (int i = 0; i < columns.size(); i++) {
			sb.append(columns.get(i));
		}

		return sb.toString();
	}
}
