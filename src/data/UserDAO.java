package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class UserDAO {

	private static Connection conn;

	static {
		getConnection();
	}
	
	public static User getUserProfileByID(int userID){
		User user = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select tweet_num,tag from user_profile where id=?;");
			preparedStatement.setInt(1, userID);
			ResultSet result = preparedStatement.executeQuery();
			if(result.next())
				user = new User(userID, result.getInt(1), result.getString(2), null);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	public static LinkedList<User> getAllUserKey() {
		LinkedList<User> list = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select id,key_word from user_key;");
			ResultSet results = preparedStatement.executeQuery();
			list = new LinkedList<User>();
			while (results.next()) {
				User user = new User(results.getInt(1), 0, null,
						results.getString(2));
				list.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static LinkedList<User> getAllUserProfile() {
		LinkedList<User> list = null;
		try {
			PreparedStatement preparedStatement = conn
					.prepareStatement("select id,tweet_num,tag from user_profile;");
			ResultSet results = preparedStatement.executeQuery();
			list = new LinkedList<User>();
			while (results.next()) {
				User user = new User(results.getInt(1), results.getInt(2),
						results.getString(3), null);
				list.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
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
