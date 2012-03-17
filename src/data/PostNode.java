package data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PostNode implements Serializable{
	private int key;
	private double weight;
	//field next should not be serialized
	private transient PostNode next;
	
	public PostNode(){}
	
	public PostNode(int key, double weight){
		this.key = key;
		this.weight = weight;
		this.next = null;
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
