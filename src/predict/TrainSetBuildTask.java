package predict;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
	private int userID;
	private String pathname;

	public TrainSetBuildTask(int userID, String pathname) {
		this.userID = userID;
		this.pathname = pathname;
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
			// unlock
			lock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDataOfUser() {
		// get mean rate of the user
		double mean = UserDAO.getRateMean(userID, MINTIME, MAXTIME);
		User userWithKey = UserDAO.getUserProfileByID(userID);
		// get log in train set
		StringBuffer strBuffer = new StringBuffer();
		LinkedList<Record> logList = RecLogDAO.getTrainSetByUser(userID,SEPTIME, MAXTIME);
		Iterator<Record> logIterator = logList.iterator();
		while (logIterator.hasNext()) {
			Record record = logIterator.next();
			int itemID = record.getItemID();
			Item item = ItemDAO.getItemByID(itemID);
			strBuffer.append("'<" + String.valueOf(userID) + ","+ String.valueOf(itemID) + ">'");
			double x[] = new double[5];
			x[0] = ItemBasedPredictor.predictByCategory(userWithKey, item);
			x[1] = ItemBasedPredictor.predictByCF(userWithKey, item);
			x[2] = ItemBasedPredictor.predictByKey(userWithKey, item);
			x[3] = UserBasedPredictor.predictByKeySim(userID, itemID);
			x[4] = UserBasedPredictor.predictByTagSim(userID, itemID);
			for(int i = 0; i < x.length; i++){
				if(x[i] == 0)
					x[i] = mean;
				strBuffer.append("," + x[i]);
			}
			strBuffer.append("," + record.getResult() + "\n");
		}
		return strBuffer.toString();
	}

}
