package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import build.LogManager;

public class InvertedIndex  implements Serializable {
	private HashMap<String, PostList> index;
	
	public InvertedIndex(){
		this.index = new HashMap<String, PostList>();
	}
	
	public int getIndexSize(){
		return index.size();
	}
	
	
	public PostList getPostListByKey(String key){
		return index.get(key);
	}
	
	public void insertNode(String key, PostNode node){
		//the term is not in the index
		if(index.containsKey(key) == false){
			PostList postList = new PostList();
			postList.insert(node);
			index.put(key, postList);
		}
		else{
			PostList postList = index.get(key);
			postList.insert(node);
		}
	}

}
