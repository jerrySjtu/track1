package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InvertedIndex implements Serializable {
	private HashMap<String, PostList> index;

	public InvertedIndex() {
		this.index = new HashMap<String, PostList>();
	}

	public int getIndexSize() {
		return index.size();
	}

	public Set<String> keySet() {
		return index.keySet();
	}

	// key array which can be visited randomly
	public ArrayList<String> keyArray() {
		ArrayList<String> keylist = new ArrayList<String>();
		Set<String> keyset = index.keySet();
		Iterator<String> iterator = keyset.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			keylist.add(key);
		}
		return keylist;
	}

	/**
	 * @param key1
	 * @param key2
	 * @return the documents' union of the two list indexed by key1 and key2
	 */
	public Set<Integer> docUnion(String key1, String key2) {
		Set<Integer> docUnion = new HashSet<Integer>();
		PostList postlist = index.get(key1);
		PostNode temp = postlist.getTop();
		while (temp != null) {
			docUnion.add(temp.getKey());
			temp = temp.getNext();
		}
		postlist = index.get(key2);
		temp = postlist.getTop();
		while (temp != null) {
			docUnion.add(temp.getKey());
			temp = temp.getNext();
		}
		return docUnion;
	}
	
	/**
	 * @param key
	 * @return convert the linked list to a map
	 */
	public Map<Integer, Double> docMap(String key) {
		PostList postList = index.get(key);
		PostNode temp = postList.getTop();
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		while(temp != null){
			map.put(temp.getKey(), temp.getWeight());
			temp = temp.getNext();
		}
		return map;
	}

	public PostList getPostListByKey(String key) {
		return index.get(key);
	}

	public void insertNode(String key, PostNode node) {
		// the term is not in the index
		if (index.containsKey(key) == false) {
			PostList postList = new PostList();
			postList.insert(node);
			index.put(key, postList);
		} else {
			PostList postList = index.get(key);
			postList.insert(node);
		}
	}
	
	public void removePostList(String key){
		index.remove(key);
	}

}
