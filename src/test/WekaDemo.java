package test;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

public class WekaDemo {
	
	public static void main(String[] args){
		test();
	}

	public static void test() {
		DataSource source;
		try {
			source = new DataSource("/home/sjtu123/weka-3-6-6/data/weather.arff");
			Instances data = source.getDataSet();
			// setting class attribute if the data format does not provide this
			// information
			// For example, the XRFF format saves the class attribute
			// information as well
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
			String[] options = new String[1];
			options[0] = "-U"; // unpruned tree
			J48 tree = new J48(); // new instance of tree
			tree.setOptions(options); // set the options
			tree.buildClassifier(data); // build classifier
			
			 Evaluation eval = new Evaluation(data);
			 eval.crossValidateModel(tree, data, 10, new Random(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
