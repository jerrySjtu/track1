package predict;

import java.io.ObjectInputStream.GetField;
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

	public static void main(String[] args) {
		System.out.println("------------");
		int userID = 1219311;
		User user = UserDAO.getUserKeyByID(userID);
		int itemID1 = 1606574;
		int itemID2 = 1774599;
		Item item = ItemDAO.getItemByID(itemID1);
		double pred1 = predictByCategory(user, item);
		double pred2 = predictByKey(user, item);
		double pred3 = predictByCF(user, item);
		System.out.println(pred1);
		System.out.println(pred2);
		System.out.println(pred3);
		
		item = ItemDAO.getItemByID(itemID2);
		pred1 = predictByCategory(user, item);
		pred2 = predictByCategory(user, item);
		pred3 = predictByCF(user, item);
		System.out.println(pred1);
		System.out.println(pred2);
		System.out.println(pred3);
		
	}
	
	public static SortArray recListByKey(User user){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get accepted items of the user
		LinkedList<PostNode> ratedItems = UserDAO.getRatedItemByID(user.getId(), MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = ratedItems.iterator();
		//whether all the ratings are 1
		boolean isAllAccpted = true;
		while (itemIterator.hasNext()) {
			PostNode node = itemIterator.next();
			if(node.getWeight() == -1)
				isAllAccpted = false;
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
			//get 50 most similar items 7
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
	
	public static double predictByCF(User user, Item item){
		LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(user.getId(), MINTIME, SEPTIME);
		System.out.println("rated item size: " + itemList.size());
		Iterator<PostNode> itemIterator = itemList.iterator();
		double rating = 0;
		double norm = 0;
		boolean isAllAccpted = true;
		while(itemIterator.hasNext()){
			PostNode ratedItem = itemIterator.next();
			if(ratedItem.getWeight() == -1)
				isAllAccpted = false;
			double sim = ItemSimCalculator.calCFSim(ratedItem.getKey(), item.getId(), itemUserIndex);
			//System.out.println("cf sim:<" + ratedItem.getKey() + ","+item.getId()+","+ sim +">" );
			rating += sim * ratedItem.getWeight();
			norm += sim;
		}
		if(isAllAccpted == false)
			return rating / norm;
		else 
			return rating / itemList.size();
	}
	
	public static double predictByKey(User user, Item item){
		LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(user.getId(), MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = itemList.iterator();
		double rating = 0;
		double norm = 0;
		boolean isAllAccpted = true;
		while(itemIterator.hasNext()){
			PostNode node = itemIterator.next();
			Item ratedItem = ItemDAO.getItemByID(node.getKey()); 
			if(node.getWeight() == -1)
				isAllAccpted = false;
			double sim = ItemSimCalculator.calKeySim(item.getId(), ratedItem.getId(), itemKeyIndex);
			rating += sim * node.getWeight();
			norm += sim;
			//System.out.println("key sim:<" + node.getKey() + ","+item.getId()+","+ sim +">" );
		}
		if(isAllAccpted == false)
			return rating / norm;
		else
			return rating / itemList.size();
	}
	
	public static double predictByCategory(User user, Item item){
		Item neighbor = getNeighborByCategory(item);
		LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(user.getId(), MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = itemList.iterator();
		double rating = 0;
		double norm = 0;
		boolean isAllAccpted = true;
		while(itemIterator.hasNext()){
			PostNode ratedItem = itemIterator.next();
			if(ratedItem.getWeight() == -1)
				isAllAccpted = false;
			double sim = ItemSimCalculator.calCFSim(ratedItem.getKey(), neighbor.getId(), itemUserIndex);
			rating += sim * ratedItem.getWeight();
			norm += sim;
			//System.out.println("category sim:<" + ratedItem.getKey() + ","+item.getId()+","+ sim +">" );
		}
		if(isAllAccpted == false)
			return rating / norm;
		else
			return rating / itemList.size();
	}
	
	private static Set<Integer> getNeighborsByCF(int itemID) {
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

	private static Set<Integer> getNeighborsByKey(int itemID) {
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

	private static Set<Integer> getNeighborsByCategory(Item item) {
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
		return getNeighborsByCF(neighbor.getId());
	}
	
	private static Item getNeighborByCategory(Item item) {
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
		return neighbor;
	}


}
