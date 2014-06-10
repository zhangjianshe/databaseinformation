package cn.polatu.tools.database.gen.oracle;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.jsp.jstl.sql.Result;

import cn.mapway.tools.database.AccessBase;
import cn.mapway.tools.database.IConnectionPool;
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
public class OracleInformation implements IGenerator {

	public OracleInformation() {

	}

	@Override
	public void export(IConnectionPool pool, Context context)
			throws SQLException {
		Schema s = new Schema();
		oracle_fetch_schema(pool, s, context.getSchema());

		OracleGenerator gen = new OracleGenerator(context);
		gen.export(s);
	}

	AccessBase a;

	private void oracle_fetch_schema(IConnectionPool pool, Schema schema,
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

	/**
	 * <p>
	 * 
	 */
	public String getTableSQL(String schema) {

		return "select c.comments ,a.table_name ,a.column_name ,a.data_type,a.data_length , a.column_id ,a.nullable,b.comments from"
				+ " user_tables d,"
				+ "(select * from user_tab_columns order by table_name ,column_id)a,"
				+ "(select * from user_col_comments) b,"
				+ "(select * from user_tab_comments) c"
				+ " where d.table_name=a.table_name and a.table_name=b.table_name and a.column_name=b.column_name and a.table_name=c.table_name";
	}

	public String getPKSQL(String schema) {
		String sql = "select m.table_name,m.column_name from "
				+ "(SELECT * FROM USER_CONS_COLUMNS) m, "
				+ "(select * from user_constraints where CONSTRAINT_TYPE='P') n "
				+ "where m.constraint_name=n.constraint_name";

		return sql;
	}

	public String getViewSQL(String schema) {
		return "select c.comments ,a.table_name ,a.column_name ,a.data_type,a.data_length , a.column_id ,a.nullable,b.comments from "
				+ " user_views d,"
				+ " (select * from user_tab_columns order by table_name ,column_id) a,"
				+ " (select * from user_col_comments) b,"
				+ " (select * from user_tab_comments) c"
				+ " where d.view_name=a.table_name and a.table_name=b.table_name and a.column_name=b.column_name and a.table_name=c.table_name";
	}
}
