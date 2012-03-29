package test;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.Random;

import javax.swing.JFrame;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.trees.J48;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

public class WekaDemo {

	public static void main(String[] args) throws Exception {
		write();
	}

	public static void readAndTest() {
		DataSource source;
		try {
			source = new DataSource(
					"/home/sjtu123/weka-3-6-6/data/weather.arff");
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
			tree.buildClassifier(data);

			Evaluation evaluation = new Evaluation(data);
			evaluation.crossValidateModel(tree, data, 10, new Random(1));
			System.out.println(evaluation.toSummaryString(true));

			ThresholdCurve tc = new ThresholdCurve();
			int classIndex = data.numAttributes()-1;
			Instances curve = tc.getCurve(evaluation.predictions(), classIndex);
			PlotData2D plotdata = new PlotData2D(curve);
			plotdata.setPlotName(curve.relationName());
			plotdata.addInstanceNumberAttribute();
			
			ThresholdVisualizePanel tvp = new ThresholdVisualizePanel();
			tvp.setROCString("(Area under ROC = " +
			Utils.doubleToString(ThresholdCurve.getROCArea(curve),4)+")");
			tvp.setName(curve.relationName());
			tvp.addPlot(plotdata);
			
			final JFrame jf = new JFrame("WEKA ROC: " + tvp.getName());
			jf.setSize(500,400);
			jf.getContentPane().setLayout(new BorderLayout());
			jf.getContentPane().add(tvp, BorderLayout.CENTER);
			jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			jf.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void write() throws Exception {
		Attribute numeric = new Attribute("attr1");
		Attribute date = new Attribute("attr2", "yyyy-MM-dd");
		FastVector labels = new FastVector();
		labels.addElement("label_a");
		labels.addElement("label_b");
		labels.addElement("label_c");
		labels.addElement("label_d");
		Attribute nominal = new Attribute("attr3", labels);
		Attribute string = new Attribute("attr4", (FastVector) null);

		FastVector atts = new FastVector();
		atts.addElement(numeric);
		atts.addElement(date);
		atts.addElement(nominal);
		atts.addElement(string);
		Instances dataset = new Instances("rel", atts, 0);
		// Attribute relational = new Attribute("name_of_attr", rel_struct);

		double[] values = new double[dataset.numAttributes()];
		values[0] = 1;
		values[1] = dataset.attribute(1).parseDate("2001-11-09");
		values[2] = dataset.attribute(2).indexOfValue("label_a");
		values[3] = dataset.attribute(3).addStringValue("attribute 3");
		Instance instance = new Instance(1, values);
		System.out.println(instance.toString());
		dataset.add(instance);
		DataSink.write("/home/sjtu123/demo.arff", dataset);
		System.out.println(dataset);
	}
	
	public static void write1() throws Exception {
		Attribute numeric = new Attribute("attr1");
		Attribute date = new Attribute("attr2", "yyyy-MM-dd");
		FastVector labels = new FastVector();
		labels.addElement("label_a");
		labels.addElement("label_b");
		labels.addElement("label_c");
		labels.addElement("label_d");
		Attribute nominal = new Attribute("attr3", labels);
		Attribute string = new Attribute("attr4", (FastVector) null);

		FastVector atts = new FastVector();
		atts.addElement(numeric);
		atts.addElement(date);
		atts.addElement(nominal);
		atts.addElement(string);
		Instances dataset = new Instances("rel", atts, 0);
		// Attribute relational = new Attribute("name_of_attr", rel_struct);

		double[] values = new double[dataset.numAttributes()];
		values[0] = 1;
		values[1] = dataset.attribute(1).parseDate("2001-11-09");
		values[2] = dataset.attribute(2).indexOfValue("label_a");
		values[3] = dataset.attribute(3).addStringValue("attribute 3");
		Instance instance = new Instance(1, values);
		System.out.println(instance.toString());
		DataSink.write("/home/sjtu123/demo1.arff", dataset);
		//
		FileWriter fr=  new FileWriter(new File("/home/sjtu123/demo1.arff"),true);
		BufferedWriter writer = new BufferedWriter(fr);
		writer.write(2 + ",2001-11-09" + ",label_b" + ",'attribute'\n");
		writer.write(2 + ",2001-11-09" + ",label_b" + ",'attribute'\n");
		writer.close();
		fr.close();
	}
	

}
