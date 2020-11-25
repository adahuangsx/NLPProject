package finalproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BowConverter {


	private Set<String> allWords = new HashSet<>();
	private StopWordRemover stopwordRemover = new StopWordRemover();
	
	/**
	 * tokenize the words and remove the stopwords, and output the words.
	 * @param text "I am an example."
	 * @return token words  List<> {"i", "am", ...}
	 */
	private List<String> tokenizeAndRemoveStopwords(char[] text) {
		List<String> res = new ArrayList<>();
		int len = text.length;
		int index = 0;
		WordNormalizer normalizerObj = new WordNormalizer();
		while (index < len) {
			while (index < len && !Character.isLetter(text[index])) {
				index++; // start
			}
			StringBuilder sb = new StringBuilder();
			while (index < text.length && (Character.isLetter(text[index]) || text[index] == '\'')) {
				sb.append(text[index++]);
			}
			if (sb.length() > 0) {
				String word = normalizerObj.stem(sb.toString().toLowerCase().toCharArray());
				if (!stopwordRemover.isStopword(word)) {
					res.add(word);
				}
			}
		}
		return res;
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
