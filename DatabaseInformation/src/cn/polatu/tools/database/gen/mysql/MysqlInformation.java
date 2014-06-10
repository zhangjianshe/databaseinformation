package cn.polatu.tools.database.gen.mysql;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.jsp.jstl.sql.Result;

import cn.mapway.tools.database.AccessBase;
import cn.mapway.tools.database.IConnectionPool;
import cn.polatu.tools.database.common.BObject;
import cn.polatu.tools.database.gen.Context;
import cn.polatu.tools.database.gen.IGenerator;
import cn.polatu.tools.database.module.Column;
import cn.polatu.tools.database.module.Schema;
import cn.polatu.tools.database.module.Table;

/**
 * 获取Postgres 数据库元数据
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class MysqlInformation extends BObject implements IGenerator {

	public MysqlInformation() {

	}

	@Override
	public void export(IConnectionPool pool, Context context)
			throws SQLException {
		Schema s = new Schema();
		try {
			mysql_fetch_schema(pool, s, context.getSchema());
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		MySQLGenerator gen = new MySQLGenerator(context);
		gen.export(s);
	}

	AccessBase a;

	private void mysql_fetch_schema(IConnectionPool pool, Schema schema,
			String schemaname) throws SQLException {
		a = new AccessBase(pool);
		fetchPks(schemaname);

		schema.name = schemaname;
		Result rs;
		try {
			rs = a.execute(getTableSQL(schemaname));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		Object[][] b = rs.getRowsByIndex();
		String old = "";

		Table tab = null;
		ArrayList<Column> cols = null;
		int colindex = 0;
		for (int i = 0; i < rs.getRowCount(); i++) {
			Object[] c = b[i];
			String tn = (String) c[1];
			String tc = (String) c[0];
			String cn = (String) c[2];
			String ct = (String) c[3];
			String cc = (String) c[7];
			String extra = (String) c[8];
			if (!tn.equals(old)) {
				old = tn;
				tab = new Table();
				tab.setName(tn);
				tab.setComment(tc);
				cols = tab.getColumns();
				schema.tables.add(tab);
				colindex = 1;
			}

			Column col = new Column();
			col.setName(cn);
			col.setComment(cc);
			col.setDbType(ct);
			col.setPosition(colindex++);
			int l = 0;
			if (c[4] != null) {

				if (c[4].getClass().getName().indexOf("Long") >= 0) {
					Long big = (Long) c[4];
					l = big.intValue();
				}
			}

			// col.setLength(l);

			if (extra != null && extra.indexOf("auto_increment") >= 0) {
				col.isGenerated = true;
			} else {
				col.isGenerated = false;
			}
			col.setKey(isPK(tn, cn));
			cols.add(col);
		}

		try {
			rs = a.execute(getViewSQL(schemaname));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		b = rs.getRowsByIndex();
		old = "";
		Table view = null;
		cols = null;

		for (int i = 0; i < rs.getRowCount(); i++) {
			Object[] c = b[i];

			String tn = (String) c[1];
			String tc = (String) c[0];
			String cn = (String) c[2];
			String ct = (String) c[3];
			String cc = (String) c[7];

			if (!tn.equals(old)) {
				old = tn;

				view = new Table();
				view.setName(tn);
				view.setComment(tc);
				view.setView(true);

				cols = view.getColumns();
				schema.tables.add(view);
				colindex = 1;

			}

			Column col = new Column();
			col.setName(cn.toUpperCase());
			col.setDbType(ct);
			col.setComment(cc);
			col.setPosition(colindex++);
			int l = 0;
			if (c[4] != null) {

				if (c[4].getClass().getName().indexOf("Long") >= 0) {
					Long big = (Long) c[4];
					l = big.intValue();
				}
			}

			// col.setLength(l);
			cols.add(col);

		}

	}

	Object[][] pks = null;

	public boolean fetchPks(String schema) {
		Result r;
		try {
			r = a.execute(getPKSQL(schema));
			pks = r.getRowsByIndex();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 表 tn中的列cn 是否为主键
	 * 
	 * @param tn
	 * @param cn
	 * @return
	 */
	public boolean isPK(String tn, String cn) {
		boolean b = false;
		for (int i = 0; i < pks.length; i++) {
			String tna = (String) pks[i][0];
			String cna = (String) pks[i][1];
			String pktype = (String) pks[i][2];
			if (tna.equals(tn) && cna.equals(cn)
					&& (pktype.compareToIgnoreCase("PRIMARY") == 0)) {
				b = true;
				break;
			}
		}
		return b;
	}

	public String getTableSQL(String schema) {

		String sql = "select b.TABLE_COMMENT COMMENTS,b.TABLE_NAME TABLE_NAME,a.COLUMN_NAME COLUMN_NAME,a.DATA_TYPE DATATYPE,a.CHARACTER_MAXIMUM_LENGTH DATA_LENGTH,a.ORDINAL_POSITION COLUMN_ID,a.IS_NULLABLE NULLABLE,a.COLUMN_COMMENT,a.EXTRA from information_schema.columns a ,\r\n"
				+ "(select * from information_schema.tables where table_schema=\'"
				+ schema
				+ "\') b where\r\n"
				+ " a.TABLE_NAME=b.TABLE_NAME and a.table_schema='"
				+ schema
				+ "' order by TABLE_NAME,COLUMN_ID;";

		return sql;
	}

	public String getPKSQL(String schema) {
		String sql = "select  TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME from information_schema.KEY_COLUMN_USAGE where TABLE_SCHEMA=\'"
				+ schema + "\'";
		return sql;
	}

	public String getViewSQL(String schema) {

		String sql = "select  'View Comments' ,b.TABLE_NAME TABLE_NAME,a.COLUMN_NAME COLUMN_NAME,a.DATA_TYPE DATATYPE,a.CHARACTER_MAXIMUM_LENGTH DATA_LENGTH,a.ORDINAL_POSITION COLUMN_ID,a.IS_NULLABLE NULLABLE,a.COLUMN_COMMENT,a.EXTRA from information_schema.columns a ,\r\n"
				+ "(select * from information_schema.views where table_schema=\'"
				+ schema
				+ "\') b where\r\n"
				+ "a.table_schema=\'"
				+ schema
				+ "\' and a.TABLE_NAME=b.TABLE_NAME order by TABLE_NAME;";

		return sql;

	}

}
