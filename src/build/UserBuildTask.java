package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.InvertedIndex;
import data.PostNode;
import data.User;
import data.UserDAO;

public class UserBuildTask {
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	
	public static void main(String[] args){
		String pathname = "/home/sjtu123/data/track1/userKeyIndex.ser";
		InvertedIndex userKeyIndex = buildUserKeyIndex();
		writeIndex(pathname, userKeyIndex);
		System.out.println(userKeyIndex.getIndexSize());
	}

	// build the user-item index
	private static InvertedIndex buildUserItemIndex() {
		// get all users with profile infromation
		LinkedList<User> userList = UserDAO.getAllUserProfile();
		InvertedIndex index = new InvertedIndex();
		Iterator<User> userIterator = userList.iterator();
		int i = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			HashSet<Integer> itemSet = UserDAO.getItemAcceptedByID(
					user.getId(), MINTIME, SEPTIME);
			Iterator<Integer> itemIterator = itemSet.iterator();
			String userID = String.valueOf(user.getId());
			while (itemIterator.hasNext()) {
				int itemID = itemIterator.next();
				PostNode postNode = new PostNode(itemID, 0);
				index.insertNode(userID, postNode);
			}
			System.out.println(i + " : " + user.getId());
			i++;
		}
		return index;
	}
	
	// build the key-user index
	private static InvertedIndex buildKeyUserIndex() {
		// get all users with profile infromation
		LinkedList<User> userList= UserDAO.getAllUserKeyWord();
		Iterator<User> userIterator = userList.iterator();
		InvertedIndex index = new InvertedIndex();
		int j = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			String[] keywords = user.getKeyWords();
			for (int i = 0; i < keywords.length; i++) {
				String[] array = keywords[i].split(":");
				//the information may be incomplete
				if (array.length == 2) {
					//regard the user id and weight as the key
					PostNode node = new PostNode(user.getId(),Double.parseDouble(array[1]));
					//regard the key word as the key
					index.insertNode(array[0], node);
				}
			}
			System.out.println(j + " : " + user.getId());
			j++;
		}
		return index;
	}
	
	private static InvertedIndex buildUserKeyIndex() {
		// get all users with profile infromation
		LinkedList<User> userList= UserDAO.getAllUserKeyWord();
		Iterator<User> userIterator = userList.iterator();
		InvertedIndex index = new InvertedIndex();
		int j = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			String[] keywords = user.getKeyWords();
			for (int i = 0; i < keywords.length; i++) {
				String[] array = keywords[i].split(":");
				// the information may be incomplete
				if (array.length == 2) {
					// regard the user id and weight as the key
					PostNode node = new PostNode(Integer.parseInt(array[0]), Double.parseDouble(array[1]));
					// regard the key word as the key
					index.insertNode(String.valueOf(user.getId()), node);
				}
			}
			System.out.println(j + " : " + user.getId());
			j++;
		}
		return index;
	}

	// build the tag-user index
	private static InvertedIndex buildTagUserIndex() {
		// get all users with profile infromation
		LinkedList<User> userList = UserDAO.getAllUserProfile();
		// get the idf vector
		System.out.println("get the idf vector--------------");
		Map<String, Double> tagIdfMap = tagIdfMap(userList);
		System.out.println("idf vector got--------------");
		InvertedIndex index = new InvertedIndex();
		Iterator<User> userIterator = userList.iterator();
		int i = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			// get the tf idf vector of the user
			Map<String, Double> tfIdfMap = tagTfMap(user, tagIdfMap);
			Set<String> keySet = tfIdfMap.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				// the tag is the key
				String key = keyIterator.next();
				PostNode postNode = new PostNode(user.getId(), tfIdfMap.get(key));
				// insert the post node to the index
				index.insertNode(key, postNode);
			}
			System.out.println(i + " : " + user.getId());
			i++;
		}
		return index;
	}
	
	private static InvertedIndex buildUserTagIndex(){
		LinkedList<User> userList = UserDAO.getAllUserProfile();
		Map<String, Double> tagIdfMap = tagIdfMap(userList);
		InvertedIndex index = new InvertedIndex();
		Iterator<User> userIterator = userList.iterator();
		int i = 1;
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			// get the tf idf vector of the user
			Map<String, Double> tfIdfMap = tagTfMap(user, tagIdfMap);
			Set<String> keySet = tfIdfMap.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				// the tag is the key
				String key = keyIterator.next();
				PostNode postNode = new PostNode(Integer.parseInt(key), tfIdfMap.get(key));
				index.insertNode(String.valueOf(user.getId()), postNode);
			}
			System.out.println(i + " th user is calculated--------------");
			i++;
		}
		return index;
	}
	
	public static InvertedIndex loadIndex(String pathName) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		InvertedIndex index = null;
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

	private static Map<String, Double> tagTfMap(User user, Map<String, Double> tagIdfMap) {
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
			// normalize
			Set<String> keySet = map.keySet();
			Iterator<String> iterator = keySet.iterator();
			String tag;
			while (iterator.hasNext()) {
				tag = iterator.next();
				map.put(tag, map.get(tag) * tagIdfMap.get(tag));
			}
		}
		return map;
	}


	// calculate the inverse document frequency by user's tag
	private static Map<String, Double> tagIdfMap(LinkedList<User> userList) {
		Map<String, Double> idfMap = new HashMap<String, Double>();
		Iterator<User> userIterator = userList.iterator();
		// calculate the document frequency
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			String[] tags = user.getTags();
			// 0 is a stop word
			if (tags[0].equals("0") == false) {
				Set<String> set = new HashSet<String>();
				for (int i = 0; i < tags.length; i++)
					set.add(tags[i]);
				Iterator<String> tagIterator = set.iterator();
				while (tagIterator.hasNext()) {
					String tag = tagIterator.next();
					if (idfMap.containsKey(tag))
						idfMap.put(tag, idfMap.get(tag) + 1);
					else
						idfMap.put(tag, 0.0);
				}
			}
		}
		// normalize
		Set<String> keySet = idfMap.keySet();
		Iterator<String> tagIterator = keySet.iterator();
		while (tagIterator.hasNext()) {
			String tag = tagIterator.next();
			idfMap.put(tag, idfMap.get(tag) / userList.size());
		}
		return idfMap;
	}
		
		
	// write the index to a file
	private static void writeIndex(String pathName, InvertedIndex index) {
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
	
	private void buildUserSNSTable(String pathname) {
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id, dID;
			while ((line = breader.readLine()) != null) {
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				dID = Integer.parseInt(temp[1]);
				UserDAO.insertUserSNS(id, dID);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildUserKeyWordTable(String pathname) {
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id;
			while ((line = breader.readLine()) != null) {
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				if (temp[1].length() > 512)
					temp[1] = temp[1].substring(0, 512);
				UserDAO.insertUserKeyWord(id, temp[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildUserProfileTable(String pathname) {
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			Matcher m;
			int id, birth, gender, tweetNum;
			Pattern pattern = Pattern.compile("(1|2)[0-9][0-9][0-9]");
			while ((line = breader.readLine()) != null) {
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				m = pattern.matcher(temp[1]);
				if (m.matches())
					birth = Integer.parseInt(temp[1]);
				else
					birth = 1988;
				gender = Integer.parseInt(temp[2]);
				tweetNum = Integer.parseInt(temp[3]);
				if (tweetNum > 32767)
					tweetNum = 32767;
				UserDAO.insertUserProfile(id, birth, gender, tweetNum, temp[4]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildUserActionTable(String pathname) {
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id, dID, atNum, retweetNum, commentNum;
			while ((line = breader.readLine()) != null) {
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				dID = Integer.parseInt(temp[1]);
				atNum = Integer.parseInt(temp[2]);
				retweetNum = Integer.parseInt(temp[3]);
				commentNum = Integer.parseInt(temp[4]);
				UserDAO.insertUserAction(id, dID, atNum, retweetNum, commentNum);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
