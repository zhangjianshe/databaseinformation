package cn.polatu.tools.database.gen;

public class Parameter {

	private String type;
	private String name;
	private String annotation;
	private String summary;

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Parameter() {

	}

	public Parameter(String t, String n, String a, String s) {
		setAnnotation(a);
		setName(n);
		setSummary(s);
		setType(t);
	}

}
