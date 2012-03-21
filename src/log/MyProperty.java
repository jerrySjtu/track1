package log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProperty {
	private static File configFile;
	private static Properties prop;
	
	static{
		if(configFile == null)
			configFile = new File("config.properties");
		if(prop == null)
			prop = new Properties();
	}
	
	private static void writeProperty(String key, String value, String comment){
		prop.setProperty(key, value);
		try {
			prop.store(new FileOutputStream(configFile), comment);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readProperty(String key){
		String value = null;
		try {
			prop.load(new FileInputStream(configFile)); 
			value = prop.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static void removeProperty(String key){
		prop.remove(key);
	}
	
}
