package data;

import java.util.HashMap;
import java.util.Map;

public class SortArray {
	private int maxSize;
	private int currentSize;
	//the elements are arranged from small to big
	private SortEntry top;
	
	public SortArray(int maxSize) {
		this.maxSize = maxSize;
		this.currentSize = 0;
	}
	
	public int size(){
		return currentSize;
	}
	
	public Map<Integer, Double> convertToMap(){
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		SortEntry element = top;
		while(element != null){
			map.put(element.getKey(), element.getValue());
			element = element.getNext();
		}
		return map;
	}
	
	public SortEntry getTop(){
		return this.top;
	}
	
	public void insert(int key, double value){
		SortEntry entity = new SortEntry(key, value);
		if(currentSize == 0){
			top = entity;
			currentSize++;
		}
		else{
			if(value <= top.getValue())
				if(currentSize < maxSize){
					entity.setNext(top);
					top = entity;
					currentSize++;
				}
			if(value > top.getValue()){	
				SortEntry pre = top;
				SortEntry post = pre;
				while(pre != null){
					if(value < pre.getValue())
						break;
					else{
						post = pre;
						pre = pre.getNext();
					}
				}
				//add the entity to the list
				if(pre == null)
					post.setNext(entity);
				else{
					entity.setNext(post.getNext());
					post.setNext(entity);
				}
				currentSize++;
				//limit the amount of the list
				if(currentSize > maxSize){
					top = top.getNext();
					currentSize--;
				}
			}
		}
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		SortEntry element = top;
		while(element != null)
		{
			buffer.append(element.getKey() + ":" + element.getValue() + "\n");
			element = element.getNext();
		}
		return buffer.toString();
	}
	
}
