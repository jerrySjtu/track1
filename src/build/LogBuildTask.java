package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import data.RecLogDAO;
import data.Record;

public class LogBuildTask {
	

	
	
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
	
}
