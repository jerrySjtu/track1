package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;

public class RecLogDAO {
	
private static Connection conn;
	
	static{
		getConnection();
	}
	

	public static LinkedList<Record> getTrainSet(long minTime, long maxTime, int startIndex, int endIndex){
		LinkedList<Record> list = new LinkedList<Record>();
		try {
			PreparedStatement statement = conn.prepareStatement("select * from rec_log limit ?,?");
			statement.setInt(1, startIndex);
			statement.setInt(2, endIndex);
			ResultSet results = statement.executeQuery();
			while(results.next()){
				int userID = results.getInt(1);
				int itemID = results.getInt(2);
				short result = results.getShort(3);
				long timestamp = Long.parseLong(results.getString(4));
				if(timestamp >= minTime && timestamp <= maxTime){
					Record record = new Record(userID, itemID, result, timestamp);
					list.add(record);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
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
