package data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

public class User {
	private int id;
	private String tags;
	private int tweetNum;
	private String keyWords;
	
	public User(int id, int tweetNum, String tags, String keyWords){
		this.id = id;
		this.tags = tags;
		this.keyWords = keyWords;
		this.tweetNum = tweetNum;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String[] getTags() {
		String regex = ";";
		return tags.split(regex);
	}
	
	public Set<Integer> getTagset() {
		Set<Integer> tagset = new HashSet<Integer>(30);
		String[] array = tags.split(";");
		for(int i = 0; i < array.length; i++)
			tagset.add(Integer.parseInt(array[i]));
		return tagset;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String[] getKeyWords() {
		String delimeter = ";";
		return keyWords.split(delimeter);
	}
	
	public LinkedList<PostNode> getKeyWordList() {
		String[] array = keyWords.split(";");
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		for (int i = 0; i < array.length; i++) {
			String[] temp = array[i].split(":");
			// key information may be incomplete
			if (temp.length == 2) {
				int key = Integer.parseInt(temp[0]);
				double weight = Double.parseDouble(temp[1]);
				PostNode node = new PostNode(key, weight);
				list.add(node);
			}
		}
		return list;
	}
	
	public static PostNode getKeyWeight(int key, List<PostNode> list){
		Iterator<PostNode> iterator = list.iterator();
		while(iterator.hasNext()){
			PostNode node = iterator.next();
			if(node.getKey() == key)
				return node;
		}
		return null;
	}
	
	public static Set<Integer> keyUnion(List<PostNode> list1, List<PostNode> list2){
		Set<Integer> keySet = new HashSet<Integer>();
		Iterator<PostNode> iterator = list1.iterator();
		while(iterator.hasNext()){
			PostNode node = iterator.next();
			keySet.add(node.getKey());
		}
		iterator = list2.iterator();
		while (iterator.hasNext()) {
			PostNode node = iterator.next();
			keySet.add(node.getKey());
		}
		return keySet;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	public int getTweetNum() {
		return tweetNum;
	}

	public void setTweetNum(int tweetNum) {
		this.tweetNum = tweetNum;
	}

	@Override
	public String toString(){
		return id + " : \n" + tags + "\n" + keyWords;
	}

}
