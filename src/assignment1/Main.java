package assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	
	public Map<String, Integer> lm;
	public Map<String, Integer> uni;
	public Map<String, Integer> bi;
	public Map<String, Integer> tri;
	public int sizeOfUniword; // uniword size is the total count of characters
	public int sizeOfBiword;  // multigram size is the count of map entry
	public int sizeOfTriword;
	
	String terminal = String.valueOf('$');
	int V = 30; // in add-k, "+k/+kV"

	public static String testDir = "data//test"; // address of test
	public static String deTrainingDir = "data//training.de";
	public static String enTrainingDir = "data//training.en";
	public static String esTrainingDir = "data//training.es"; // address of training dataset
	
	public void buildUnigramModel(String fileName) throws IOException {
		// this function gives frequency of each letter
		BufferedReader testReader = new BufferedReader(new FileReader(fileName));
		String line = null;
		Map<String, Integer> count = new HashMap<>();
		
		count.put(terminal, 0);
		int totalCount = 1; // there is already terminal char in map, so 1 instead of 0.
		String cc = null; // to process char c
		while ((line = testReader.readLine()) != null) { // unigram doesn't consider the sentences
			for (char c : line.toCharArray()) {
				if (Character.isLetter(c) || c == ' ' || c == ',' || c == '.' || c == '\'') {
					if (Character.isLowerCase(c)) {
						c = (char) (c + 'A' - 'a');
					}
					cc = String.valueOf(c);
					if (!count.containsKey(cc)) {
						count.put(cc, 1);
					}
					else {
						count.put(cc, count.get(cc) + 1);
					}
					totalCount++;
				}
			}
			count.put(terminal, count.get(terminal) + 1); // add a '$' to the end of this sentence.
		}
		this.sizeOfUniword = totalCount;
		uni = new HashMap<>(count);
		lm.putAll(count);
		System.out.println("File " + fileName + "\'s unigram model is ready.");
		testReader.close();
	}
	
	public void buildMultigramModel(String fileName, int n) throws IOException {
		// this function gives frequency of each n letters like "ab" or "abc"
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuilder sb = null;
		Map<String, Integer> count = new HashMap<>();
		int totalCount = 0;
		while ((line = reader.readLine()) != null) {
			sb = new StringBuilder();
//			sb.append(terminal);  // no "$" at the beginning
			for (char c : line.toCharArray()) {
				if (Character.isLetter(c) || c == ' ' || c == ',' || c == '.' || c == '\'') {
					if (Character.isLowerCase(c)) {
						c = (char) (c + 'A' - 'a');
					}
					sb.append(c);
				}
			}
			sb.append(terminal);
			// sb is established and preprocessed: "hello, this is a sentence.$"
			
			while (sb.length() >= n) { // n == 2 if it is a bigram model
				String crt = sb.substring(0, n); // the first n characters
				if (count.containsKey(crt)) {
					count.put(crt, count.get(crt) + 1);
					totalCount++;
				}else {
					count.put(crt, 1);
				}
				sb.deleteCharAt(0);
			}
		}
		if (n == 2) {
			bi = new HashMap<>(count);
			lm.putAll(bi);
			this.sizeOfBiword = totalCount;
		} else if (n == 3) {
			tri = new HashMap<>(count);
			lm.putAll(tri);
			this.sizeOfTriword = totalCount;
		}
		System.out.println("File " + fileName + "\'s " + n + "-gram model is ready.");
		reader.close();
	}
	
	public void training(String fileName) throws IOException {
		lm = new HashMap<>();
		buildUnigramModel(fileName);
		buildMultigramModel(fileName, 2);
		buildMultigramModel(fileName, 3);
	}
	
	public void calculateScore(String fileName, double addk, int[] lambdas) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuilder sb = null;
		List<Double> prob_uni = new ArrayList<>();
		List<Double> perp_uni = new ArrayList<>();
		List<Double> prob_bi = new ArrayList<>();
		List<Double> perp_bi = new ArrayList<>();
		List<Double> prob_tri = new ArrayList<>();
		List<Double> perp_tri = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
			sb = new StringBuilder();
			sb.append(terminal);
			for (char c : line.toCharArray()) {
				if (Character.isLetter(c) || c == ' ' || c == ',' || c == '.' || c == '\'') {
					if (Character.isLowerCase(c)) {
						c = (char) (c + 'A' - 'a');
					}
					sb.append(c);
				}
			}
			sb.append(terminal);
			// StringBuilder sb_copy = new StringBuilder(sb); // sb can only be used once.
			List<Double> likelihood_uni = calculateLineProbwithUnigramModel(new StringBuilder(sb));
			double crtProb_uni = meanProbability(likelihood_uni);
			double crtPerp_uni = meanPerplexity(likelihood_uni);
			List<Double> likelihood_bi = calculateLineProbwithBigramModel(new StringBuilder(sb), addk);
			double crtProb_bi = meanProbability(likelihood_bi);
			double crtPerp_bi = meanPerplexity(likelihood_bi);
			List<Double> likelihood_tri = calculateLineProbwithTrigramModel(new StringBuilder(sb), addk);
			double crtProb_tri = meanProbability(likelihood_tri);
			double crtPerp_tri = meanPerplexity(likelihood_tri);
			prob_uni.add(crtProb_uni);
			perp_uni.add(crtPerp_uni);
			prob_bi.add(crtProb_bi);
			perp_bi.add(crtPerp_bi);
			prob_tri.add(crtProb_tri);
			perp_tri.add(crtPerp_tri);
		}
		System.out.println("Test score:");
		System.out.println("(Unigram, add:" + addk + ") Prob.  Perp.");
		System.out.println(meanProbability(prob_uni) + "  " + meanProbability(perp_uni));
		System.out.println("(Bigram, add:" + addk + ") Prob.  Perp.");
		System.out.println(meanProbability(prob_bi) + "  " + meanProbability(perp_bi));
		System.out.println("(Trigram, add:" + addk + ") Prob.  Perp.");
		System.out.println(meanProbability(prob_tri) + "  " + meanProbability(perp_tri));
		reader.close();
	}
	private double meanProbability(List<Double> llhd) {
		double res = 0;
		for (double prob : llhd) {
			res += prob;
		}
		return res / llhd.size();
	}
	private double meanPerplexity(List<Double> llhd) {
		double res = 1;
		for (double prob : llhd) {
			if (prob != 0) // this can be avoided by add-one
				res *= prob;
		}
		return Math.pow(res, -1.0 / llhd.size());
	}
	
	public List<Double> calculateLineProbwithUnigramModel(StringBuilder sb) {
		List<Double> likelihood = new ArrayList<>();
		for (char c : sb.toString().toCharArray()) {
			String uni = String.valueOf(c);
			if (lm.get(uni) == null) {
				likelihood.add(0.0);
			} else {
				likelihood.add(1.0 * lm.get(uni) / sizeOfUniword);
			}
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithBigramModel(StringBuilder sb, double addk) {
		List<Double> likelihood = new ArrayList<>();
//		likelihood.add(1.0 * (lm.get(sb.substring(0, 1)) + addk) / (sizeOfUniword) + addk * this.V);
		while (sb.length() > 2) {
			String bi = sb.substring(0, 2);
			String uni = bi.substring(0, 1);
			int countBi = lm.get(bi) == null ? 0 : lm.get(bi);
			int countUni = lm.get(uni) == null ? 0 : lm.get(uni);
			if (addk == 0 && (countUni == 0 || countBi == 0)) {
				likelihood.add(0.0);
			} else {
				likelihood.add(1.0 * (countBi + addk) / (countUni + addk * this.V));				
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithTrigramModel(StringBuilder sb, double addk) {
		List<Double> likelihood = new ArrayList<>();
		String firstBi = sb.substring(0, 2);
		String firstUni = firstBi.substring(0, 1);
		if (lm.get(firstBi) != null && lm.get(firstUni) != null) {
			likelihood.add(1.0 * lm.get(firstUni) / sizeOfUniword);
			likelihood.add(1.0 * (lm.get(firstBi) + addk) / (lm.get(firstUni) + addk * this.V));
		}
		while (sb.length() > 3) {
			String tri = sb.substring(0, 3);
			String bi = tri.substring(0, 2);
			int countBi = lm.get(bi) == null ? 0 : lm.get(bi);
			int countTri = lm.get(tri) == null ? 0 : lm.get(tri);
			if (addk == 0 && (countBi == 0 || countTri == 0)) {
				likelihood.add(0.0);
			} else {
				likelihood.add(1.0 * (countTri + addk) / (countBi + addk * this.V));
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public void checkValid(String history) {
		int historyCount = lm.get(history);
		List<Integer> counts = new ArrayList<>();
		int total = 0;
		for (Map.Entry<String, Integer> entry : this.tri.entrySet()) {
			if (entry.getKey().startsWith(history)) {
				counts.add(entry.getValue());
				System.out.print(entry.getKey() + " ");
				total += entry.getValue();
			}
		}
		double prob = 0;
		for (int count : counts) {
			prob += 1.0 * count / total;
		}
		System.out.println("The total prob. of prefix " + history + " is " + prob);
	}
	
	public double calculateScoreWithTrigramModel(Map<String, Integer> bigram, Map<String, Integer> trigram, String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			for (char c : line.toCharArray()) {
				if (Character.isLetter(c)) {
					if (!Character.isLowerCase(c)) {
						c = (char) (c - 'A' + 'a');
					}
					sb.append(c);
				}
				else if (sb.charAt(sb.length() - 1) != '$'){
					sb.append('$');
				}
			}
		}
		// sb is processed into "abcds$aa$fdja ..."
		double logScore = 0;
//		double score = 1.0;
		while (sb.length() >= 3) {
			String crtTri = sb.substring(0, 3);
			String crtBi = crtTri.substring(0, 2);
			if (bigram.get(crtBi) == null || trigram.get(crtTri) == null) {
//				System.out.println("Missing: " + crtTri + ", " + crtBi);
			}
			else {
				logScore = logScore + Math.log(trigram.get(crtTri)) - Math.log(bigram.get(crtBi));
//				System.out.println(crtBi + ", " + bigram.get(crtBi) + ", " + crtTri + ", " + trigram.get(crtTri));
//				score = score * bigram.get(crtBi) / unigram.get(crtC); // MLE
			}
			sb.deleteCharAt(0);
		}
		reader.close();
		return logScore;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Main t = new Main();
		t.training(enTrainingDir);
		t.checkValid("AB");
		
//		t.training(enTrainingDir);
//		t.calculateScore(testDir, 0, null);
//		t.training(esTrainingDir);
//		t.calculateScore(testDir, 0, null);
//		t.training(deTrainingDir);
//		t.calculateScore(testDir, 0, null);
//		
//		t.training(enTrainingDir);
//		t.calculateScore(testDir, 1, null);
//		t.training(esTrainingDir);
//		t.calculateScore(testDir, 0, null);
//		t.training(deTrainingDir);
//		t.calculateScore(testDir, 0, null);
	}

}
