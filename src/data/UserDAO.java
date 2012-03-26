package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class UserDAO {

	private static Connection conn;

	static {
		getConnection();
	}
	
	public static LinkedList<PostNode> getNeighborByKey(int userID){
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement statement = conn.prepareStatement("select d_id, sim from user_sim_key where id=?");
			statement.setInt(1, userID);
			ResultSet result = statement.executeQuery();
			while(result.next()){
				PostNode node = new PostNode(result.getInt(1), result.getDouble(2));
				list.add(node);
			}
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static LinkedList<PostNode> getNeighborByTag(int userID){
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		try {
			PreparedStatement statement = conn.prepareStatement("select d_id, sim from user_sim_tag where id=?");
			statement.setInt(1, userID);
			ResultSet result = statement.executeQuery();
			while(result.next()){
				PostNode node = new PostNode(result.getInt(1), result.getDouble(2));
				list.add(node);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static double getRateMean(int userID, long minTime, long maxTime) {
		double mean = 0;
		try {
			PreparedStatement statement = conn
					.prepareStatement("select sum(result)/count(*) from rec_log where user_id=? and rec_time>=? and rec_time<=?;");
			statement.setInt(1, userID);
			statement.setString(2, String.valueOf(minTime));
			statement.setString(3, String.valueOf(maxTime));
			ResultSet results = statement.executeQuery();
			if (results.next())
				mean = results.getDouble(1);
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mean;
	}

	// get the items which are rated by the user between minTime and maxTime
	public static LinkedList<PostNode> getRatedItemByID(int userID,
			long minTime, long maxTime) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		PreparedStatement statement;
		try {
			statement = conn
					.prepareStatement("select item_id, result from rec_log where user_id=? and rec_time>=? and rec_time<=?;");
			statement.setInt(1, userID);
			statement.setString(2, String.valueOf(minTime));
			statement.setString(3, String.valueOf(maxTime));
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				PostNode node = new PostNode(results.getInt(1),results.getInt(2));
				list.add(node);
			}
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// get the items which are accepted by the user between minTime and maxTime
	public static HashSet<Integer> getItemAcceptedByID(int userID,
			long minTime, long maxTime) {
		HashSet<Integer> set = new HashSet<Integer>();
		try {
			PreparedStatement statement = conn
					.prepareStatement("select item_id, rec_time from rec_log where user_id=? and result=1");
			statement.setInt(1, userID);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				long timestamp = Long.parseLong(results.getString(2));
				if (timestamp >= minTime && timestamp <= maxTime)
					set.add(results.getInt(1));
			}
			results.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static User getUserProfileByID(int userID) {
		User user = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select tweet_num,tag from user_profile where id=?;");
			preparedStatement.setInt(1, userID);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next())
				user = new User(userID, result.getInt(1), result.getString(2),
						null);
			result.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	public static Map<Integer, String> getAllUserKeyWord() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select id,key_word from user_key;");
			ResultSet results = preparedStatement.executeQuery();
			while (results.next())
				map.put(results.getInt(1), results.getString(2));
			results.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public static LinkedList<User> getAllUserProfile() {
		LinkedList<User> list = new LinkedList<User>();
		;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select id,tweet_num,tag from user_profile;");
			ResultSet results = preparedStatement.executeQuery();
			while (results.next()) {
				User user = new User(results.getInt(1), results.getInt(2),
						results.getString(3), null);
				list.add(user);
			}
			results.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void insertKeySim(int userID1, int userID2, double similarity) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into user_sim_key(id,d_id,sim) values(?,?,?)");
			preparedStatement.setInt(1, userID1);
			preparedStatement.setInt(2, userID2);
			preparedStatement.setDouble(3, similarity);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertSimByCF(int userID1, int userID2, double similarity) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into user_sim_cf(id,d_id,sim) values(?,?,?)");
			preparedStatement.setInt(1, userID1);
			preparedStatement.setInt(2, userID2);
			preparedStatement.setDouble(3, similarity);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertSimByTag(int userID1, int userID2,
			double similarity) {
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("insert into user_sim_tag(id,d_id,sim) values(?,?,?)");
			preparedStatement.setInt(1, userID1);
			preparedStatement.setInt(2, userID2);
			preparedStatement.setDouble(3, similarity);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertUserProfile(int id, int birth, int gender,
			int tweetNum, String tag) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into user_profile(id, birth, gender, tweet_num, tag) values(?,?,?,?,?);");
			preparedStatement.setInt(1, id);
			preparedStatement.setInt(2, birth);
			preparedStatement.setInt(3, gender);
			preparedStatement.setInt(4, tweetNum);
			preparedStatement.setString(5, tag);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertUserAction(int id, int dID, int atNum,
			int retweetNum, int commentNum) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into user_action(id, d_id, at_num, retweet_num, comment_num) values(?,?,?,?,?);");
			preparedStatement.setInt(1, id);
			preparedStatement.setInt(2, dID);
			preparedStatement.setInt(3, atNum);
			preparedStatement.setInt(4, retweetNum);
			preparedStatement.setInt(5, commentNum);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("id and d_id are : (" + id + ", " + dID + ")");
		}
	}

	public static void insertUserKeyWord(int id, String keyWord) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into user_key(id, key_word) values(?,?);");
			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, keyWord);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertUserSNS(int id, int dID) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn
					.prepareStatement("insert into user_sns(id, d_id) values(?,?);");
			preparedStatement.setInt(1, id);
			preparedStatement.setInt(2, dID);
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

}
