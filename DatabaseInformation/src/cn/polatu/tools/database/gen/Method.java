package cn.polatu.tools.database.gen;

import java.util.ArrayList;

public class Method {

	public ArrayList<String> comments;
	ArrayList<String> lines;
	private String name;
	private String access;
	private boolean isFinal;
	private boolean isStatic;
	public Parameters paras;
	public Parameter returnValue;
	ArrayList<String> throwsType;

	public Method() {
		isFinal = false;
		isStatic = false;
		lines = new ArrayList<String>();
		paras = new Parameters();
		comments = new ArrayList<String>();
		returnValue = new Parameter("void", "", "", "");
		access = "public";
		throwsType = new ArrayList<String>();
	}

	private String space(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	/**
	 * 添加throw
	 * 
	 * @param throwtype
	 */

	public void addThrow(String throwtype) {
		this.throwsType.add(throwtype);
	}

	public void addBody(int indent, String line) {
		lines.add(space(indent) + line);
	}

	public void addComment(String title) {
		comments.add(title);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Comment c = processComments();
		sb.append(c.toString());
		sb.append("\t");
		sb.append(access + " ");
		if (isFinal) {
			sb.append("final ");
		}
		if (isStatic) {
			sb.append("static ");
		}
		sb.append(returnValue.getType() + " ");
		sb.append(name);
		sb.append("(");
		for (int i = 0; i < paras.size(); i++) {
			Parameter p = paras.get(i);
			sb.append(p.getAnnotation() + " " + p.getType() + " " + p.getName());
			if (i < paras.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		if (throwsType.size() > 0) {
			sb.append(" throws ");
			for (int i = 0; i < throwsType.size(); i++) {
				sb.append(throwsType.get(i));
				if (i < throwsType.size() - 1) {
					sb.append(",");
				}
			}
		}

		sb.append("\r\n");
		sb.append("\t{\r\n");
		for (int i = 0; i < lines.size(); i++) {
			sb.append(space(1));
			sb.append(lines.get(i));
			sb.append("\r\n");
		}
		sb.append("\t}\r\n");

		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * 处理方法的注释文档
	 * 
	 * @return
	 */
	private Comment processComments() {
		Comment c = new Comment(1);
		for (int i = 0; i < comments.size(); i++) {
			c.addLine(comments.get(i));
		}

		for (int i = 0; i < paras.size(); i++) {
			Parameter p = paras.get(i);
			c.addLine(
					"@param " + p.getName() + " " + p.getType() + " "
							+ p.getSummary(), false);
		}

		c.addLine(
				"@return " + returnValue.getType() + " "
						+ returnValue.getSummary(), false);

		if (throwsType.size() > 0) {
			for (int i = 0; i < throwsType.size(); i++) {
				c.addLine("@throws " + throwsType.get(i));
			}
		}
		return c;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public final static Method createMethod(String name) {
		Method m = new Method();
		m.setName(name);
		return m;
	}
}
