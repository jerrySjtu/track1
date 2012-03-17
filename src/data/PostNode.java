package data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PostNode implements Serializable{
	private int key;
	private double weight;
	private PostNode next;
	
	public PostNode(int key, double weight){
		this.key = key;
		this.weight = weight;
		this.next = null;
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

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public PostNode getNext() {
		return next;
	}

	public void setNext(PostNode next) {
		this.next = next;
	}

	@Override
	public String toString(){
		return key + ":" + weight;
	}
}
