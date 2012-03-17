package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import data.ItemDAO;
import data.RecLogDAO;

public class Builder {
	
	public static void main(String[] args){
		calItemSimByKeyWord();
	}
	
	private static void calItemSimByKeyWord(){

	} 
	
	private static void buildRecLogTable(){
		System.out.println("begins-----------------------");
		String pathname = "/home/sjtu123/data/track1/rec_log_train.txt";
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int userID, itemID, result;
			while((line = breader.readLine()) != null){
				temp = line.split(delimiter);
				userID = Integer.parseInt(temp[0]);
				itemID = Integer.parseInt(temp[1]);
				result = Integer.parseInt(temp[2]);
				RecLogDAO.insertRecLog(userID, itemID, result, temp[3]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("ends-----------------------");
	}
	
	private static void buildUserTable(){
		String pathname = "/home/sjtu123/data/track1/user_action.txt";
		UserBuildTask actionTask = new UserBuildTask(pathname, "action");
		Thread actionThread = new Thread(actionTask);
		pathname = "/home/sjtu123/data/track1/user_sns.txt";
		UserBuildTask snsTask = new UserBuildTask(pathname, "sns");
		Thread snsThread = new Thread(snsTask);
		pathname = "/home/sjtu123/data/track1/user_key_word.txt";
		UserBuildTask keyWordTask = new UserBuildTask(pathname, "keyword");
		Thread keyWordThread = new Thread(keyWordTask);
		actionThread.start();
		snsThread.start();
		keyWordThread.start();
	}

}
