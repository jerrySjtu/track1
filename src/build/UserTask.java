package build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import data.SortArray;
import data.SortEntry;
import data.User;
import data.UserDAO;

public class UserTask implements Runnable {
	private static final int BESTNUM = 20;
	private static HashMap<String, Double> idfMap;
	private static LinkedList<User> userList;

	public static void main(String[] args) {
		UserTask instance = new UserTask();
		System.out.println("begins----------------");
		//instance.allSimilarityByTag();
		System.out.println("ends----------------");
	}

	@Override
	public void run() {
		
	}

	/**
	 * calculate the similarity among all users and write to database
	 */
	public void allSimilarityByTag() {
		if (userList == null)
			userList = UserDAO.getAllUserProfile();
		double similarity;
		User user1, user2;
		for (int i = 0; i < userList.size(); i++) {
			//the user has tags
			user1 = userList.get(i);
			SortArray sortArray = new SortArray(BESTNUM);
			if (user1.getTags()[0].equals("0") == false) {
				for (int j = i + 1; j < userList.size(); j++) {
					System.out.println("The user (" + i + ","+ j+ ") user is calculated...");
					user2 = userList.get(j);
					//the user has tags
					if (user2.getTags()[0].equals("0") == false) {
						similarity = simlarityByTag(user1, user2);
						sortArray.insert(user2.getId(), similarity);
					}
				}
			}
			SortEntry top = sortArray.getTop();
			while(top != null){
				UserDAO.insertSimByTag(user1.getId(), top.getKey(), top.getValue());
				top = top.getNext();
			}
		}//end for
	}

	/**
	 * @param user1
	 * @param user2
	 * @return the similarity calculated by tags between user1 and user2
	 */
	private double simlarityByTag(User user1, User user2) {
		HashMap<String, Double> map1 = tfTag(user1);
		HashMap<String, Double> map2 = tfTag(user2);
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(map1.keySet());
		keySet.addAll(map2.keySet());
		Iterator<String> keyIterator = keySet.iterator();
		String key;
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (keyIterator.hasNext()) {
			key = keyIterator.next();
			if (map1.containsKey(key))
				length1 += map1.get(key) * map1.get(key);
			if (map2.containsKey(key))
				length2 += map2.get(key) * map2.get(key);
			if (map1.containsKey(key) && map2.containsKey(key))
				product += map1.get(key) * map2.get(key);
		}
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	private HashMap<String, Double> tfTag(User user) {
		String[] tags = user.getTags();
		HashMap<String, Double> map = new HashMap<String, Double>();
		// term frequency
		for (int i = 0; i < tags.length; i++) {
			if (map.containsKey(tags[i]))
				map.put(tags[i], map.get(tags[i]) + 1);
			else
				map.put(tags[i], 1.0);
		}
		// tf * idf
		idfTag();// get the idf
		Set<String> keySet = map.keySet();
		Iterator<String> iterator = keySet.iterator();
		String tag;
		while (iterator.hasNext()) {
			tag = iterator.next();
			map.put(tag, map.get(tag) * idfMap.get(tag));
		}
		return map;
	}

	/**
	 * calculate the inverse document frequency
	 */
	private HashMap<String, Double> idfTag() {
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
