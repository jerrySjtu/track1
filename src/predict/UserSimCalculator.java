package predict;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import data.InvertedIndex;

public class UserSimCalculator {
	
	
	public static double calKeySim(int userID1, int userID2, InvertedIndex userKeyIndex) {
		Map<Integer, Double> map1 = userKeyIndex.docMap(String.valueOf(userID1));
		Map<Integer, Double> map2 = userKeyIndex.docMap(String.valueOf(userID2));
		Set<Integer> union = new HashSet<Integer>();
		union.addAll(map1.keySet());
		union.addAll(map2.keySet());
		Iterator<Integer> iterator = union.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (iterator.hasNext()) {
			int tag = iterator.next();
			if (map1.containsKey(tag))
				length1 += map1.get(tag) * map1.get(tag);
			if (map2.containsKey(tag))
				length2 += map2.get(tag) * map2.get(tag);
			if (map1.containsKey(tag) && map2.containsKey(tag))
				product += map1.get(tag) * map2.get(tag);
		}
		//some users have no tag
		if(length1 * length2 == 0)
			return 0;
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	// calculate the similarity of the two users by profile
	public static double calTagSim(int userID1, int userID2, InvertedIndex userTagIndex) {
		Map<Integer, Double> map1 = userTagIndex.docMap(String.valueOf(userID1));
		Map<Integer, Double> map2 = userTagIndex.docMap(String.valueOf(userID2));
		Set<Integer> union = new HashSet<Integer>();
		union.addAll(map1.keySet());
		union.addAll(map2.keySet());
		Iterator<Integer> iterator = union.iterator();
		double length1 = 0;
		double length2 = 0;
		double product = 0;
		while (iterator.hasNext()) {
			int tag = iterator.next();
			if (map1.containsKey(tag))
				length1 += map1.get(tag) * map1.get(tag);
			if (map2.containsKey(tag))
				length2 += map2.get(tag) * map2.get(tag);
			if (map1.containsKey(tag) && map2.containsKey(tag))
				product += map1.get(tag) * map2.get(tag);
		}
		//some users have no tag
		if(length1 * length2 == 0)
			return 0;
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

}
