package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import data.InvertedIndex;
import data.PostList;
import data.PostNode;

import build.LogManager;

public class TestSerialization {
	
	public static void main(String[] args){
		System.out.println("begins----------");
		writeObject();
		InvertedIndex index = readObject();
		PostList postList = index.getPostListByKey("key");
		System.out.println(postList.getSize());
		PostNode node = postList.getTop();
		System.out.println(node);
		System.out.println(node.getNext());
		System.out.println(node.getNext().getNext());
		System.out.println("ends-----------");
	}
	
	private static void writeObject(){
		String pathname = "/home/sjtu123/test.ser";
		InvertedIndex index = new InvertedIndex();
		for(int i = 0; i < 10; i++){
			PostNode node = new PostNode(i, i * 0.006);
			index.insertNode("key", node);
			System.out.println("insert a node :"+ i + "-----------");
		}
		FileOutputStream fos = null; 
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pathname);
			out = new ObjectOutputStream(fos);
			out.writeObject(index);
			out.close();
			fos.close();
		} catch (IOException e) {
			LogManager.writeLogToFile(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static InvertedIndex readObject(){
		String pathname = "/home/sjtu123/test.ser";
		FileInputStream fis = null;
		ObjectInputStream in = null;
		InvertedIndex index = null;
		try {
			fis = new FileInputStream(pathname);
			in = new ObjectInputStream(fis);
			index = (InvertedIndex) in.readObject();
			in.close();
			fis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return index;
	}
}
