package assignment3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment {
	String text;
	List<String> words;
	Map<String, Integer> wordMap;
	boolean isCons; // is constructive
	int[] counts;
	
	public Comment (String text, List<String> words, boolean isCons) {
		this.text = text;
		this.words = words;
		this.isCons = isCons;
	}
	public void setCount(int[] c) {
		counts = c;
	}
	
	/**
	 * Convert the comment words into a [word - count] map.
	 * @return the count map
	 */
	public Map<String, Integer> toMap() {
		// TODO Auto-generated method stub
		if (wordMap != null) {
			return wordMap;
		}
		Map<String, Integer> count = new HashMap<>();
		for (String word : words) {
			if (!count.containsKey(word)) {
				count.put(word, 0);
			}
			count.put(word, count.get(word) + 1);
		}
		wordMap = count;
		return wordMap;
	}
}
