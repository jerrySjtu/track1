package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ItemDAO {
	private static Connection conn;

	static {
		getConnection();
	}

	/**
	 * @param itemID
	 * @param minTime
	 * @param maxTime
	 * @return the users who have accepted the item during the time
	 */
	public static LinkedList<Integer> getUserByAcceptedItem(int itemID,
			long minTime, long maxTime) {
		LinkedList<Integer> list = new LinkedList<Integer>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select unique user_id from rec_log where item_id=? " +
							"and rec_time>=? and rec_time<=? and result=1;");
			preparedStatement.setInt(1, itemID);
			preparedStatement.setLong(2, minTime);
			preparedStatement.setLong(3, maxTime);
			ResultSet results = preparedStatement.executeQuery();
			while(results.next())
				list.add(results.getInt(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static Item getItemByID(int id) {
		Item item = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select * from item where id=?;");
			preparedStatement.setInt(1, id);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next())
				item = new Item(result.getInt(1), result.getString(2),
						result.getString(3));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return item;
	}

	public static LinkedList<Item> readAllItems() {
		LinkedList<Item> list = new LinkedList<Item>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select * from item;");
			ResultSet results = preparedStatement.executeQuery();
			while (results.next()) {
				Item item = new Item(results.getInt(1), results.getString(2),
						results.getString(3));
				list.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void insertItemSimKey(int itemID, int dID, double similariy) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into item_sim_key(id, d_id, sim) values(?,?,?)");
			preparedStatement.setInt(1, itemID);
			preparedStatement.setInt(2, dID);
			preparedStatement.setDouble(3, similariy);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertItem(int id, String category, String keyword) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into item(id, category, keyword) values(?,?,?);");
			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, category);
			preparedStatement.setString(3, keyword);
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
