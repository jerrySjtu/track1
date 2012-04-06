package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	public Set<Integer> getKeyset() {
		String[] keys = keyWords.split(";");
		Set<Integer> keyset = new HashSet<Integer>();
		for(int i = 0; i < keys.length; i++)
			keyset.add(Integer.parseInt(keys[i]));
		return keyset;
	}
	
	public Map<Integer,Integer> getKeyFreqMap() {
		String[] keys = keyWords.split(";");
		Map<Integer, Integer> freqMap = new HashMap<Integer, Integer>();
		for(int i = 0; i < keys.length; i++){
			int key = Integer.parseInt(keys[i]);
			if(freqMap.containsKey(key)){
				int value = freqMap.get(key) + 1;
				freqMap.put(key, value);
			}
			else
				freqMap.put(key, 1);
		}
		return freqMap;
	}
	
	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	
	@Override
	public String toString(){
		return "item: " + id + "," + category  + "," + keyWords;
	}

}
