package cn.polatu.tools.database.gen;

import java.util.ArrayList;

public class Parameters extends ArrayList<Parameter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Parameters() {
	}

	public void addParam(Parameter p) {
		add(p);
	}

	/**
	 * 
	 * @param t
	 * @param n
	 * @param a
	 * @param s
	 */
	public void addParam(String t, String n, String a, String s) {
		Parameter p = new Parameter();
		p.setAnnotation(a);
		p.setName(n);
		p.setSummary(s);
		p.setType(t);
		addParam(p);
	}
}
