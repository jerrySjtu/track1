package test;

import data.SortArray;

public class TestSortArray {
	
	public static void main(String[] args){
		test2();
	}
	
	public static void test1(){
		SortArray array = new SortArray(2);
		array.insert(1,2);
		System.out.println(array);
	}
	
	public static void test2(){
		SortArray array = new SortArray(2);
		array.insert(1,3);
		array.insert(1,2);
		array.insert(1,-1);
		System.out.println(array);
	}
	

}
