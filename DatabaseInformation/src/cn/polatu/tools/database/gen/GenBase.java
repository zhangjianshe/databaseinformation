package cn.polatu.tools.database.gen;

import java.io.IOException;

import cn.polatu.tools.database.common.BObject;
import cn.polatu.tools.database.common.Util;
import cn.polatu.tools.database.module.Column;
import cn.polatu.tools.database.module.Table;

/**
 * 代码生成的基类
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class GenBase extends BObject {

	public Context mContext;

	public GenBase(Context context) {
		mContext = context;
	}

	/**
	 * 输出表的 Restful API基类
	 * 
	 * @param table
	 */
	public void genTableApi(Context context, Table table) {
		CompileUint unit = new CompileUint(context);
		unit.setRelativePackage("api");
		unit.setUnitName(table.getApiName());
		unit.setRecordChanged(false);
		unit.getComment().addLine(
				"表" + table.getComment() + table.getName() + "的API输出");

		try {
			unit.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param t
	 */
	protected void genObj(Table t) {
		log("export table obj " + t.getObjName());
		CompileUint unit = new CompileUint(mContext);
		unit.getComment().addLine("数据库表[" + t.getName() + "]" + t.getComment());

		unit.setUnitName(t.getObjName());
		unit.setRelativePackage("module");
		unit.addImplement("java.io.Serializable");
		unit.setRecordChanged(true);

		unit.getComment().addLine("<code>", false);
		unit.getComment().addLine("\t<table>", false);
		unit.getComment().addLine(
				"\t<tr><td>字段名称</td><td>数据类型</td><td>备注</td></tr>");
		Column c = new Column();
		c.access = Column.ACCESS_PRIVATE;
		c.isChaned = false;
		c.isFinal = true;
		c.isRead = false;
		c.isWrite = false;
		c.isStatic = true;
		c.setDbType("custom_long");
		c.defaultValue = t.getObjName().hashCode() + "L";
		c.setName("serialVersionUID", false);
		unit.getFields().add(c);

		for (int j = 0; j < t.getColumns().size(); j++) {
			c = t.getColumns().get(j);
			c.access = Column.ACCESS_PRIVATE;
			c.isChaned = true;
			unit.getFields().add(c);

			unit.getComment().addLine(
					String.format(
							"\t<tr><td>%s</td><td>%s</td><td>%s</td></tr>",
							c.getFieldName(), c.getDbType(), c.getComment()),
					false);
			Column c1 = new Column();
			c1.setComment(c.getName() + "索引");
			c1.setDbType("custom_int");
			c1.setName("INDEX_" + c.getName().toUpperCase());
			c1.access = "public";
			c1.defaultValue = "" + j;
			c1.isChaned = false;
			c1.isFinal = true;
			c1.isRead = false;
			c1.isWrite = false;
			c1.isStatic = true;
			unit.getFields().add(c1);
		}
		unit.getComment().addLine("\t</table>", false);
		unit.getComment().addLine("</code>", false);
		unit.getComment().addLine(CompileUint.AUTHOR);

		String obj = t.getName().toLowerCase();
		Method m = Method.createMethod("mergeTo");
		unit.getMethods().add(m);
		m.setFinal(false);
		m.setAccess(Column.ACCESS_PUBLIC);

		m.paras.add(new Parameter(t.getObjName(), obj, "", "拷贝目标对象"));
		m.comments.add("将本对象数据中的变化拷贝到目标对象中");
		m.addBody(1, "if(" + obj + "==null){");
		m.addBody(2, "return;");
		m.addBody(1, "}");

		for (int j = 0; j < t.getColumns().size(); j++) {
			c = t.getColumns().get(j);
			m.addBody(
					1,
					"if(isChanged(" + j + ")){" + obj + ".set"
							+ Util.toUpperCaseFirstOne(c.getFieldName())
							+ "(this." + c.getFieldName() + ");}");
		}

		try {
			unit.save();
		} catch (IOException e) {
			log(e.getMessage());
		}
	};
}
