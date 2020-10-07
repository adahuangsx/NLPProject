package assignment2;

import java.io.IOException;

public class ProbCKY {
	
	private CNFParser sentenceParser;
	
	public  ProbCKY(String filePath, String sentence, String goldenStd) {
		try {
			sentenceParser = new CNFParser(filePath, sentence);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
