package cn.polatu.tools.database.module;

import java.io.BufferedReader;
import java.io.File;
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

	private void readFromFile() throws IOException {
		ClassLoader cl= TypeMapper.class.getClassLoader();
		InputStream in=cl.getResourceAsStream("cn/polatu/tools/database/module/datatypemapper.txt");	
		BufferedReader reader = new BufferedReader(new InputStreamReader(in) );
		String line = reader.readLine();
		while (line != null && line.length() > 0) {
			String[] ds = line.split("\t");
			String dbtype = ds.length > 0 ? ds[0].trim().toUpperCase() : "";
			String javatype = ds.length > 1 ? ds[1].trim() : "";
			String sqltype = ds.length > 2 ? ds[2].trim() : "";
			String importtype = ds.length > 3 ? ds[3].trim(): "";
			TypePair p = new TypePair(dbtype, javatype, sqltype, importtype);
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

	public static void main(String[] args)
	{
		TypeMapper m=TYPEMAPPER;
	}
}
