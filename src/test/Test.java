package test;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		date();
	}
	
	private static void date(){
		long timeStamp = 1318348785;
		Date time = new java.util.Date((long)timeStamp*1000);
		System.out.println(time);
	}
	
	private static void strEq(String str){
		System.out.println(str == "profile");
	}
	
	private static void rex(){
		Pattern pattern = Pattern.compile("(1|2)[0-9][0-9][0-9]");
		Matcher m = pattern.matcher("0988");
		boolean b = m.matches();	
		System.out.println(b);
	}

}
