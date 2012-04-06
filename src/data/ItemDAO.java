package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ItemDAO {
	private static Connection conn;

	static {
		getConnection();
	}
	
	public static LinkedList<Item> getItemsInLog(){
		LinkedList<Item> list = new LinkedList<Item>();
		try {
			PreparedStatement statement = conn.prepareStatement(
					"select distinct item_id from rec_log");
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				Item item = getItemByID(resultSet.getInt(1));
				list.add(item);
			}
			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static Set<Integer> getItemsetNotInLog(){
		Set<Integer> set = new HashSet<Integer>();
		try {
			PreparedStatement statement = conn.prepareStatement("select id " +
					"from item where id not in (select distinct item_id from rec_log)");
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next())
				set.add(resultSet.getInt(1));
			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}
	
	/**
	 * 
	 * @param itemID
	 * @return find a similar item in recommendation log for which not 
	 * in recommendation log
	 */
	public static int getSimilarItemByCategory(int itemID){
		try {
			PreparedStatement statement = conn.prepareStatement(
					"select d_id from item_sim_category");
			statement.setInt(1, itemID);
			ResultSet result = statement.executeQuery();
			if(result.next())
				return result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static LinkedList<PostNode> getSimilarItemByCF(int itemID) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement statement = conn
					.prepareStatement("select d_id, sim from item_sim_cf where id=? order by sim desc limit 1,50");
			statement.setInt(1, itemID);
			ResultSet results = statement.executeQuery();
			while(results.next()){
				PostNode node = new PostNode(results.getInt(1), results.getDouble(2));
				list.add(node);
			}
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	

	public static LinkedList<PostNode> getSimilarItemByKey(int itemID) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement statement = conn
					.prepareStatement("select d_id, sim from item_sim_key where id=? order by sim desc limit 1,50");
			statement.setInt(1, itemID);
			ResultSet results = statement.executeQuery();
			while(results.next()){
				PostNode node = new PostNode(results.getInt(1), results.getDouble(2));
				list.add(node);
			}
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static double getKeySim(int itemID1, int itemID2) {
		PreparedStatement statement;
		double similarity = 0;
		try {
			statement = conn
					.prepareStatement("select sim from item_sim_key where id=? and d_id=?;");

			statement.setInt(1, itemID1);
			statement.setInt(2, itemID2);
			ResultSet result = statement.executeQuery();
			if (result.next())
				similarity = result.getDouble(1);
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return similarity;
	}

	public static double getCFSim(int itemID1, int itemID2) {
		PreparedStatement statement;
		double similarity = 0;
		try {
			statement = conn
					.prepareStatement("select sim from item_sim_cf where id=? and d_id=?;");
			statement.setInt(1, itemID1);
			statement.setInt(2, itemID2);
			ResultSet result = statement.executeQuery();
			if (result.next())
				similarity = result.getDouble(1);
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return similarity;
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
					.prepareStatement("select DISTINCT user_id from rec_log where item_id=? "
							+ "and rec_time>=? and rec_time<=? and result=1;");
			preparedStatement.setInt(1, itemID);
			preparedStatement.setLong(2, minTime);
			preparedStatement.setLong(3, maxTime);
			ResultSet results = preparedStatement.executeQuery();
			while (results.next())
				list.add(results.getInt(1));
			results.close();
			preparedStatement.close();
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
			result.close();
			preparedStatement.close();
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
				Item item = new Item(results.getInt(1), results.getString(2),results.getString(3));
				list.add(item);
			}
			results.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// get similar items with CF similarity
	public static LinkedList<PostNode> getItemWithCFSim(int itemID) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select d_id, sim from item_sim_cf where id=?");
			preparedStatement.setInt(1, itemID);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				PostNode node = new PostNode(result.getInt(1),
						result.getDouble(2));
				list.add(node);
			}
			result.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// get similar items with key similarity
	public static LinkedList<PostNode> getItemWithKeySim(int itemID) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select d_id, sim from item_sim_key where id=?");
			preparedStatement.setInt(1, itemID);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				PostNode node = new PostNode(result.getInt(1),
						result.getDouble(2));
				list.add(node);
			}
			result.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	public static void insetItemCategorySim(int id, int dID){
		try {
			PreparedStatement statement = conn.prepareStatement("" +
					"insert into item_sim_category(id,d_id) values(?,?)");
			statement.setInt(1, id);
			statement.setInt(2, dID);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertItemCFSim(int itemID, int dID, double similariy) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into item_sim_cf(id, d_id, sim) values(?,?,?)");
			preparedStatement.setInt(1, itemID);
			preparedStatement.setInt(2, dID);
			preparedStatement.setDouble(3, similariy);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertItemKeySim(int itemID, int dID, double similariy) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into item_sim_key(id, d_id, sim) values(?,?,?)");
			preparedStatement.setInt(1, itemID);
			preparedStatement.setInt(2, dID);
			preparedStatement.setDouble(3, similariy);
			preparedStatement.execute();
			preparedStatement.close();
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
