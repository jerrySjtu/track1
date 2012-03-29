package predict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import build.ItemBuildTask;

import data.InvertedIndex;
import data.Item;
import data.ItemDAO;
import data.PostList;
import data.PostNode;
import data.SortArray;
import data.User;
import data.UserDAO;

public class ItemBasedPredictor {
	private static final int LISTLIMIT = 100;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	private static InvertedIndex itemUserIndex;
	private static InvertedIndex userItemIndex;
	private static InvertedIndex keyItemIndex;
	private static InvertedIndex itemKeyIndex;
	
	static{
		if(itemKeyIndex == null)
			itemKeyIndex = ItemBuildTask.loadIndex("/home/sjtu123/data/track1/itemKeyIndex.ser");
		if(keyItemIndex == null)
			keyItemIndex = ItemBuildTask.loadIndex("/home/sjtu123/data/track1/keyItemIndex.ser");
		if(userItemIndex == null)
			userItemIndex = ItemBuildTask.loadIndex("/home/sjtu123/data/track1/userItemIndex.ser");
		if(itemUserIndex == null)
			itemUserIndex = ItemBuildTask.loadIndex("/home/sjtu123/data/track1/itemUserIndex.ser");
	}

//	public static void main(String[] args) {
//		int userID = 601635;
//		int itemID = 1774594;
//		SortArray array = recListByCF(userID);
//		System.out.println(array);
//	}
//	
	public static SortArray recListByKey(User user){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get accepted items of the user
		LinkedList<PostNode> ratedItems = UserDAO.getRatedItemByID(user.getId(), MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = ratedItems.iterator();
		while (itemIterator.hasNext()) {
			PostNode node = itemIterator.next();
			//get 50 most similar items 
			LinkedList<PostNode> similarItems = ItemDAO.getSimilarItemByKey(node.getKey());
			Iterator<PostNode> neighborIterator = similarItems.iterator();
			while(neighborIterator.hasNext()){
				PostNode neighbor = neighborIterator.next();
				int key = neighbor.getKey();
				if(rateMap.containsKey(key)){
					double value = rateMap.get(key) + node.getWeight() * neighbor.getWeight();
					double norm = normMap.get(key) + neighbor.getWeight();
					rateMap.put(key, value);
					normMap.put(key, norm);
				}
				else{
					rateMap.put(key, node.getWeight() * neighbor.getWeight());
					normMap.put(key, neighbor.getWeight());
				}
			}
		}
		//normalize and sort
		Set<Integer> keyset = rateMap.keySet();
		Iterator<Integer> keyIterator = keyset.iterator();
		SortArray sortArray = new SortArray(LISTLIMIT);
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			sortArray.insert(key, rateMap.get(key) / normMap.get(key));
		}
		return sortArray;
	}
	
	public static SortArray recListByCF(int userID){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get accepted items of the user
		LinkedList<PostNode> ratedItems = UserDAO.getRatedItemByID(userID, MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = ratedItems.iterator();
		while (itemIterator.hasNext()) {
			PostNode node = itemIterator.next();
			//get 50 most similar items 
			LinkedList<PostNode> similarItems = ItemDAO.getSimilarItemByCF(node.getKey());
			Iterator<PostNode> neighborIterator = similarItems.iterator();
			while(neighborIterator.hasNext()){
				PostNode neighbor = neighborIterator.next();
				int key = neighbor.getKey();
				if(rateMap.containsKey(key)){
					double value = rateMap.get(key) + node.getWeight() * neighbor.getWeight();
					double norm = normMap.get(key) + neighbor.getWeight();
					rateMap.put(key, value);
					normMap.put(key, norm);
				}
				else{
					rateMap.put(key, node.getWeight() * neighbor.getWeight());
					normMap.put(key, neighbor.getWeight());
				}
			}
		}
		//normalize and sort
		Set<Integer> keyset = rateMap.keySet();
		Iterator<Integer> keyIterator = keyset.iterator();
		SortArray sortArray = new SortArray(LISTLIMIT);
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			sortArray.insert(key, rateMap.get(key) / normMap.get(key));
		}
		return sortArray;
	}
	
	private Set<Integer> getSimItemByCF(int itemID) {
		Set<Integer> itemset = new HashSet<Integer>();
		String key = String.valueOf(itemID);
		PostList userList = itemUserIndex.getPostListByKey(key);
		if(userList != null){
			PostNode user = userList.getTop();
			while(user != null){
				key = String.valueOf(user.getKey());
				PostList itemList = userItemIndex.getPostListByKey(key);
				if(itemList != null){
					PostNode item = itemList.getTop();
					while(item != null){
						itemset.add(item.getKey());
						item = item.getNext();
					}
				}
				user = user.getNext();
			}
		}
		return itemset;
	}

	private Set<Integer> getSimItemByKey(int itemID) {
		Set<Integer> itemset = new HashSet<Integer>();
		String key = String.valueOf(itemID);
		PostList keyList = itemKeyIndex.getPostListByKey(key);
		if(keyList != null){
			PostNode keynode = keyList.getTop();
			while(keynode != null){
				key = String.valueOf(keynode.getKey());
				PostList itemList = keyItemIndex.getPostListByKey(key);
				if(itemList != null){
					PostNode item = itemList.getTop();
					while(item != null){
						itemset.add(item.getKey());
						item = item.getNext();
					}
				}
				keynode = keynode.getNext();
			}
		}
		return itemset;
	}

	private Set<Integer> getSimItemByCategory(Item item) {
		LinkedList<Item> itemInList = ItemDAO.getItemsInLog();
		Iterator<Item> itemIterator = itemInList.iterator();
		double maxinfo = 0;
		Item neighbor = null;
		while(itemIterator.hasNext()){
			Item tempItem = itemIterator.next();
			double info = ItemSimCalculator.calInfoValue(item, tempItem);
			if(info > maxinfo){
				maxinfo = info;
				neighbor = tempItem;
			}
		}
		return getSimItemByCF(neighbor.getId());
	}


}
