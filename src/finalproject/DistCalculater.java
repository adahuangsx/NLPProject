package finalproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistCalculater {

	public double distance(int[] bow1, int[] bow2) {
		if (bow1.length != bow2.length) {
			System.out.println("the lengths are different.");
			return -1;
		}
		double sum = 0;
		double bow1Abs = 0;
		double bow2Abs = 0;
		for (int i = 0; i < bow1.length; i++) {
			bow1Abs += Math.pow(bow1[i], 2);
			bow2Abs += Math.pow(bow2[i], 2);
			sum += bow1[i] * bow2[i];
		}
		double dist = ((double) sum) / (Math.sqrt((double) bow1Abs) + Math.sqrt((double) bow2Abs));
		return dist;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		StoryRecordReader rdr = new StoryRecordReader(StoryRecordReader.VAL_DATA);
		BowConverter t = new BowConverter();
		DistCalculater c = new DistCalculater();
		Record r = null;
		List<Record> records = new ArrayList<>();
		int i = 50;
		int counter = 0;
		while ((r = rdr.next()) != null && i > 0) {
			records.add(r);
			if (++counter % 300 == 0) {
				System.out.println("finished reading in " + counter + " records. ");
			}
		}
		t.inputAllwords(records);
		
		counter = 0;
		int trueNum = 0;
		for (Record record : records) {
			int[] input4Bow = t.convertBOW(record.inputSentce);
			int[] rightstcBow = t.convertBOW(record.rightEnd);
			int[] wrongstcBow = t.convertBOW(record.wrongEnd);
			double rightstcDist = c.distance(input4Bow, rightstcBow);
			double wrongstcDist = c.distance(input4Bow, wrongstcBow);
			if (rightstcDist < wrongstcDist) {
				trueNum++;
			}
			if (++counter % 300 == 0) {
				System.out.println("finished calculating " + counter + " records. ");
			}
		}
		System.out.println((double) trueNum / records.size());
	}

}
