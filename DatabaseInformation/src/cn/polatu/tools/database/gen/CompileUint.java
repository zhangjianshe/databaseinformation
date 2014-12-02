package cn.polatu.tools.database.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

import cn.polatu.tools.database.common.BObject;
import cn.polatu.tools.database.common.Util;
import cn.polatu.tools.database.module.Column;

/**
 * 编译单元
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class CompileUint extends BObject {

	public final static String AUTHOR = "@author <a href=\"mailto:zhangjianshe@gmail.com\">zhang jason</a><br/>";
	public final static String UNIT_TYPE_INTERFACE = "interface";
	public final static String UNIT_TYPE_CLASS = "class";
	public final static String ACCESS_PUBLIC = "public";
	public final static String ACCESS_PRIVATE = "private";
	public final static String ACCESS_PROTECTED = "protected";

	Context mContext;
	private String UnitType;
	private String UnitAccess;
	private String UnitName;
	private String relativePackage;
	private String extendsClass;
	private ArrayList<String> impClass;
	private ArrayList<Column> fields;
	private ArrayList<Method> methods;
	private HashSet<String> imports;
	private boolean recordChanged;
	private ArrayList<String> annos = new ArrayList<>();

	public boolean isRecordChanged() {
		return recordChanged;
	}

	public void setRecordChanged(boolean recordChanged) {
		this.recordChanged = recordChanged;
	}

	public void addAnnos(String anno) {
		annos.add(anno);
	}

	public String getRelativePackage() {
		return relativePackage;
	}

	public void setRelativePackage(String relativePackage) {
		this.relativePackage = relativePackage;
	}

	private Comment comment;

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public CompileUint(Context context) {
		mContext = context;
		UnitType = UNIT_TYPE_CLASS;
		UnitAccess = ACCESS_PUBLIC;
		comment = new Comment(0);
		fields = new ArrayList<Column>();
		methods = new ArrayList<Method>();
		imports = new HashSet<String>();
		extendsClass = "";
		impClass = new ArrayList<String>();
		setRecordChanged(false);
	}

	/**
	 * 获取函数集合
	 * 
	 * @return
	 */
	public ArrayList<Method> getMethods() {
		return methods;
	}

	/**
	 * 添加实现接口
	 * 
	 * @param imp
	 */
	public void addImplement(String imp) {
		impClass.add(imp);
	}

	/**
	 * 设置继承类
	 * 
	 * @param extend
	 */
	public void setExtend(String extend) {
		extendsClass = extend;
	}

	/**
	 * 字段属性
	 * 
	 * @return
	 */
	public ArrayList<Column> getFields() {
		return fields;
	}

	public void setUnitName(String name) {
		UnitName = name;
	}

	/**
	 * 设置单元属性
	 * 
	 * @param access
	 * @param unitType
	 */
	public void setUnitInformation(String access, String unitType) {
		UnitAccess = access;
		UnitType = unitType;
	}

	public int getTableFieldCount() {
		int count = 0;
		for (Column f : fields) {
			if (f.isWrite) {
				count++;
			}
		}
		return count;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mContext.unitComment.toString());
		sb.append("package " + getPackage() + ";\r\n");

		for (int i = 0; i < fields.size(); i++) {
			Column f = fields.get(i);
			processImport(f.getImport());
		}

		if (isRecordChanged()) {
			Column c = new Column();
			c.access = Column.ACCESS_PRIVATE;
			c.isChaned = false;
			c.isRead = false;
			c.isWrite = false;
			c.isStatic = false;
			c.isFinal = false;
			c.setName("changed", false);
			c.defaultValue = "new byte[" + getTableFieldCount() + "]";
			c.setDbType("custom_byte[]");
			getFields().add(c);

			Method m;
			m = Method.createMethod("resetChanged");
			m.setAccess(ACCESS_PUBLIC);
			m.addComment("重置字段是否被更改");
			m.paras.addParam("boolean", "b", "", "布尔型是否更改");
			m.addBody(1, "byte by = (b == true) ? (byte) 1 : (byte) 0;");
			m.addBody(1, "for (int i = 0; i < changed.length; i++) {");
			m.addBody(2, "changed[i] = by;");
			m.addBody(1, "}");
			methods.add(m);

			m = Method.createMethod("isChanged");
			m.setAccess(ACCESS_PUBLIC);
			m.returnValue.setType("boolean");
			m.addComment("字段是否被更改");
			m.paras.addParam("int", "index", "", "查询的字段索引");
			m.addBody(1, "return changed[index] == 0 ? false : true;");
			methods.add(m);

			m = Method.createMethod("setChanged");
			m.setAccess(ACCESS_PUBLIC);

			m.addComment("字段是否被更改");
			m.paras.addParam("int", "index", "", "字段索引");
			m.paras.addParam("boolean", "b", "", "数据是否更改");
			m.addBody(1, "changed[index] = (b == true) ? (byte) 1 : (byte) 0;");
			methods.add(m);

		}

		String[] is = new String[imports.size()];
		is = imports.toArray(is);

		for (int i = 0; i < is.length; i++) {
			sb.append("import " + is[i] + ";\r\n");
		}

		for (String line : annos) {
			sb.append(line);
			sb.append("\r\n");
		}
		sb.append(UnitAccess).append(" ").append(UnitType).append(" ");
		sb.append(UnitName);

		if (extendsClass.length() > 0) {
			sb.append(" extends ").append(extendsClass);
		}
		if (impClass.size() > 0) {
			sb.append(" implements ");
			for (int i = 0; i < impClass.size(); i++) {
				sb.append(impClass.get(i));
				if (i < impClass.size() - 1) {
					sb.append(",");
				}
			}
		}

		sb.append("\r\n");
		sb.append("{\r\n");
		for (int i = 0; i < fields.size(); i++) {
			Column f = fields.get(i);
			processField(sb, f);
		}

		for (int i = 0; i < methods.size(); i++) {
			Method m = methods.get(i);
			sb.append(m.toString());
		}

		sb.append("}\r\n");
		return sb.toString();
	}

	private void processField(StringBuilder sb, Column f) {

		for (String line : f.getAnnotation()) {
			sb.append(line);
			sb.append("\r\n");
		}
		sb.append("\t" + f.access + (f.isFinal ? " final " : " ")
				+ (f.isStatic ? " static " : " ") + f.getJavaType() + " "
				+ f.getFieldName()
				+ (f.defaultValue.length() > 0 ? ("=" + f.defaultValue) : "")
				+ ";\r\n");
		String imp = f.getImport();
		processImport(imp);

		Method m;
		if (f.isRead) {
			m = Method.createMethod("get"
					+ Util.toUpperCaseFirstOne(f.getFieldName()));
			m.addComment("获取字段" + f.getComment() == null ? f.getFieldName() : f
					.getComment() + "值");
			m.returnValue.setType(f.getJavaType());
			m.returnValue.setSummary("字段" + f.getFieldName() + " "
					+ f.getJavaType());

			m.addBody(1, "return this." + f.getFieldName() + ";");
			methods.add(m);
		}
		if (f.isWrite) {
			m = Method.createMethod("set"
					+ Util.toUpperCaseFirstOne(f.getFieldName()));
			m.addComment("设置字段" + f.getComment() == null ? f.getFieldName() : f
					.getComment() + "值");
			m.paras.addParam(f.getJavaType(), f.getFieldName().toLowerCase(),
					"", f.getComment());
			if (isRecordChanged()) {
				m.addBody(1, "setChanged(" + (f.getPosition() - 1) + ",true);");
			}
			m.addBody(1, "this." + f.getFieldName() + "="
					+ f.getFieldName().toLowerCase() + ";");
			methods.add(m);
		}

	}

	private void processImport(String imp) {
		if (imp != null) {
			imp = imp.trim();
			if (imp.length() > 0)
				imports.add(imp);
		}
	}

	/**
	 * 获取单元的package
	 * 
	 * @return
	 */
	public String getPackage() {
		String packagePath = mContext.getPackageName();

		if (relativePackage != null && relativePackage.length() > 0) {
			packagePath += "." + relativePackage;
		}

		return packagePath;
	}

	/**
	 * 保存编译单元
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		String packagePath = getPackage();

		packagePath = packagePath.replace('.', File.separatorChar);
		String path = mContext.getBasePath() + File.separator + packagePath;
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		String fileName = path + File.separator + UnitName + ".java";
		FileOutputStream out = new FileOutputStream(new File(fileName));
		log("source:" + fileName);
		String data = toString();

		out.write(data.getBytes("UTF-8"));
		out.close();
	}

	public void save(String body) throws UnsupportedEncodingException,
			IOException {
		String packagePath = getPackage();

		packagePath = packagePath.replace('.', File.separatorChar);
		String path = mContext.getBasePath() + File.separator + packagePath;
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		String fileName = path + File.separator + UnitName + ".java";
		FileOutputStream out = new FileOutputStream(new File(fileName));
		log("source:" + fileName);

		out.write(body.getBytes("UTF-8"));
		out.close();
	}

	public void addImport(String imp) {
		if (imp != null) {
			imp = imp.trim();
			if (imp.length() > 0)
				imports.add(imp);
		}
	}
}
