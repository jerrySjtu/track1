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
	
	public static void main(String[] args){
		InvertedIndex index = new InvertedIndex();
		PostNode node = new PostNode(2, 4);
		index.insertNode("a", node);
		node = new PostNode(2, 4);
		index.insertNode("a", node);
		node = new PostNode(3, 4);
		index.insertNode("a", node);
		node = new PostNode(1, 4);
		index.insertNode("a", node);
		node = new PostNode(2, 4);
		index.insertNode("a", node);
		//
		String pathName = "/home/sjtu123/data/track1/userTagIndex.ser";
		FileOutputStream fos = null; 
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pathName);
			out = new ObjectOutputStream(fos);
			out.writeObject(index);
			out.close();
			fos.close();
		} catch (IOException e) {
			LogManager.writeLogToFile(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("ends-------");
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(pathName);
			in = new ObjectInputStream(fis);
			InvertedIndex newIndex = (InvertedIndex) in.readObject();
			in.close();
			fis.close();
			System.out.println(newIndex.getIndexSize());
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	public int getIndexSize(){
		return index.size();
	}
	
	/**
	 * @param out
	 * implement my own version of writeObject
	 */
	public void writeObject(ObjectOutputStream out){
		try {
			out.defaultWriteObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param in
	 * implement my own version of readObject
	 */
	public void readObject(ObjectInputStream in) {
		try {
			in.defaultReadObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
