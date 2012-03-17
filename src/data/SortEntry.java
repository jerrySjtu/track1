package data;

public class SortEntry {
	private int key;
	private double value;
	private SortEntry next;
	
	public SortEntry(int key, double value){
		this.key = key;
		this.value = value;
		this.next = null;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public SortEntry getNext() {
		return next;
	}

	public void setNext(SortEntry next) {
		this.next = next;
	}

	@Override
	public String toString(){
		return value + " : " + key;
	}
	
}
