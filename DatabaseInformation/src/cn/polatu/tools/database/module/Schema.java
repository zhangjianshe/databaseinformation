package cn.polatu.tools.database.module;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 数据库中的表分区
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class Schema {

	public Schema() {
		tables = new ArrayList<>();
	}

	/**
	 * 名称
	 */
	public String name;

	/**
	 * 注释
	 */
	public String comment;

	public ArrayList<Table> tables;

	/**
	 * 添加表
	 * 
	 * @param t
	 */
	public void addTable(Table t) {
		tables.add(t);
	}

	private String mSchema;

	public void init(DatabaseMetaData meta, String schema) {
		mSchema = schema;
		ResultSet rs;
		try {
			rs = meta.getTables(schema, "", null, new String[] { "TABLE",
					"VIEW" });

			boolean b = rs.first();
			while (b) {
				// handle table information
				String tname = rs.getString("table_name");
				String catalog = rs.getString("table_schem");
				Table t = new Table();
				t.setName(tname);
				t.setCatalog(catalog);
				this.addTable(t);
				processTableInformation(meta, t);
				b = rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理表信息
	 * 
	 * @param meta
	 * @param t
	 * @throws SQLException
	 */
	private void processTableInformation(DatabaseMetaData meta, Table t)
			throws SQLException {
		ResultSet rs = meta.getColumns(null, t.getCatalog(), t.getName(), null);
		ResultSet pk = meta.getPrimaryKeys(null, t.getCatalog(), t.getName());
		

		boolean b = rs.first();
		while (b) {
			Column c = new Column();
			c.setName(rs.getString("COLUMN_NAME"));
			c.setDbType(rs.getString("TYPE_NAME"));
			c.setPosition(rs.getInt("ORDINAL_POSITION"));
			c.setKey(isPK(pk,c.getName()));
			t.columns.add(c);
			b = rs.next();
		}

	}

	private boolean isPK(ResultSet pks, String cn) throws SQLException {
		boolean b = pks.first();
		while (b) {
			String col = pks.getString("COLUMN_NAME");
			if (col.equalsIgnoreCase(cn)) {
				return true;
			}
			b = pks.next();
		}
		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("==========" + mSchema + "==============\r\n");
		for (int i = 0; i < tables.size(); i++) {
			sb.append(tables.get(i));
			sb.append("\r\n");
		}

		return sb.toString();
	}
}
