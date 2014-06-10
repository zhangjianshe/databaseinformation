package cn.polatu.tools.database.gen;

import java.sql.SQLException;

import cn.mapway.tools.database.IConnectionPool;

/**
 * API 生成接口
 * 
 * @author zhangjianshe@gmail.com
 * 
 */
public interface IGenerator {
	/**
	 * 输出接口
	 * @param pool 数据库连接池
	 * @param context 生成代码环境
	 * @throws SQLException
	 */
	public void export(IConnectionPool pool, Context context)
			throws SQLException;
}
