package predict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import build.ItemBuildTask;

import data.CategoryKey;
import data.InvertedIndex;
import data.Item;
import data.ItemDAO;

public class ItemSimCalculator {
	private static Map<CategoryKey, Double> categoryMap;
	
	static{
		if(categoryMap == null)
			categoryMap = calCategoryProbability();
	}
	
	// calculate the CF similarity between two items
	public static double calCFSim(int itemID1, int itemID2, InvertedIndex itemUserIndex) {
		String key1 = String.valueOf(itemID1);
		String key2 = String.valueOf(itemID2);
		Map<Integer, Double> map1 = itemUserIndex.docMap(key1);
		Map<Integer, Double> map2 = itemUserIndex.docMap(key2);
		Set<Integer> docUnion = itemUserIndex.docUnion(key1, key2);
		Iterator<Integer> iterator = docUnion.iterator();
		double length1 = 0;
		double length2 = 0;
		double similarity = 0;
		while (iterator.hasNext()) {
			int userID = iterator.next();
			if (map1.containsKey(userID))
				length1++;
			if (map2.containsKey(userID))
				length2++;
			if (map1.containsKey(userID) && map2.containsKey(userID))
				similarity++;
		}
		//there are some users who have no record.
		return similarity / (Math.sqrt(length1) * Math.sqrt(length2));
	}
	
	/**
	 * 
	 * @param item1
	 * @param item2
	 * @return the similarity calculated by key words of the two items 
	 */
	public static double calKeySim(int itemID1, int itemID2, InvertedIndex itemKeyIndex) {
		String key1 = String.valueOf(itemID1);
		String key2 = String.valueOf(itemID2);
		Map<Integer, Double> map1 = itemKeyIndex.docMap(key1);
		Map<Integer, Double> map2 = itemKeyIndex.docMap(key2);
		Set<Integer> keySet = new HashSet<Integer>();
		keySet.addAll(map1.keySet());
		keySet.addAll(map2.keySet());
		Iterator<Integer> iterator = keySet.iterator();
		double product = 0.0;
		double length1 = 0.0;
		double length2 = 0.0;
		while (iterator.hasNext()) {
			int key = iterator.next();
			if (map1.containsKey(key))
				length1 += map1.get(key) * map1.get(key);
			if (map2.containsKey(key))
				length2 += map2.get(key) * map2.get(key);
			if (map1.containsKey(key) && map2.containsKey(key))
				product += map1.get(key) * map2.get(key);
		}
		return product / (Math.sqrt(length1) * Math.sqrt(length2));
	}

	
	/**
	 *
	 * @param item1
	 * @param item2
	 * @return information content value of the common parent of the two items
	 */
	public static double calInfoValue(Item item1, Item item2){
		int[] array1 = item1.getCategory();
		int[] array2 = item2.getCategory();
		int i;
		for(i = 0; i < 4; i++)
			if(array1[i] != array2[i])
				break;
		//different category in the root layer
		CategoryKey key;
		if(i == 0)
			return 0;
		if(i == 1)
			key = new CategoryKey(0, 0, array1[i-1]);
		else
			key = new CategoryKey(i-1, array1[i-2], array1[i-1]);
		return categoryMap.get(key);
	}

	// calculate the information content value of each category node
	public static Map<CategoryKey, Double> calCategoryProbability() {
		// build the category map
		Map<CategoryKey, Double> categoryMap = new HashMap<CategoryKey, Double>();
		LinkedList<Item> itemList = ItemDAO.readAllItems();
		Iterator<Item> iterator = itemList.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			int[] cateArray = item.getCategory();
			for (int i = 0; i < 4; i++) {
				// 0 is the root node
				CategoryKey key;
				if (i == 0)
					key = new CategoryKey(i, 0, cateArray[i]);
				else
					key = new CategoryKey(i, cateArray[i - 1], cateArray[i]);
				if (categoryMap.containsKey(key)) {
					double value = categoryMap.get(key) + 1;
					categoryMap.put(key, value);
				} else
					categoryMap.put(key, 1.0);
			}
		}
		// normalize: calculate the probability for each node
		Iterator<CategoryKey> keyIterator = categoryMap.keySet().iterator();
		while (keyIterator.hasNext()) {
			CategoryKey key = keyIterator.next();
			double value = categoryMap.get(key) / itemList.size();
			categoryMap.put(key, -Math.log(value));
		}
		return categoryMap;
	}

}
