package predict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

import data.RecLogDAO;

public class HybridPredictor {
	//number of threads
	private static final int NTHREDS = 20;
	// the minimum time
	private static final long MINTIME = 1318348785;
	// the time separate the train set and test set
	private static final long SEPTIME = 1320537601;
	// the maximum time
	private static final long MAXTIME = 1321027199;
	
	public static void main(String[] args) throws Exception{
		buildTrainFile();
	}
	
	private static void loadData() throws Exception{
		String pathname = "/home/sjtu123/data/track1/train1.arff";
		DataSource source = new DataSource(pathname);
		Instances data = source.getDataSet();
		if (data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);
	}
	
	//build the train file with the arff extension
	private static void buildTrainFile() throws Exception{
		//thread pool
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		String pathname = "/home/sjtu123/data/track1/train1.arff";
		//write head
		writeHead(pathname);
		//get all users in train set
		System.out.println("begin to get all users in train set!");
		Set<Integer> userset = RecLogDAO.getTrainUser(SEPTIME, MAXTIME);
		System.out.println( " finished....");
		Iterator<Integer> userIterator = userset.iterator();
		int i = 1;
		while(userIterator.hasNext()){
			int userID = userIterator.next();
			Thread t = new Thread(new TrainSetBuildTask(userID, pathname));
			executor.execute(t);
			System.out.println(i + " th user calculated!!!");
			i++;
		}//end while
		executor.shutdown();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {}
		System.out.println("Finished all threads");
	}
	
	private static void writeHead(String pathname) throws Exception {
		// initiate the data file format
		Attribute key = new Attribute("key", (FastVector) null);
		Attribute itemCFRate = new Attribute("itemCFRate");
		Attribute itemKeyRate = new Attribute("itemKeyRate");
		Attribute userKeyRate = new Attribute("userKeyRate");
		Attribute userTagRate = new Attribute("userTagRate");
		Attribute result = new Attribute("result");
		FastVector atts = new FastVector();
		atts.addElement(key);
		atts.addElement(itemCFRate);
		atts.addElement(itemKeyRate);
		atts.addElement(userKeyRate);
		atts.addElement(userTagRate);
		atts.addElement(result);
		Instances dataset = new Instances("recommendation", atts, 14130272);
		DataSink.write(pathname, dataset);
	}
}
