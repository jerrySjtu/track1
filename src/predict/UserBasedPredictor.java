package predict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import build.LogBuildTask;
import build.UserBuildTask;

import data.InvertedIndex;
import data.PostList;
import data.PostNode;
import data.SortArray;
import data.SortEntry;
import data.User;
import data.UserDAO;

public class UserBasedPredictor {
	private static final int STOPKEYLIMIT = 5000;
	private static final int STOPTAGLIMIT = 5000;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	private static InvertedIndex tagUserIndex;
	private static InvertedIndex keyUserIndex;
	
	static{
		
//		if(userKeyIndex == null){
//			userKeyIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/userKeyIndex.ser");
//			System.out.println("keyUserindex");
//		}
		
//		if(userTagIndex == null)
//			userTagIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/userTagIndex.ser");
	}
	
	public static void main(String[] args){
		System.out.println("------------");
		int userID = 1619517;
		System.out.println(recListByTagSim(userID));
		System.out.println(recListKeySim(userID));
	}
	
	
	/**
	 * 
	 * @param itemID
	 * @param userID
	 * @return predicted recommendation list based on tag similarity
	 */
	public static Map<Integer,Double> recListByTagSim(int userID){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		User user = UserDAO.getUserProfileByID(userID);
		SortArray neighborArray = getNeighborsByTag(user);
		SortEntry node = neighborArray.getTop();
		//calculate prediction 
		while(node != null){
			int neighborID = node.getKey();
			double similarity = node.getValue();
			//get the items rated by the neighbor
			LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(neighborID, MINTIME, SEPTIME);
			Iterator<PostNode> itemIterator = itemList.iterator();
			while(itemIterator.hasNext()){
				PostNode itemNode = itemIterator.next();
				if(recMap.containsKey(itemNode.getKey())){
					double value = recMap.get(itemNode.getKey()) + similarity * itemNode.getWeight();
					recMap.put(itemNode.getKey(), value);
					double norm = similarity + normMap.get(itemNode.getKey());
					normMap.put(itemNode.getKey(), norm);
				}
				else {
					recMap.put(itemNode.getKey(), similarity * itemNode.getWeight());
					normMap.put(itemNode.getKey(), similarity);
				}
			}
			node = node.getNext();
		}
		//normalize and sort the items by prediction
		Set<Integer> keySet = recMap.keySet();
		Iterator<Integer> keyIterator = keySet.iterator();
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			recMap.put(key, recMap.get(key) / normMap.get(key));
		}
		return recMap;
	}
	
	/**
	 * 
	 * @param userID
	 * @return predicted recommendation list based on key similarity
	 */
	public static Map<Integer,Double> recListKeySim(int userID){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		User user = UserDAO.getUserKeyByID(userID);
		SortArray sortArray = getNeighborsByKeyword(user);
		SortEntry node = sortArray.getTop();
		//calculate prediction
		while(node != null){
			int neighborID = node.getKey();
			double similarity = node.getValue();
			//get the items rated by the neighbor
			LinkedList<PostNode> itemList = UserDAO.getRatedItemByID(neighborID, MINTIME, SEPTIME);
			Iterator<PostNode> itemIterator = itemList.iterator();
			while(itemIterator.hasNext()){
				PostNode itemNode = itemIterator.next();
				if(recMap.containsKey(itemNode.getKey())){
					double value = recMap.get(itemNode.getKey()) + similarity * itemNode.getWeight();
					recMap.put(itemNode.getKey(), value);
					double norm = similarity + normMap.get(itemNode.getKey());
					normMap.put(itemNode.getKey(), norm);
				}
				else {
					recMap.put(itemNode.getKey(), similarity * itemNode.getWeight());
					normMap.put(itemNode.getKey(), similarity);
				}
			}
			node = node.getNext();
		}
		//normalize and sort the items by prediction
		Set<Integer> keySet = recMap.keySet();
		Iterator<Integer> keyIterator = keySet.iterator();
		while(keyIterator.hasNext()){
			int key = keyIterator.next();
			recMap.put(key, recMap.get(key) / normMap.get(key));
		}
		return recMap;
	}

	
	
	// get all the users who have some common item
	private static SortArray getNeighborsByKeyword(User user) {
		InvertedIndex keyUserIndex = getKeyUserIndex();
		LinkedList<PostNode> keywords = user.getKeyWordList();
		Iterator<PostNode> keyIterator = keywords.iterator();
		Set<Integer> neighborSet = new HashSet<Integer>();
		while (keyIterator.hasNext()) {
			String keyword = String.valueOf(keyIterator.next().getKey());
			// not a stop key word
			PostList postList = keyUserIndex.getPostListByKey(keyword);
			if (postList != null && postList.getSize() < STOPKEYLIMIT) {
				PostNode node = postList.getTop();
				while (node != null) {
					neighborSet.add(node.getKey());
					node = node.getNext();
				}
			}
		}
		//sort
		SortArray sortArray = new SortArray(30);
		Iterator<Integer> iterator = neighborSet.iterator();
		while(iterator.hasNext()){
			int neighborID = iterator.next();
			double sim = UserSimCalculator.getKeySim(user.getId(), neighborID);
			sortArray.insert(neighborID, sim);
		}
		return sortArray;
	}
	
	// get all the users who have some common tags
	private static SortArray getNeighborsByTag(User user) {
		InvertedIndex tagUserIndex = getTagUserIndex();
		String[] tags = user.getTags();
		Set<Integer> neighborSet = new HashSet<Integer>();
		for (int i = 0; i < tags.length; i++) {
			PostList postList = tagUserIndex.getPostListByKey(tags[i]);
			if (postList != null && postList.getSize() < STOPTAGLIMIT) {
				PostNode temp = postList.getTop();
				while (temp != null) {
					neighborSet.add(temp.getKey());
					temp = temp.getNext();
				}
			}
		}
		//sort
		SortArray sortArray = new SortArray(30);
		Iterator<Integer> iterator = neighborSet.iterator();
		while (iterator.hasNext()) {
			int neighborID = iterator.next();
			double sim = UserSimCalculator.getTagSim(user.getId(), neighborID);
			sortArray.insert(neighborID, sim);
		}
		return sortArray;
	}
	
	private static InvertedIndex getKeyUserIndex(){
		if(keyUserIndex == null){
			keyUserIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/keyUserIndex.ser");
			System.out.println("keyUserindex");
		}
		return keyUserIndex;
	}
	
	private static InvertedIndex getTagUserIndex(){
		if(tagUserIndex == null)
			tagUserIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/tagUserIndex.ser");
		return tagUserIndex;
	}

}
