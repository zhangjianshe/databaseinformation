package cn.polatu.tools.database.gen;

import java.util.ArrayList;

public class Comment {

	ArrayList<String> lines;
	private String title = "";
	private int indent = 0;

	public Comment(int indent) {
		lines = new ArrayList<String>();
		this.indent = indent;
	}

	public void addLine(String line) {
		addLine(line, true);
	}

	public void addLine(String line, boolean breakline) {
		if (breakline == true) {
			lines.add(line + "<br/>");
		} else {
			lines.add(line);
		}
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		String space = "";
		for (int i = 0; i < indent; i++) {
			space += "\t";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(space);
		sb.append("/**\r\n");
		if (this.title != null && this.title.length() > 0) {
			sb.append(space);
			sb.append(" * ");
			sb.append(title);
			sb.append("<br/>\r\n");
		}
		for (int i = 0; i < lines.size(); i++) {
			sb.append(space);
			sb.append(" * ");
			sb.append(lines.get(i));
			sb.append("\r\n");
		}
		sb.append(space);
		sb.append(" */\r\n");

		return sb.toString();
	}

}
