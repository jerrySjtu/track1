package data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PostList implements Serializable {
	private PostNode top;
	private int size;

	public PostList() {
		this.size = 0;
	}

	/**
	 * @param node
	 *            insert a node to the post list, all the node are arranged by
	 *            its key from the small to the big
	 */
	public void insert(PostNode node) {
		if (top == null)
			top = node;
		else {
			// the top is needed to be changed to avoid the dead loop
			if (top.getKey() >= node.getKey()) {
				node.setNext(top);
				top = node;
			} else {
				PostNode pre = top;
				PostNode post = pre;
				while (pre != null) {
					if (pre.getKey() < node.getKey()) {
						post = pre;
						pre = pre.getNext();
					} else
						break;
				}
				node.setNext(pre);
				post.setNext(node);
			}
		}
		size++;
	}
	
	/**
	 * @param out
	 * implement my own version of writeObject
	 */
	public void writeObject(ObjectOutputStream out){
		try {
			PostNode temp = top;
			while(temp != null){ 
				temp.writeObject(out);
				temp = temp.getNext();
			}
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
			PostNode temp = top;
			while(temp != null){
				temp.readObject(in);
				temp = temp.getNext();
			}
			try {
				in.defaultReadObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	}

	

	public PostNode getTop() {
		return top;
	}

	public void setTop(PostNode top) {
		this.top = top;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		PostNode temp = top;
		while (temp != null) {
			buffer.append(temp.toString() + "\n");
			temp = temp.getNext();
		}
		return buffer.toString();
	}

}
