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
	public int[] getCategory() {
		int[] intArray = new int[4];
		String[] strArray = category.split("\\.");
		for(int i = 0; i < strArray.length; i++)
			intArray[i] = Integer.parseInt(strArray[i]);
		return intArray;
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
