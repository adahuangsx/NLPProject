package assignment3;

import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

/**
 * This class can read the csv file and extract the F and G columns into a list of Comments.
 * Meanwhile, tokenize the words and remove the stopwords, and save the words into the Comment object.
 * @author Sixuan Huang
 */
public class CommentReader {
	

	public static final String csvFile = "data//SFUcorpus.csv";
	public List<Comment> comments;
	
	private StopWordRemover stopwordRemover = new StopWordRemover();

	
	public CommentReader(String file) throws Exception {
		comments = new ArrayList<>();
		
		CsvReader data = new CsvReader(file);
		data.readHeaders();
		while (data.readRecord()) {
			String comment = data.get("comment_text");
			List<String> words = tokenizeAndRemoveStopwords(comment.toCharArray());
//			System.out.println(words);
			boolean isConstru = data.get("is_constructive").equals("yes") ? true : false;
			comments.add(new Comment(comment, words, isConstru));
		}
	}
	
	/**
	 * tokenize the words and remove the stopwords, and output the words.
	 * @param text
	 * @return token words
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
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CommentReader t = new CommentReader(csvFile);
	}

}
