package data;

public class Record {
	private int userID;
	private int itemID;
	private short result;
	private long timestamp;
	
	public Record(int userID, int itemID, short result, long timestamp){
		this.userID = userID;
		this.itemID = itemID;
		this.result = result;
		this.timestamp = timestamp;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public short getResult() {
		return result;
	}

	public void setResult(short result) {
		this.result = result;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString(){
		return "<" + userID +","+ itemID + ","+ result+ ","+ timestamp + ">";
	}

}
