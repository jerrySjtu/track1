package predict;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import build.UserBuildTask;

import data.InvertedIndex;
import data.PostNode;
import data.User;
import data.UserDAO;

public class UserSimCalculator {
	
	public static void main(String[] args){
		int userID1 = 105093;
		int userID2 = 983457;
		System.out.println(getKeySim(userID1, userID2));
		System.out.println(getTagSim(userID1, userID2));
	}
	
	public static double getKeySim(int userID1, int userID2) {
		User user1 = UserDAO.getUserKeyByID(userID1);
		User user2 = UserDAO.getUserKeyByID(userID2);
		LinkedList<PostNode> list1 = user1.getKeyWordList();
		LinkedList<PostNode> list2 = user2.getKeyWordList();
		Set<Integer> union = User.keyUnion(list1, list2);
		Iterator<Integer> iterator = union.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (iterator.hasNext()) {
			int tag = iterator.next();
			PostNode node1 = User.getKeyWeight(tag, list1);
			PostNode node2 = User.getKeyWeight(tag, list2);
			if (node1 != null)
				length1 += node1.getWeight() * node1.getWeight();
			if (node2  != null)
				length2 += node2.getWeight() * node2.getWeight();
			if (node1 != null && node2 != null)
				product += node1.getWeight() * node2.getWeight();
		}
		//some users have no tag
		if(length1 * length2 == 0)
			return 0;
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	// calculate the similarity of the two users by profile
	public static double getTagSim(int userID1, int userID2) {
		User user1 = UserDAO.getUserProfileByID(userID1);
		User user2 = UserDAO.getUserProfileByID(userID2);
		Set<Integer> tagset1 = user1.getTagset();
		Set<Integer> tagset2 = user2.getTagset();
		Set<Integer> union = new HashSet<Integer>();
		union.addAll(tagset1);
		union.addAll(tagset2);
		Iterator<Integer> iterator = union.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (iterator.hasNext()) {
			int tag = iterator.next();
			if (tagset1.contains(tag))
				length1++;
			if (tagset2.contains(tag))
				length2++;
			if (tagset1.contains(tag) && tagset2.contains(tag))
				product++;
		}
		//some users have no tag
		if(length1 * length2 == 0)
			return 0;
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

}
