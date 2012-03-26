package predict;

import java.util.LinkedList;

import data.PostNode;
import data.SortArray;

public class ItemFilter {

	// remove the item rated by the user from the recommender list
	public LinkedList<PostNode> ratedFilter(int userID, long minTime,
			long maxTime, SortArray sortArray) {
		LinkedList<PostNode> list = new LinkedList<PostNode>();
		return list;
	}

}
