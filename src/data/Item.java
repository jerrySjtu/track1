package data;

public class Item {
	private int id;
	private String category;
	private String keyWords;
	
	public Item(int id, String category, String keyWords){
		this.id = id;
		this.category = category;
		this.keyWords = keyWords;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String[] getKeyWords() {
		String delimeter = ";";
		return keyWords.split(delimeter);
	}
	
	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	@Override
	public String toString(){
		return "item: " + id + "," + category  + "," + keyWords;
	}

}
