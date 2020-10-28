package assignment3;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;

public class CommentReader {
	
	class Comment {
		String text;
		List<String> words;
		boolean isCons; // is constructive
		
		public Comment (String text, List<String> words, boolean isCons) {
			this.text = text;
			this.words = words;
			this.isCons = isCons;
		}
	}

	private static final String csvFile = "data//SFUcorpus.csv";
	public Map<String, Boolean> comments;
	
	private StopWordRemover stopwordRemover = new StopWordRemover();

	
	public CommentReader(String file) throws Exception {
		comments = new HashMap<>();
		
		CsvReader data = new CsvReader(file);
		data.readHeaders();
		while (data.readRecord()) {
			String comment = data.get("comment_text");
			List<String> words = tokenizeAndRemoveStopwords(comment.toCharArray());
			System.out.println(words);
			boolean isConstru = data.get("is_constructive").equals("yes") ? true : false;
			comments.put(comment, isConstru);
			System.out.println(comment);
			System.out.println(comments.get(comment));
		}
		
		System.out.println(comments.size());
	}
	
	
	private List<String> tokenizeAndRemoveStopwords(char[] text) {
		List<String> res = new ArrayList<>();
		int len = text.length;
		int index = 0;
		while (index < len) {
			while (index < len && !Character.isLetter(text[index])) {
				index++; // start
			}
			StringBuilder sb = new StringBuilder();
			while (index < text.length && (Character.isLetter(text[index]) || text[index] == '\'')) {
				sb.append(text[index++]);
			}
			if (sb.length() > 0) {
				String word = sb.toString().toLowerCase();
				if (!stopwordRemover.isStopword(word)) {
					res.add(word);
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CommentReader t = new CommentReader(csvFile);
	}

}
