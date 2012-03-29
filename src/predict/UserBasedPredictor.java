package predict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import build.UserBuildTask;

import data.InvertedIndex;
import data.PostList;
import data.PostNode;
import data.SortArray;
import data.User;
import data.UserDAO;

public class UserBasedPredictor {
	private static final int STOPKEYLIMIT = 10000;
	private static final int STOPTAGLIMIT = 10000;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	private static InvertedIndex tagUserIndex;
	private static InvertedIndex userTagIndex;
	private static InvertedIndex keyUserIndex;
	private static InvertedIndex userKeyIndex;
	
	static{
		if(tagUserIndex == null)
			tagUserIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/tagUserIndex.ser");
		if(userTagIndex == null)
			userTagIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/userTagIndex.ser");
		if(keyUserIndex == null)
			keyUserIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/keyUserIndex.ser");
		if(userKeyIndex == null)
			userKeyIndex = UserBuildTask.loadIndex("/home/sjtu123/data/track1/userKeyIndex.ser");
	}
	
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
	public static Map<Integer,Double> recListByTagSim(User user){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		Set<Integer> neighborset = getNeighborByTag(user);
		Iterator<Integer> neighborIterator = neighborset.iterator();
		//calculate prediction 
		while(neighborIterator.hasNext()){
			int neighborID = neighborIterator.next();
			double similarity = UserSimCalculator.calTagSim(user.getId(), neighborID, userTagIndex);
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
	public static Map<Integer,Double> recListKeySim(User user){
		Map<Integer, Double> recMap = new HashMap<Integer, Double>();
		Map<Integer, Double> normMap = new HashMap<Integer, Double>();
		//get neighbors with tag similarity
		Set<Integer> neighbors = getNeighborByKeyword(user);
		Iterator<Integer> neighborIterator = neighbors.iterator();
		//calculate prediction
		while(neighborIterator.hasNext()){
			int neighborID = neighborIterator.next();
			double similarity = UserSimCalculator.calKeySim(user.getId(), neighborID, userKeyIndex);
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
	private static Set<Integer> getNeighborByKeyword(User user) {
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
		return neighborSet;
	}
	
	// get all the users who have some common tags
	private static Set<Integer> getNeighborByTag(User user) {
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
		return neighborSet;
	}

}
