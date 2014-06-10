package cn.polatu.tools.database.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import cn.polatu.tools.database.module.TypeMapper;

/**
 * 工具栏
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class Util {

	public Util() {

	}

	/**
	 * 首字母大写
	 * 
	 * @param name
	 * @return
	 */
	public static String toUpperCaseFirstOne(String name) {
		char[] ch = name.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (i == 0) {
				ch[0] = Character.toUpperCase(ch[0]);
			} else {
				// ch[i] = Character.toLowerCase(ch[i]);
			}
		}
		StringBuffer a = new StringBuffer();
		a.append(ch);
		return a.toString();
	}

	public static void printResultReadOnly(ResultSet rs) throws SQLException {

		ResultSetMetaData rsm = rs.getMetaData();
		int count = rsm.getColumnCount();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			sb.append(rsm.getColumnName(i));
			sb.append("(");
			sb.append(rsm.getColumnTypeName(i));
			sb.append(")");
			sb.append("\t");
		}
		System.out.println(sb.toString());

		boolean b = rs.next();
		while (b) {
			sb = new StringBuilder();
			for (int i = 1; i < count; i++) {
				sb.append(rs.getString(i));
				sb.append("\t");
			}
			System.out.println(sb.toString());
			b = rs.next();
		}
	}

	public static void printResult(ResultSet rs) throws SQLException {

		ResultSetMetaData rsm = rs.getMetaData();
		int count = rsm.getColumnCount();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= count; i++) {
			sb.append(rsm.getColumnName(i));
			sb.append("(");
			sb.append(rsm.getColumnTypeName(i));
			sb.append(")");
			sb.append("\t");
		}
		System.out.println(sb.toString());

		boolean b = rs.first();
		while (b) {
			sb = new StringBuilder();
			for (int i = 1; i < count; i++) {
				sb.append(rs.getString(i));
				sb.append("\t");
			}
			System.out.println(sb.toString());
			b = rs.next();
		}
	}

	/**
	 * 读取JAR包中的资源
	 * 
	 * @param resurce
	 * @return
	 * @throws IOException
	 */
	public final static String readResource(String resurce) throws IOException {
		ClassLoader cl = Util.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(resurce);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count = in.read(buffer);
		while (count > 0) {
			out.write(buffer, 0, count);
			count = in.read(buffer);
		}

		in.close();
		String str = out.toString("UTF-8");
		return str;
	}

	/**
	 * 写字符串到文件
	 * 
	 * @param str
	 * @param outfile
	 * @throws IOException
	 */
	public final static void writeToFile(String str, String outfile)
			throws IOException {
		FileOutputStream out = new FileOutputStream(new File(outfile));
		out.write(str.getBytes(Charset.forName("UTF-8")));
		out.close();
	}
}
