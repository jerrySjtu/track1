package test;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import predict.TrainSetBuildTask;

import data.CategoryKey;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		while(true){
			int i = 99 * 99;
			i = i / 7;
		}
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
	
	public static int[] testArray(int size){
		int[] array = new int[size];
		for(int i = 0; i < size; i++)
			array[i] = i;
		return array;
	}
		
	public static void testHash(){
		Set<CategoryKey> set= new HashSet<CategoryKey>();
		HashMap<CategoryKey, Integer> map = new HashMap<CategoryKey, Integer>();
		set.add( new CategoryKey(2, 2, 2));
		map.put( new CategoryKey(2, 2, 2), 1);
		map.put( new CategoryKey(2, 2, 2), 2);
		map.put( new CategoryKey(2, 1, 2), 9);
		set.add( new CategoryKey(2, 4, 2));
		set.add( new CategoryKey(2, 2, 2));
		Set<CategoryKey> keySet = map.keySet();
		Iterator<CategoryKey> iterator = keySet.iterator();
		while(iterator.hasNext()){
			System.out.println(iterator.next());
		}
	}

}
