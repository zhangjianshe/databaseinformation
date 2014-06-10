package cn.polatu.tools.database.gen.postgres;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
public class PostgresInformation implements IGenerator {

	public PostgresInformation() {

	}

	@Override
	public void export(IConnectionPool pool, Context context)
			throws SQLException {
		Schema s = new Schema();
		postgres_fetch_schema(pool, s, context.getSchema());

		PostgresGenerator gen = new PostgresGenerator(context);
		gen.export(s);
	}

	private void postgres_fetch_schema(IConnectionPool pool, Schema schema,
			String schemaname) throws SQLException {

		schema.name = schemaname;
		StringBuilder sql_table = new StringBuilder();
		sql_table
				.append("SELECT c.oid, n.nspname AS schemaname, c.relname AS tablename,d.description, c.relkind,pg_get_userbyid(c.relowner) AS tableowner");
		sql_table.append(" FROM pg_class c");
		sql_table.append(" LEFT JOIN pg_namespace n ON n.oid = c.relnamespace");
		sql_table
				.append(" left join pg_description d on d.objoid =c.oid and d.objsubid=0");
		sql_table.append(" where (c.relkind='r' or c.relkind='v') ");
		sql_table.append(" and n.nspname='" + schemaname + "' ");

		AccessBase a = new AccessBase(pool);
		Result r = a.execute(sql_table.toString());

		Object[][] rows = r.getRowsByIndex();

		for (int i = 0; i < rows.length; i++) {
			Object[] row = rows[i];
			Table t = new Table();
			schema.addTable(t);
			t.setCatalog(schemaname);
			t.setName((String) row[2]);
			t.setComment((String) row[3]);
			t.setView("r".equals((String) row[4]) ? false : true);
			postgres_fetch_table(pool, t, (Long) row[0]);
		}

	}

	/**
	 * 获取表的列信息
	 * 
	 * @param pool
	 * 
	 * @param t
	 * @throws SQLException
	 */
	private void postgres_fetch_table(IConnectionPool pool, Table t,
			Long tableid) throws SQLException {
		StringBuilder sql_table = new StringBuilder();
		sql_table
				.append(" select cols.attname,cols.attnum,cols.type,cols.def, d.description from ( ");
		sql_table
				.append(" select attrelid,attname,attnum,format_type(atttypid,0) as type,atthasdef as def from pg_attribute a ");
		sql_table
				.append(" where a.attrelid="
						+ tableid
						+ "  and attnum >0 and a.attisdropped=false order by attnum asc ");
		sql_table.append(" ) cols");
		sql_table
				.append(" left join pg_description d on cols.attrelid=d.objoid and cols.attnum = d.objsubid");

		Set<Integer> pks = postgres_is_pk(pool, tableid);
		AccessBase a = new AccessBase(pool);
		Result r = a.execute(sql_table.toString());
		Object[][] rows = r.getRowsByIndex();
		for (int i = 0; i < rows.length; i++) {
			Object[] row = rows[i];
			String col_name = (String) row[0];
			Integer num = (Integer) row[1];
			String col_type = (String) row[2];
			boolean col_has_def = (Boolean) row[3];
			String col_comment = (String) row[4];
			Column c = new Column();
			c.setPosition(num);
			c.setComment(col_comment);
			c.setName(col_name);
			c.setDbType(col_type);
			if (pks.contains(num)) {
				c.setKey(true);
			} else {
				c.setKey(false);
			}
			c.isGenerated = col_has_def;
			t.getColumns().add(c);
		}
	}

	private Set<Integer> postgres_is_pk(IConnectionPool pool, Long tableid)
			throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT conkey FROM pg_constraint where contype='p' and conrelid="
				+ tableid);
		AccessBase a = new AccessBase(pool);
		Result r = a.execute(sql.toString());
		Object[][] rows = r.getRowsByIndex();
		Set<Integer> r1 = new HashSet<Integer>();
		if (rows.length > 0) {
			Object[] row = rows[0];
			String cols = row[0].toString();
			// {1,2}
			String t = cols.subSequence(1, cols.length() - 1).toString();
			String[] lines = t.split(",");

			for (int i = 0; i < lines.length; i++) {
				r1.add(Integer.parseInt(lines[i]));
			}
		}

		return r1;
	}
}
