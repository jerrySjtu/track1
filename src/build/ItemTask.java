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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import data.CategoryKey;
import data.InvertedIndex;
import data.Item;
import data.ItemDAO;
import data.PostNode;
import data.SortArray;
import data.SortEntry;

public class ItemTask{
	private final int BESTNUM = 300;
	private static HashMap<String, Double> tfMap;
	private static LinkedList<Item> itemList;
	// the minimum time
	private final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private final long SEPTIME = 1320537601;
	// the maximum time
	private final long MAXTIME = 1321027199;

	public static void main(String[] args) {
		ItemTask instance = new ItemTask();
		instance.calCategorySimilarity();
	}
	
	public void calCategorySimilarity(){
		//calculate the information content value
		System.out.println("begin to calcualte information content value...");
		Map<CategoryKey, Double> infoValueMap = calCategoryProbability();
		System.out.println("finished...");
		//get items not in the log 
		LinkedList<Item> itemNotList = ItemDAO.getItemsNotInLog();
		LinkedList<Item> itemInList = ItemDAO.getItemsInLog();
		Iterator<Item> itemNotIterator = itemNotList.iterator();
		int i = 1;
		while(itemNotIterator.hasNext()){
			Item itemNot = itemNotIterator.next();
			double maxValue = -1;
			//item 786313 is in the recommendation log
			Item similarItem = ItemDAO.getItemByID(786313);
			Iterator<Item> itemInIterator = itemInList.iterator();
			while(itemInIterator.hasNext()){
				Item itemIn = itemInIterator.next();
				double infoValue = calInfoValue(infoValueMap, itemNot, itemIn);
				if(maxValue < infoValue){
					maxValue = infoValue;
					similarItem = itemIn;
				}
			}
			//write to database
			ItemDAO.insetItemCategorySim(itemNot.getId(), similarItem.getId());
			System.out.println(i + " item finished..."); 
			i++;
		}
	}
	
	public double calInfoValue(Map<CategoryKey, Double> categoryMap, Item item1, Item item2){
		int[] array1 = item1.getCategory();
		int[] array2 = item2.getCategory();
		int i;
		for(i = 0; i < 4; i++)
			if(array1[i] != array2[i])
				break;
		//different category in the root layer
		CategoryKey key;
		if(i == 0)
			return 0;
		if(i == 1)
			key = new CategoryKey(0, 0, array1[i-1]);
		else
			key = new CategoryKey(i-1, array1[i-2], array1[i-1]);
		return categoryMap.get(key);
	}
	
	//calculate the information content value of each category
	public Map<CategoryKey,Double> calCategoryProbability() {
		// build the category map
		Map<CategoryKey, Double> categoryMap = new HashMap<CategoryKey, Double>();
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		Iterator<Item> iterator = itemList.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			int[] cateArray = item.getCategory();
			for (int i = 0; i < 4; i++) {
				//0 is the root node
				CategoryKey key;
				if(i == 0)
					key = new CategoryKey(i, 0, cateArray[i]);
				else
					key = new CategoryKey(i, cateArray[i-1], cateArray[i]);
				if (categoryMap.containsKey(key)) {
					double value = categoryMap.get(key) + 1;
					categoryMap.put(key, value);
				}
				else
					categoryMap.put(key, 1.0);
			}
		}
		// normalize: calculate the probability for each node
		Iterator<CategoryKey> keyIterator = categoryMap.keySet().iterator();
		while(keyIterator.hasNext()){
			CategoryKey key = keyIterator.next();
			double value = categoryMap.get(key) / itemList.size();
			categoryMap.put(key, -Math.log(value));
		}
		return categoryMap;
	}

	// calculate the CF similarity among all items
	public void calSimilarityByCF() {
		System.out.println("load item-user index----------------");
		String pathname = "/home/sjtu123/data/track1/itemUserIndex.ser";
		InvertedIndex index = loadIndex(pathname);
		System.out.println("finish loading----------------");
		ArrayList<String> itemArray = index.keyArray();
		System.out.println("item size: " + itemArray.size());
		for (int i = 0; i < itemArray.size(); i++) {
			String itemID1 = itemArray.get(i);
			Map<Integer, Double> map1 = index.docMap(itemID1);
			// calculate the similarity between item i and item j
			for (int j = i + 1; j < itemArray.size(); j++) {
				String itemID2 = itemArray.get(j);
				Map<Integer, Double> map2 = index.docMap(itemID2);
				Set<Integer> docUnion = index.docUnion(itemID1, itemID1);
				double similariy = similarityByCF(docUnion, map1, map2);
				// write to database
				if (similariy != 0) {
					ItemDAO.insertItemCFSim(Integer.parseInt(itemID1),Integer.parseInt(itemID2), similariy);
					ItemDAO.insertItemCFSim(Integer.parseInt(itemID2),Integer.parseInt(itemID1), similariy);
				}
			}
			System.out.println(i + "th item finsihed calculating CF sim----------------");
		}
	}

	// calculate the CF similarity between two items
	private double similarityByCF(Set<Integer> docUnion,
			Map<Integer, Double> map1, Map<Integer, Double> map2) {
		Iterator<Integer> iterator = docUnion.iterator();
		double length1 = 0;
		double length2 = 0;
		double similarity = 0;
		while (iterator.hasNext()) {
			int itemID = iterator.next();
			if (map1.containsKey(itemID))
				length1++;
			if (map2.containsKey(itemID))
				length2++;
			if (map1.containsKey(itemID) && map2.containsKey(itemID))
				similarity++;
		}
		//there are some users who have no record.
		if(length1 * length2 == 0)
			return 0;
		return similarity / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	// calculate the similarity among all items by key similarity
	public void calSimlarityByKeyWord() {
		termFrequency();
		Item item, dItem;
		double similariy;
		for (int i = 0; i < BESTNUM; i++) {
			item = itemList.get(i);
			SortArray sortArray = new SortArray(BESTNUM);
			// calculate the similarity between item i and item j
			for (int j = i + 1; j < BESTNUM; j++) {
				dItem = itemList.get(j);
				similariy = simlarityByKeyWord(item, dItem);
				sortArray.insert(dItem.getId(), similariy);
			}
			SortEntry top = sortArray.getTop();
			// write to the database
			while (top != null) {
				ItemDAO.insertItemKeySim(item.getId(), top.getKey(),
						top.getValue());
				top = top.getNext();
			}
		}
	}

	/**
	 * 
	 * @param item
	 * @param dItem
	 * @return the similarity of the two items
	 */
	private double simlarityByKeyWord(Item item, Item dItem) {
		Map<String, Double> map1 = tfInDoc(item);
		Map<String, Double> map2 = tfInDoc(dItem);
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(map1.keySet());
		keySet.addAll(map2.keySet());
		Iterator<String> iterator = keySet.iterator();
		String key;
		double product = 0.0;
		double length1 = 0.0;
		double length2 = 0.0;
		while (iterator.hasNext()) {
			key = iterator.next();
			if (map1.containsKey(key))
				length1 += map1.get(key) * map1.get(key);
			if (map2.containsKey(key))
				length2 += map2.get(key) * map2.get(key);
			if (map1.containsKey(key) && map2.containsKey(key))
				product += map1.get(key) * map2.get(key);
		}
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	/**
	 * @param item
	 * @return normalized idf
	 */
	private HashMap<String, Double> tfInDoc(Item item) {
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
		String key;
		double value;
		while (iterator.hasNext()) {
			key = iterator.next();
			value = map.get(key);
			map.put(key, value * tfMap.get(key));
		}
		return map;
	}

	private void termFrequency() {
		if (itemList == null)
			itemList = ItemDAO.readAllItems();
		if (tfMap == null) {
			tfMap = new HashMap<String, Double>();
			Iterator<Item> iterator = itemList.iterator();
			Item item;
			String[] keywords;
			String key;
			double value;
			while (iterator.hasNext()) {
				item = iterator.next();
				keywords = item.getKeyWords();
				HashSet<String> set = new HashSet<String>();
				for (int i = 0; i < keywords.length; i++)
					set.add(keywords[i]);
				Iterator<String> wordIterator = set.iterator();
				while (wordIterator.hasNext()) {
					key = wordIterator.next();
					if (tfMap.containsKey(key) == false)
						tfMap.put(key, 1.0);
					else
						tfMap.put(key, tfMap.get(key) + 1);
				}
			}// end while
				// normalize
			Set<String> keySet = tfMap.keySet();
			Iterator<String> keyIterator = keySet.iterator();
			while (keyIterator.hasNext()) {
				key = keyIterator.next();
				value = Math.log(tfMap.get(key) / itemList.size());
				tfMap.put(key, value);
			}
		}
	}
	
	/**
	 * build the inverted document index with the item id as the term and user
	 * id as the document
	 */
	public InvertedIndex buildCFIndex() {
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

	/**
	 * @param index
	 * @param pathName
	 *            write the index to the file
	 */
	public void writeIndex(InvertedIndex index, String pathName) {
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
	public InvertedIndex loadIndex(String pathName) {
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
