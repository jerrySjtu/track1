package data;

import java.util.LinkedList;

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

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String[] getKeyWords() {
		String delimeter = ";";
		return keyWords.split(delimeter);
	}
	
	public LinkedList<String> getKeyWordWithoutWeight(){
		String[] array = keyWords.split(";");
		LinkedList<String> list = new LinkedList<String>();
		for(int i = 0; i < array.length; i++)
			list.add(array[i].split(":")[0]);
		return list;
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
