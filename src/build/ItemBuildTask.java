package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import data.InvertedIndex;
import data.Item;
import data.ItemDAO;
import data.PostNode;

public class ItemBuildTask{
	// the minimum time
	private final static long MINTIME = 1318348785;
	// the time separate the train set and test set
	private final static long SEPTIME = 1320537601;
	// the maximum time

	/**
	 * build the inverted document index with the item id as the term and user
	 * id as the document
	 */
	private static InvertedIndex buildCFIndex() {
		// get all items
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		System.out.println(itemList.size());
		Iterator<Item> itemIterator = itemList.iterator();
		Item item;
		LinkedList<Integer> userList;
		InvertedIndex index = new InvertedIndex();
		int userID;
		int i = 1;
		while (itemIterator.hasNext()) {
			item = itemIterator.next();
			userList = ItemDAO.getUserByAcceptedItem(item.getId(), MINTIME, SEPTIME);
			Iterator<Integer> userIterator = userList.iterator();
			while (userIterator.hasNext()) {
				userID = userIterator.next();
				PostNode postNode = new PostNode(userID, 0);
				// insert the node
				index.insertNode(Integer.toString(item.getId()), postNode);
			}
			System.out.println("The index of " + i + " item is calculated.");
			i++;
		}
		return index;
	}

	/**
	 * @param index
	 * @param pathName
	 *            write the index to the file
	 */
	private static void writeIndex(InvertedIndex index, String pathName) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pathName);
			out = new ObjectOutputStream(fos);
			out.writeObject(index);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param pathName
	 * @return load the index from the file
	 */
	public static InvertedIndex loadIndex(String pathName) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		InvertedIndex index = null;
		try {
			fis = new FileInputStream(pathName);
			in = new ObjectInputStream(fis);
			index = (InvertedIndex) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return index;
	}

	private static void buildItemTable() {
		String pathname = "/home/sjtu123/data/track1/item.txt";
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id;
			while ((line = breader.readLine()) != null) {
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				ItemDAO.insertItem(id, temp[1], temp[2]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
