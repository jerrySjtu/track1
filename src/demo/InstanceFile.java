package demo;

import weka.core.Instances;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InstanceFile {

	public static void main(String[] args) {

	}

	public void read(String file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(file)));
			Instances data = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
