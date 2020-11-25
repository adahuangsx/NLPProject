package finalproject;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.csvreader.CsvReader;

public class StoryRecordReader {

	public static final String TRAIN_DATA = "data//Supporting Materials//ROC-Story-Cloze-Data.csv";
	public static final String TEST_DATA = "data//Supporting Materials//ROC-Story-Cloze-Test-Release.csv";
	public static final String VAL_DATA = "data//Supporting Materials//ROC-Story-Cloze-Val.csv";
	
	CsvReader csvReader;
	
	public StoryRecordReader (String file) throws IOException {
		
		csvReader = new CsvReader(file);
		csvReader.readHeaders();
		
	}
	
	public Record next() throws IOException {
		String storyID = null;
		StringBuilder input = new StringBuilder();
		String rightSentc = null;
		String wrongSentc = null;
		while (csvReader.readRecord()) {
			storyID = csvReader.get(0); // "InputStoryid"
			for (int i = 1; i <= 4; i++) {
				input.append(csvReader.get(i)).append(" ");  // "InputSentence" + i
			}
			if (csvReader.get(7) == "1") {  // "AnswerRightEnding"
				rightSentc = csvReader.get(5);  // "RandomFifthSentenceQuiz1"
				wrongSentc = csvReader.get(6);  // "RandomFifthSentenceQuiz2"
			}
			else {
				rightSentc = csvReader.get(6);  // "RandomFifthSentenceQuiz2"
				wrongSentc = csvReader.get(5);  // "RandomFifthSentenceQuiz1"
			}
			return new Record(storyID, input.toString(), rightSentc, wrongSentc);
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		StoryRecordReader t = new StoryRecordReader(TRAIN_DATA);
		Record r = null;
		int i = 50;
		while ((r = t.next()) != null && i > 0) {
			System.out.println(r);
			i--;
		}
	}

}
