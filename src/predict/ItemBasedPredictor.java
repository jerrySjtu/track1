package predict;

import java.util.List;
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
	private static LinkedList<Item> itemsInLog;
	private static Set<Integer> itemsetNotIn;
	
	public static void main(String[] args) {
		System.out.println("------------");
		int userID = 2253039;
		//User user = UserDAO.getUserKeyByID(userID);
		int itemID1 = 1928678;
		int itemID2 = 1774599;
		Item item = ItemDAO.getItemByID(itemID1);
		double pred1 = recByCF(userID, item);
		double pred2 = recByKey(userID, item);
		System.out.println(pred1);
		System.out.println(pred2);
		
	}
	
	public static void init(){
		getItemListInLog();
		getItemsetNotIn();
	}
	
	public static Map<Integer,Double> recListByKey(int userID, List<Integer> itemList){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get items in the train set
		LinkedList<PostNode> trainItems = UserDAO.getRatedItemByID(userID, MINTIME, SEPTIME);
		Iterator<Integer> iterator1 = itemList.iterator();
		while(iterator1.hasNext()){
			int itemID1 = iterator1.next();
			Item item1 = ItemDAO.getItemByID(itemID1);
			Iterator<PostNode> iterator2 = trainItems.iterator();
			while(iterator2.hasNext()){
				PostNode node = iterator2.next();
				int itemID2 = node.getKey();
				Item item2 = ItemDAO.getItemByID(itemID2);
				double sim = ItemSimCalculator.getKeySim(item1, item2);//get key similarity
				if(rateMap.containsKey(itemID1)){
					double value = rateMap.get(itemID1) + node.getWeight() * sim;
					double norm = normMap.get(itemID1) + sim;
					rateMap.put(itemID1, value);
					normMap.put(itemID1, norm);
				}
				else{
					rateMap.put(itemID1, node.getWeight() * sim);
					normMap.put(itemID1, sim);
				}
			}
		}
		//normalize
		Set<Integer> keyset = rateMap.keySet();
		Iterator<Integer> keyIterator = keyset.iterator();
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			rateMap.put(key, rateMap.get(key) / normMap.get(key));
		}
		return rateMap;
	}
	
	public static Map<Integer,Double> recListByCF(int userID, List<Integer> itemList){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get items in the train set
		LinkedList<PostNode> trainItems = UserDAO.getRatedItemByID(userID, MINTIME, SEPTIME);
		Iterator<Integer> iterator1 = itemList.iterator();
		while (iterator1.hasNext()) {
			int itemID1 = iterator1.next();
			Set<Integer> itemnotSet = getItemsetNotIn();
			// item1 not in the rec log
			if (itemnotSet.contains(itemID1)) {
				Item item1 = ItemDAO.getItemByID(itemID1);
				double value = recByCF(userID, item1);
				rateMap.put(itemID1, value);
				normMap.put(itemID1, 1.0);
			} else {
				Iterator<PostNode> iterator2 = trainItems.iterator();
				while (iterator2.hasNext()) {
					PostNode node = iterator2.next();
					int itemID2 = node.getKey();
					double sim = ItemSimCalculator.getCFSim(itemID1, itemID2);
					if (rateMap.containsKey(itemID1)) {
						double value = rateMap.get(itemID1) + node.getWeight()
								* sim;
						double norm = normMap.get(itemID1) + sim;
						rateMap.put(itemID1, value);
						normMap.put(itemID1, norm);
					} else {
						rateMap.put(itemID1, node.getWeight() * sim);
						normMap.put(itemID1, sim);
					}
				}
			}
		}
		//normalize and sort
		Set<Integer> keyset = rateMap.keySet();
		Iterator<Integer> keyIterator = keyset.iterator();
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			rateMap.put(key, rateMap.get(key) / normMap.get(key));
		}
		return rateMap;
	}
	
	private static List<Item> getItemListInLog() {
		if(itemsInLog == null)
			itemsInLog = ItemDAO.getItemsInLog();
		return itemsInLog;
	}
	
	private static Set<Integer> getItemsetNotIn() {
		if(itemsetNotIn == null)
			itemsetNotIn = ItemDAO.getItemsetNotInLog();
		return itemsetNotIn;
	}
	
	public static double recByCF(int userID, Item item) {
		Set<Integer> itemsNotIn = getItemsetNotIn();
		if (itemsNotIn.contains(item.getId())) {
			Item neighbor = getNeighborByCategory(item);
			return recByCF(userID, neighbor);
		} else {
			LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(
					userID, MINTIME, SEPTIME);
			Iterator<PostNode> itemIterator = itemList.iterator();
			double rating = 0;
			double norm = 0;
			while (itemIterator.hasNext()) {
				PostNode ratedItem = itemIterator.next();
				double sim = ItemSimCalculator.getCFSim(ratedItem.getKey(),item.getId());
				rating += sim * ratedItem.getWeight();
				norm += sim;
			}
			if (norm != 0)
				return rating / norm;
			else
				return 0;
		}
	}
	
	public static double recByKey(int userID, Item item){
		LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(userID, MINTIME, SEPTIME);
		Iterator<PostNode> itemIterator = itemList.iterator();
		double rating = 0;
		double norm = 0;
		while(itemIterator.hasNext()){
			PostNode node = itemIterator.next();
			Item ratedItem = ItemDAO.getItemByID(node.getKey()); 
			double sim = ItemSimCalculator.getKeySim(item, ratedItem);
			rating += sim * node.getWeight();
			norm += sim;
			//System.out.println("key sim:<" + node.getKey() + ","+item.getId()+","+ sim +">" );
		}
		if (norm != 0)
			return rating / norm;
		else
			return 0;
	}

	
	private static Item getNeighborByCategory(Item item) {
		List<Item> itemsInLog = getItemListInLog();
		Iterator<Item> itemIterator = itemsInLog.iterator();
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
