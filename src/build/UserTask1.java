package build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import data.InvertedIndex;
import data.PostList;
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
		System.out.println("begins--------------");
		String pathName = "/home/sjtu123/data/track1/userTagIndex.ser";
		calUserTagSim(pathName);
//		String pathName = "/home/sjtu123/data/track1/itemCFIndex.ser";
//		calUserCFSim(pathName);
		System.out.println("finished--------------");
	}

	/**
	 * @param pathName
	 *            the path of the item-user index file calculate the similarity
	 *            among all users by CF and write the results to table
	 *            user_sim_cf
	 */
	public static void calUserCFSim(String pathName) {
		InvertedIndex index = loadIndex(pathName);
		// get all users
		LinkedList<User> users = UserDAO.getAllUserProfile();
		Iterator<User> userIterator = users.iterator();
		int i = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			//get all users who accepted some common items
			Set<Integer> neighborSet = getNeighborByItem(index, user);
			Iterator<Integer> neighborIerator = neighborSet.iterator();
			while(neighborIerator.hasNext()){
				int neighborID = neighborIerator.next();
				double similarity = cfSimilarity(user.getId(), neighborID);
				UserDAO.insertSimByCF(user.getId(), neighborID, similarity);
			}
			System.out.println(i + " user's cf sim finished--------------");
			i++;
		}
	}

	/**
	 * @param pathName
	 *            the path of the tag-user index file calculate the similarity
	 *            among all users by tag and write the results to table
	 *            user_sim_tag
	 */
	public static void calUserTagSim(String pathName) {
		InvertedIndex index = loadIndex(pathName);
		// get all users
		LinkedList<User> users = UserDAO.getAllUserProfile();
		Iterator<User> userIterator = users.iterator();
		int i = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			Set<Integer> neighborSet = getNeighborSetByTag(index, user);
			// get all the users who have some common tags
			Iterator<Integer> neighborIterator = neighborSet.iterator();
			// calculate the similarity and write to database
			while (neighborIterator.hasNext()) {
				User neighbor = UserDAO.getUserProfileByID(neighborIterator
						.next());
				double similarity = profileSimilarity(user, neighbor);
				UserDAO.insertSimByTag(user.getId(), neighbor.getId(),
						similarity);
			}
			System.out.println(i + " user's tag sim finished--------------");
			i++;
		}
	}
	
	// calculate the similarity of the two users by cf
	private static double cfSimilarity(int user1, int user2){
		Set<Integer> set1 = UserDAO.getItemAcceptedByID(user1);
		Set<Integer> set2 = UserDAO.getItemAcceptedByID(user2);
		Set<Integer> itemSet = new HashSet<Integer>();
		itemSet.addAll(set1);
		itemSet.addAll(set2);
		Iterator<Integer> itemIterator = itemSet.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while(itemIterator.hasNext()){
			int itemID = itemIterator.next();
			if(set1.contains(itemID))
				length1++;
			if(set2.contains(itemID))
				length2++;
			if(set1.contains(itemID) && set2.contains(itemID))
				product++;
		}
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	// calculate the similarity of the two users by profile
	private static double profileSimilarity(User user1, User user2) {
		Map<String, Double> map1 = tfTag(user1);
		Map<String, Double> map2 = tfTag(user2);
		Set<String> union = new HashSet<String>();
		union.addAll(map1.keySet());
		union.addAll(map2.keySet());
		Iterator<String> iterator = union.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (iterator.hasNext()) {
			String tag = iterator.next();
			if (map1.containsKey(tag))
				length1 += map1.get(tag) * map1.get(tag);
			if (map2.containsKey(tag))
				length2 += map2.get(tag) * map2.get(tag);
			if (map1.containsKey(tag) && map2.containsKey(tag))
				product += map1.get(tag) * map2.get(tag);
		}
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	// get all the users who accept some common item
	private static Set<Integer> getNeighborByItem(InvertedIndex index, User user) {
		Set<Integer> itemList = UserDAO.getItemAcceptedByID(user.getId());
		Iterator<Integer> itemIterator = itemList.iterator();
		Set<Integer> neighborSet = new HashSet<Integer>();
		while (itemIterator.hasNext()) {
			int itemID = itemIterator.next();
			PostList postList = index
					.getPostListByKey(Integer.toString(itemID));
			PostNode node = postList.getTop();
			while (node != null) {
				neighborSet.add(node.getKey());
				node = node.getNext();
			}
		}
		return neighborSet;
	}

	// get all the users who have some common tags
	private static Set<Integer> getNeighborSetByTag(InvertedIndex index, User user) {
		String[] tags = user.getTags();
		Set<Integer> neighborSet = new HashSet<Integer>();
		for (int i = 0; i < tags.length; i++) {
			PostList postList = index.getPostListByKey(tags[i]);
			PostNode temp = postList.getTop();
			while (temp != null) {
				neighborSet.add(temp.getKey());
				temp = temp.getNext();
			}
		}
		return neighborSet;
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
