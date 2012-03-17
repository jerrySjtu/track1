package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RecLogDAO {
	
private static Connection conn;
	
	static{
		getConnection();
	}
	
	public static void insertRecLog(int userID,int itemID, int result, String time) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into rec_log(user_id, item_id, result, rec_time) values(?,?,?,?);");
			preparedStatement.setInt(1, userID);
			preparedStatement.setInt(2, itemID);
			preparedStatement.setInt(3, result);
			preparedStatement.setString(4, time);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * connect to the database
	 */
	public static Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager
						.getConnection("jdbc:mysql://localhost/track1?"
								+ "user=root&password=19882006");
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}// end if
		return conn;
	}

	/*
	 * close the connection
	 */
	public static void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
