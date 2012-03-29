package predict;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import data.RecLogDAO;
import data.Record;
import data.SortArray;
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
		// get recommendation list by user id
		ArrayList<Map<Integer, Double>> mapArray = new ArrayList<Map<Integer, Double>>();
		SortArray recList1 = ItemBasedPredictor.recListByCF(userID);
		mapArray.add(recList1.convertToMap());
		SortArray recList2 = ItemBasedPredictor.recListByKey(userID);
		mapArray.add(recList2.convertToMap());
		SortArray recList3 = UserBasedPredictor.recListByTagSim(userID);
		mapArray.add(recList3.convertToMap());
		SortArray recList4 = UserBasedPredictor.recListKeySim(userID);
		mapArray.add(recList4.convertToMap());
		// get mean rate of the user
		double mean = UserDAO.getRateMean(userID, MINTIME, MAXTIME);
		// get log in train set
		StringBuffer strBuffer = new StringBuffer();
		LinkedList<Record> logList = RecLogDAO.getTrainSetByUser(userID,SEPTIME, MAXTIME);
		Iterator<Record> logIterator = logList.iterator();
		while (logIterator.hasNext()) {
			Record record = logIterator.next();
			int itemID = record.getItemID();
			strBuffer.append("<" + String.valueOf(userID) + ","
					+ String.valueOf(itemID) + ">");
			for (int i = 0; i < 4; i++) {
				Double rate = mapArray.get(i).get(itemID);
				if (rate == null)
					strBuffer.append("," + mean);
				else
					strBuffer.append("," + rate);
			}
			strBuffer.append("," + record.getResult() + "\n");
		}
		return strBuffer.toString();
	}

}
