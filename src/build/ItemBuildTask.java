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
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import data.InvertedIndex;
import data.Item;
import data.ItemDAO;
import data.PostNode;

public class ItemBuildTask{
	// the minimum time
	private final static long MINTIME = 1318348785;
	// the time separate the train set and test set
	private final static long SEPTIME = 1320537601;
	// the maximum time
	
	public static void main(String[] args){
		String pathName = "/home/sjtu123/data/track1/keyItemIndex.ser";
		InvertedIndex index = buildKeyItemIndex();
		writeIndex(index, pathName);
//		InvertedIndex index = loadIndex(pathName);
		System.out.println(index.getPostListByKey("974"));
	}

	/**
	 * build the inverted document index with the item id as the term and user
	 * id as the document
	 */
	private static InvertedIndex buildCFIndex() {
		// get all items
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		System.out.println(itemList.size());
		Iterator<Item> itemIterator = itemList.iterator();
		Item item;
		LinkedList<Integer> userList;
		InvertedIndex index = new InvertedIndex();
		int userID;
		int i = 1;
		while (itemIterator.hasNext()) {
			item = itemIterator.next();
			userList = ItemDAO.getUserByAcceptedItem(item.getId(), MINTIME, SEPTIME);
			Iterator<Integer> userIterator = userList.iterator();
			while (userIterator.hasNext()) {
				userID = userIterator.next();
				PostNode postNode = new PostNode(userID, 0);
				// insert the node
				index.insertNode(Integer.toString(item.getId()), postNode);
			}
			System.out.println("The index of " + i + " item is calculated.");
			i++;
		}
		return index;
	}
	
	private static InvertedIndex buildItemKeyIndex(){
		InvertedIndex itemKeyIndex = new InvertedIndex();
		//get idf map
		Map<String,Double> idfMap = getIdfMap();
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		Iterator<Item> itemIterator = itemList.iterator();
		int i = 1;
		while(itemIterator.hasNext()){
			Item item = itemIterator.next();
			//get term frequency map
			Map<String,Double>tfMap = getTfMap(item, idfMap);
			Set<String> keyset = tfMap.keySet();
			Iterator<String> keyIterator = keyset.iterator();
			while(keyIterator.hasNext()){
				String key = keyIterator.next();
				PostNode node = new PostNode(Integer.parseInt(key), tfMap.get(key));
				itemKeyIndex.insertNode(String.valueOf(item.getId()), node);
			}
			System.out.println(i + " th item is calculated");
			i++;
		}
		return itemKeyIndex;
	}
	
	private static InvertedIndex buildKeyItemIndex(){
		InvertedIndex keyItemIndex = new InvertedIndex();
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		Iterator<Item> itemIterator = itemList.iterator();
		int j = 1;
		while(itemIterator.hasNext()){
			Item item = itemIterator.next();
			String[] keys = item.getKeyWords();
			for(int i = 0; i< keys.length; i++){
				PostNode node = new PostNode(item.getId(), 0);
				keyItemIndex.insertNode(keys[i], node);
			}
			System.out.println(j + " th item is calculated");
			j++;
		}
		return keyItemIndex;
	}
	
	/**
	 * @param item
	 * @return normalized term frequency of the item
	 */
	private static Map<String, Double> getTfMap(Item item, Map<String, Double> idfMap) {
		String[] wordArray = item.getKeyWords();
		HashMap<String, Double> map = new HashMap<String, Double>();
		// term frequency in document
		for (int i = 0; i < wordArray.length; i++) {
			if (map.containsKey(wordArray[i]))
				map.put(wordArray[i], map.get(wordArray[i]) + 1);
			else
				map.put(wordArray[i], 1.0);
		}
		// normalize the term frequency by idf
		Set<String> keySet = map.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			double value = map.get(key);
			map.put(key, value * idfMap.get(key));
		}
		return map;
	}
	
	// return inverted document frequency map
	private static Map<String, Double> getIdfMap() {
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		Map<String, Double> idfMap = new HashMap<String, Double>();
		Iterator<Item> iterator = itemList.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			String[] keywords = item.getKeyWords();
			HashSet<String> set = new HashSet<String>();
			for (int i = 0; i < keywords.length; i++)
				set.add(keywords[i]);
			Iterator<String> wordIterator = set.iterator();
			while (wordIterator.hasNext()) {
				String key = wordIterator.next();
				if (idfMap.containsKey(key) == false)
					idfMap.put(key, 1.0);
				else
					idfMap.put(key, idfMap.get(key) + 1);
			}
		}// end while
		// normalize
		Set<String> keySet = idfMap.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			double value = -Math.log(idfMap.get(key) / itemList.size());
			idfMap.put(key, value);
		}
		return idfMap;
	}

	/**
	 * @param index
	 * @param pathName
	 *            write the index to the file
	 */
	private static void writeIndex(InvertedIndex index, String pathName) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pathName);
			out = new ObjectOutputStream(fos);
			out.writeObject(index);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param pathName
	 * @return load the index from the file
	 */
	public static InvertedIndex loadIndex(String pathName) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		InvertedIndex index = null;
		try {
			fis = new FileInputStream(pathName);
			in = new ObjectInputStream(fis);
			index = (InvertedIndex) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return index;
	}

	private static void buildItemTable() {
		String pathname = "/home/sjtu123/data/track1/item.txt";
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
				ItemDAO.insertItem(id, temp[1], temp[2]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
