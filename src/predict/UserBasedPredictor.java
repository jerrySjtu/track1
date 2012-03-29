package predict;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import data.PostNode;
import data.SortArray;
import data.UserDAO;

public class UserBasedPredictor {
	public static final int LISTSIZE = 100;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	
	
//	public static void main(String[] args){
//		int userID = 630535;
//		SortArray array = recListKeySim(userID);
//		System.out.println(array);
//	}
	
	/**
	 * 
	 * @param itemID
	 * @param userID
	 * @return predicted recommendation list based on tag similarity
	 */
	public static SortArray recListByTagSim(int userID){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		LinkedList<PostNode> neighborList = UserDAO.getNeighborByTag(userID);
		Iterator<PostNode> neighborIterator = neighborList.iterator();
		//calculate prediction
		while(neighborIterator.hasNext()){
			PostNode neighborNode = neighborIterator.next();
			//get the items rated by the neighbor
			LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(neighborNode.getKey(), MINTIME, SEPTIME);
			Iterator<PostNode> itemIterator = itemList.iterator();
			while(itemIterator.hasNext()){
				PostNode itemNode = itemIterator.next();
				if(recMap.containsKey(itemNode.getKey())){
					double value = recMap.get(itemNode.getKey()) + neighborNode.getWeight() * itemNode.getWeight();
					recMap.put(itemNode.getKey(), value);
					double norm = neighborNode.getWeight() + normMap.get(itemNode.getKey());
					normMap.put(itemNode.getKey(), norm);
				}
				else {
					recMap.put(itemNode.getKey(), neighborNode.getWeight()*itemNode.getWeight());
					normMap.put(itemNode.getKey(), neighborNode.getWeight());
				}
			}
		}
		//normalize and sort the items by prediction
		Set<Integer> keySet = recMap.keySet();
		Iterator<Integer> keyIterator = keySet.iterator();
		SortArray sortArray = new SortArray(LISTSIZE);
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			sortArray.insert(key, recMap.get(key) / normMap.get(key));
		}
		return sortArray;
	}
	
	/**
	 * 
	 * @param userID
	 * @return predicted recommendation list based on key similarity
	 */
	public static SortArray recListKeySim(int userID){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		LinkedList<PostNode> neighborList = UserDAO.getNeighborByKey(userID);
		Iterator<PostNode> neighborIterator = neighborList.iterator();
		//calculate prediction
		while(neighborIterator.hasNext()){
			PostNode neighborNode = neighborIterator.next();
			//get the items rated by the neighbor
			LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(neighborNode.getKey(), MINTIME, SEPTIME);
			Iterator<PostNode> itemIterator = itemList.iterator();
			while(itemIterator.hasNext()){
				PostNode itemNode = itemIterator.next();
				if(recMap.containsKey(itemNode.getKey())){
					double value = recMap.get(itemNode.getKey()) + neighborNode.getWeight() * itemNode.getWeight();
					recMap.put(itemNode.getKey(), value);
					double norm = neighborNode.getWeight() + normMap.get(itemNode.getKey());
					normMap.put(itemNode.getKey(), norm);
				}
				else {
					recMap.put(itemNode.getKey(), neighborNode.getWeight()*itemNode.getWeight());
					normMap.put(itemNode.getKey(), neighborNode.getWeight());
				}
			}
		}
		//normalize and sort the items by prediction
		Set<Integer> keySet = recMap.keySet();
		Iterator<Integer> keyIterator = keySet.iterator();
		SortArray sortArray = new SortArray(LISTSIZE);
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			sortArray.insert(key, recMap.get(key) / normMap.get(key));
		}
		return sortArray;
	}

}
