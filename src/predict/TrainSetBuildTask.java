package predict;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import data.Item;
import data.ItemDAO;
import data.RecLogDAO;
import data.Record;
import data.SortArray;
import data.User;
import data.UserDAO;

public class TrainSetBuildTask implements Runnable {
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	// lock for the file
	private static ReentrantLock lock = new ReentrantLock();
	private static String pathname;
	private static int num = 1;
	private int userID;

	public TrainSetBuildTask(int userID) {
		this.userID = userID;
	}

	@Override
	public void run() {
		try {
			// get content
			String content = getDataOfUser();
			// lock the file
			lock.lock();
			// write to the file
			FileWriter fw = new FileWriter(new File(pathname), true);
			fw.write(content);
			fw.close();
			System.out.println(num + " th user is calculated...");
			num++;
			// unlock
			lock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDataOfUser() {
		// get mean rate of the user
		double mean = UserDAO.getRateMean(userID, MINTIME, MAXTIME);
		// get log in train set
		StringBuffer strBuffer = new StringBuffer();
		LinkedList<Record> logList = RecLogDAO.getTrainSetByUser(userID,SEPTIME+1, MAXTIME);
		Iterator<Record> logIterator = logList.iterator();
		while (logIterator.hasNext()) {
			Record record = logIterator.next();
			int itemID = record.getItemID();
			Item item = ItemDAO.getItemByID(itemID);
			strBuffer.append("'<" + String.valueOf(userID) + ","+ String.valueOf(itemID) + ">'");
			double x[] = new double[2];
			x[0] = ItemBasedPredictor.recByCF(userID, item);
			x[1] = ItemBasedPredictor.recByKey(itemID, item);
			for(int i = 0; i < x.length; i++){
				if(x[i] == 0)
					x[i] = mean;
				strBuffer.append("," + x[i]);
			}
			strBuffer.append(",'" + record.getResult() + "'\n");
		}
		return strBuffer.toString();
	}
	
	public static void setPathName(String path){
		pathname = path;
	}

}
