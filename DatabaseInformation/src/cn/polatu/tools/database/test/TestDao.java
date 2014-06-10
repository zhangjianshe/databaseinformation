package cn.polatu.tools.database.test;

import java.sql.SQLException;
import java.text.ParseException;

import cn.mapway.tools.database.ConnectionPool;
import cn.mapway.tools.database.IConnectionPool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestDao {

	public TestDao() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ParseException, SQLException {
		TestDao app = new TestDao();

	}

	public IConnectionPool getConnectionPool() {
		IConnectionPool pool = null;
		try {
			pool = ConnectionPool.create("postgres");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return pool;
	}

	public static void print(Object o) {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		String str = g.toJson(o);
		System.out.println(str);
	}

}
