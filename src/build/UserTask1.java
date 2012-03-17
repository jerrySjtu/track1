package build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import data.InvertedIndex;
import data.PostNode;
import data.User;
import data.UserDAO;

/**
 * 
 * @author sjtu123 UserTask1 take advantage of the inverted document index to
 *         speed up the calculation of similarity of users by tag
 */
public class UserTask1 {
	private static LinkedList<User> userList;
	private static InvertedIndex index;
	private static HashMap<String, Double> idfMap;

	static {
		if (userList == null)
			idfTag();// get the idf and user list
	}

	public static void main(String[] args) {
		String pathName = "/home/sjtu123/data/track1/userTagIndex.ser";
		System.out.println("begin to build index--------------");
		buildIndex();
		System.out.println("finish building index --------------");
		System.out.println("begin to write index--------------");
		writeIndex(pathName);
		System.out.println("finish writing index --------------");
	}

	/**
	 * build the inverted document index
	 */
	private static void buildIndex() {
		index = new InvertedIndex();
		Iterator<User> userIterator = userList.iterator();
		User user;
		String[] tags;
		PostNode postNode;
		String key;
		int i = 1;
		while (userIterator.hasNext()) {
			user = userIterator.next();
			// get the tf idf vector of the user
			HashMap<String, Double> tfIdfMap = tfTag(user);
			Set<String> keySet = tfIdfMap.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				// the tag is the key
				key = keyIterator.next();
				postNode = new PostNode(user.getId(), tfIdfMap.get(key));
				// insert the post node to the index
				index.insertNode(key, postNode);
			}
			System.out.println(i + " : " + user.getId());
			i++;
		}
	}

	/**
	 * @param pathName
	 *            write the index to a file
	 */
	private static void writeIndex(String pathName) {
		FileOutputStream fos = null; 
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pathName);
			out = new ObjectOutputStream(fos);
			out.writeObject(index);
			out.flush();
			out.close();
			fos.close();
		} catch (IOException e) {
			LogManager.writeLogToFile(e.getMessage());
			e.printStackTrace();
		}
	}

	public static InvertedIndex loadIndex(String pathName) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(pathName);
			in = new ObjectInputStream(fis);
			index = (InvertedIndex) in.readObject();
			in.close();
			fis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return index;
	}

	private static HashMap<String, Double> tfTag(User user) {
		String[] tags = user.getTags();
		HashMap<String, Double> map = new HashMap<String, Double>();
		// 0 is a stop word
		if (tags[0].equals("0") == false) {
			// term frequency
			for (int i = 0; i < tags.length; i++) {
				if (map.containsKey(tags[i]))
					map.put(tags[i], map.get(tags[i]) + 1);
				else
					map.put(tags[i], 1.0);
			}
			// tf * idf
			Set<String> keySet = map.keySet();
			Iterator<String> iterator = keySet.iterator();
			String tag;
			while (iterator.hasNext()) {
				tag = iterator.next();
				map.put(tag, map.get(tag) * idfMap.get(tag));
			}
		}
		return map;
	}

	/**
	 * calculate the inverse document frequency
	 */
	private static HashMap<String, Double> idfTag() {
		if (userList == null)
			userList = UserDAO.getAllUserProfile();
		if (idfMap == null) {
			idfMap = new HashMap<String, Double>();
			Iterator<User> userIterator = userList.iterator();
			User user;
			String[] tags;
			HashSet<String> set;
			Iterator<String> tagIterator;
			String tag;
			// calculate the document frequency
			while (userIterator.hasNext()) {
				user = userIterator.next();
				tags = user.getTags();
				// 0 is a stop word
				if (tags[0].equals("0") == false) {
					set = new HashSet<String>();
					for (int i = 0; i < tags.length; i++)
						set.add(tags[i]);
					tagIterator = set.iterator();
					while (tagIterator.hasNext()) {
						tag = tagIterator.next();
						if (idfMap.containsKey(tag))
							idfMap.put(tag, idfMap.get(tag) + 1);
						else
							idfMap.put(tag, 0.0);
					}
				}
			}
			// normalize
			Set<String> keySet = idfMap.keySet();
			tagIterator = keySet.iterator();
			while (tagIterator.hasNext()) {
				tag = tagIterator.next();
				idfMap.put(tag, idfMap.get(tag) / userList.size());
			}
		}
		return idfMap;
	}
}
