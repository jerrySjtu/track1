package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.UserDAO;

public class UserBuildTask implements Runnable {
	private String action;
	private String pathname;
	
	public UserBuildTask(String pathname, String action){
		this.action = action;
		this.pathname = pathname;
	}

	@Override
	public void run() {
		System.out.println("Task " + this.action + " start---------------");
		if(action.equals("profile"))
			buildUserProfileTable(pathname);
		else if(action.equals("action"))
			buildUserActionTable(pathname);
		else if(action.equals("keyword"))
			buildUserKeyWordTable(pathname);
		else if(action.equals("sns"))
				buildUserSNSTable(pathname);
		System.out.println("Task " + this.action + " end---------------");
	}
	
	private void buildUserSNSTable(String pathname){
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id, dID;
			while((line = breader.readLine()) != null){
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				dID = Integer.parseInt(temp[1]);
				UserDAO.insertUserSNS(id, dID);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void buildUserKeyWordTable(String pathname){
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id;
			while((line = breader.readLine()) != null){
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				if(temp[1].length() > 512)
					temp[1] = temp[1].substring(0, 512);
				UserDAO.insertUserKeyWord(id, temp[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void buildUserProfileTable(String pathname){
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			Matcher m;
			int id, birth, gender, tweetNum;
			Pattern pattern = Pattern.compile("(1|2)[0-9][0-9][0-9]");
			while((line = breader.readLine()) != null){
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				m = pattern.matcher(temp[1]);
				if(m.matches())
					birth = Integer.parseInt(temp[1]);
				else
					birth = 1988;
				gender = Integer.parseInt(temp[2]);
				tweetNum = Integer.parseInt(temp[3]);
				if(tweetNum > 32767)
					tweetNum = 32767;
				UserDAO.insertUserProfile(id, birth, gender, tweetNum, temp[4]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void buildUserActionTable(String pathname){
		try {
			FileReader freader = new FileReader(new File(pathname));
			BufferedReader breader = new BufferedReader(freader);
			String line;
			String delimiter = "\t";
			String[] temp;
			int id, dID, atNum, retweetNum, commentNum;
			while((line = breader.readLine()) != null){
				temp = line.split(delimiter);
				id = Integer.parseInt(temp[0]);
				dID = Integer.parseInt(temp[1]);
				atNum = Integer.parseInt(temp[2]);
				retweetNum = Integer.parseInt(temp[3]);
				commentNum = Integer.parseInt(temp[4]);
				UserDAO.insertUserAction(id, dID, atNum, retweetNum, commentNum);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
