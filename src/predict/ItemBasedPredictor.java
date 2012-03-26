package predict;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import data.ItemDAO;
import data.PostNode;
import data.SortArray;
import data.UserDAO;

public class ItemBasedPredictor {
	private static final int LISTLIMIT = 30;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;

	public static void main(String[] args) {
		int userID = 601635;
		int itemID = 1774594;
		SortArray array = recListByCF(userID);
		System.out.println(array);
	}
	
	public static SortArray recListByKey(int userID){
		Map<Integer,Double> rateMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get accepted items of the user
		LinkedList<PostNode> ratedItems = UserDAO.getRatedItemByID(userID, MINTIME, SEPTIME);
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


}
