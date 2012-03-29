package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class RecLogDAO {

	private static Connection conn;

	static {
		getConnection();
	}
	
	public static void main(String[]  args){
		System.out.println(getLogSize());
	}
	
	public static Set<Integer> getTrainUser(long minTime, long maxTime) {
		Set<Integer> userset = new HashSet<Integer>();
		try {
			PreparedStatement statement = conn
					.prepareStatement("select distinct user_id from rec_log where rec_time>? and rec_time<=?");
			statement.setString(1, String.valueOf(minTime));
			statement.setString(2, String.valueOf(maxTime));
			ResultSet resultset = statement.executeQuery();
			while(resultset.next())
				userset.add(resultset.getInt(1));
			resultset.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userset;
	}

	public static LinkedList<Record> getTrainSetByUser(int userID, long minTime, long maxTime) {
		LinkedList<Record> list = new LinkedList<Record>();
		try {
			PreparedStatement statement = conn
					.prepareStatement("select item_id,result,rec_time from " +
							"rec_log where user_id=? and rec_time>? and rec_time<=?");
			statement.setInt(1, userID);
			statement.setString(2, String.valueOf(minTime));
			statement.setString(3, String.valueOf(maxTime));
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				int itemID = results.getInt(1);
				short result = results.getShort(2);
				long timestamp = Long.parseLong(results.getString(3));
				Record record = new Record(userID, itemID, result, timestamp);
				list.add(record);
			}
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static int getLogSize(){
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement("select count(*) from rec_log;");
			ResultSet resultset = statement.executeQuery();
			if(resultset.next())
				return resultset.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static LinkedList<Record> getLogWithLimit(int downLimit, int upLimit){
		LinkedList<Record> logList = new LinkedList<Record>();
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement("" +
					"select user_id,item_id,result,rec_time from rec_log limit ?,?;");
			statement.setInt(1, downLimit);
			statement.setInt(2, upLimit);
			ResultSet resultset = statement.executeQuery();
			while(resultset.next()){
				int userID = resultset.getInt(1);
				int itemID = resultset.getInt(2);
				short result = resultset.getShort(3);
				long timestamp = resultset.getLong(4);
				Record record = new Record(userID, itemID, result, timestamp);
				logList.add(record);
			}
			resultset.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return logList;
	}
	
	public static void insertLogBuffer(Record record){
		try {
			PreparedStatement statement = conn.prepareStatement("" +
					"insert into rec_log_buffer(user_id,item_id,result,rec_time) values(?,?,?,?)");
			statement.setInt(1, record.getUserID());
			statement.setInt(2, record.getItemID());
			statement.setShort(3, record.getResult());
			statement.setString(4, String.valueOf(record.getTimestamp()));
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertRecLog(int userID, int itemID, int result,
			String time) {
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
