package data;

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
