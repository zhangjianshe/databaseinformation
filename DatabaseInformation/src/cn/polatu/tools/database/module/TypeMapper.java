package cn.polatu.tools.database.module;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 数据类型对照表
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public class TypeMapper {

	public final static TypeMapper TYPEMAPPER = new TypeMapper();
	public ArrayList<TypePair> maps;

	private TypeMapper() {
		maps = new ArrayList<TypePair>();
		try {
			readFromFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 追加格式对照表
	 * [{dbtype,javatype,sqltype,importtypr,gwttype},{dbtype,javatype,sqltype,
	 * importtypr,gwttype}]
	 * 
	 * @param mapper
	 */
	public void append(String mapper) {
		String temp = mapper.trim();
		if (temp.startsWith("[") && temp.endsWith("]")) {
			temp = temp.substring(1, temp.length() - 1);
			ArrayList<String> lines = new ArrayList<>();
			String line = "";
			boolean start = false;
			for (int i = 0; i < temp.length(); i++) {
				if (temp.charAt(i) == '{') {
					if (line.length() > 0) {
						lines.add(line);
						line = "";
					}
					start = true;
					continue;
				}
				if (start) {
					if (temp.charAt(i) == '}') {
						start = false;
						lines.add(line);
						line = "";
						continue;
					}
					line += temp.charAt(i);
				}
			}

			for (String l : lines) {
				String[] ds = l.split(",");
				String dbtype = ds.length > 0 ? ds[0].trim().toUpperCase() : "";
				String javatype = ds.length > 1 ? ds[1].trim() : "";
				String sqltype = ds.length > 2 ? ds[2].trim() : "";
				String gwttype = ds.length > 3 ? ds[3].trim() : javatype;
				String importtype = ds.length > 4 ? ds[4].trim() : "";

				TypePair p = new TypePair(dbtype, javatype, sqltype,
						importtype, gwttype);
				maps.add(p);
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < maps.size(); i++) {
			TypePair p = maps.get(i);
			sb.append(p.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * 根据文件追加对照表
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void appendFile(String fileName) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = reader.readLine();
		while (line != null && line.length() > 0) {
			String[] ds = line.split("\t");
			String dbtype = ds.length > 0 ? ds[0].trim().toUpperCase() : "";
			String javatype = ds.length > 1 ? ds[1].trim() : "";
			String sqltype = ds.length > 2 ? ds[2].trim() : "";
			String gwttype = ds.length > 3 ? ds[3].trim() : javatype;
			String importtype = ds.length > 4 ? ds[4].trim() : "";
			TypePair p = new TypePair(dbtype, javatype, sqltype, importtype,
					gwttype);
			maps.add(p);
			line = reader.readLine();
		}
		reader.close();
	}

	private void readFromFile() throws IOException {
		ClassLoader cl = TypeMapper.class.getClassLoader();
		InputStream in = cl
				.getResourceAsStream("cn/polatu/tools/database/resource/datatypemapper.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		while (line != null && line.length() > 0) {
			String[] ds = line.split("\t");
			String dbtype = ds.length > 0 ? ds[0].trim().toUpperCase() : "";
			String javatype = ds.length > 1 ? ds[1].trim() : "";
			String sqltype = ds.length > 2 ? ds[2].trim() : "";
			String gwttype = ds.length > 3 ? ds[3].trim() : javatype;
			String importtype = ds.length > 4 ? ds[4].trim() : "";

			TypePair p = new TypePair(dbtype, javatype, sqltype, importtype,
					gwttype);
			maps.add(p);
			line = reader.readLine();
		}
		reader.close();
	}

	public TypePair findByDbType(String dbtype) {
		for (int i = 0; i < maps.size(); i++) {
			TypePair p = maps.get(i);
			if (dbtype.toUpperCase().startsWith(p.dbType)) {
				return p;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		TypeMapper m = TYPEMAPPER;
		String data = "[{dbtype,javatype,sqltype,importtypr},{dbtype,javatype,sqltype,importtypr}]";
		m.append(data);
		System.out.println(m);
	}
}
