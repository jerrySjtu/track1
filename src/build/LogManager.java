package build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogManager {
	private static String PATH = "/home/sjtu123/data/track1/log";
	
//	public static void main(String[] args){
//		writeLogToFile("hello");
//	}
	
	public static void writeLogToFile(String log) {
		try {
			FileWriter fw = new FileWriter(new File(PATH));
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(log);
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
