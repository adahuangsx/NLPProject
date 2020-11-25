package finalproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BowConverter {


	private Map<String, Integer> allWords = new HashMap<>();
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
			while (index < len && (Character.isLetter(text[index]) /* || text[index] == '\'' */)) {
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
	
	/**
	 * Input all the records I have, convert them into a dictionary.
	 * @param records
	 */
	public void inputAllwords(List<Record> records) {
		allWords = new HashMap<>();
		for (Record record : records) {
			List<String> words = this.tokenizeAndRemoveStopwords(record.toString().toCharArray());
			for (String word : words) {
				if (!allWords.containsKey(word)) {
					allWords.put(word, 1);
				}else {
					allWords.put(word, allWords.get(word) + 1);
				}
			}
		}
		// end
	}
	
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		StoryRecordReader rdr = new StoryRecordReader(StoryRecordReader.TRAIN_DATA);
		BowConverter t = new BowConverter();
		Record r = null;
		List<Record> records = new ArrayList<>();
		int i = 50;
		while ((r = rdr.next()) != null && i > 0) {
			records.add(r);
		}
		t.inputAllwords(records);
		System.out.println(t.allWords);
	}

}
