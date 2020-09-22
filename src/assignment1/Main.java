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
	
	public void training(String fileName, double holdOut) throws IOException {
		lm = new HashMap<>();
		buildUnigramModel(fileName);
		buildMultigramModel(fileName, 2);
		buildMultigramModel(fileName, 3);
	}
	
	public void calculateScore(String fileName, double addk, double[] lambdas_bi, double[] lambdas_tri) throws IOException {
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
			List<Double> likelihood_bi = calculateLineProbwithBigramModel(new StringBuilder(sb), addk, lambdas_bi);
			double crtProb_bi = meanProbability(likelihood_bi);
			double crtPerp_bi = meanPerplexity(likelihood_bi);
			List<Double> likelihood_tri = calculateLineProbwithTrigramModel(new StringBuilder(sb), addk, lambdas_tri);
			double crtProb_tri = meanProbability(likelihood_tri);
			double crtPerp_tri = meanPerplexity(likelihood_tri);
//			System.out.print(crtPerp_tri + " ");
			prob_uni.add(crtProb_uni);
			perp_uni.add(crtPerp_uni);
			prob_bi.add(crtProb_bi);
			perp_bi.add(crtPerp_bi);
			prob_tri.add(crtProb_tri);
			perp_tri.add(crtPerp_tri);
		}
		
		System.out.println("Test score:");
		System.out.println("(Unigram, add:" + addk + ") Prob " + meanProbability(prob_uni) + " Perp." + mean(perp_uni));
		System.out.println("(Bigram, add:" + addk + ") Prob " + meanProbability(prob_bi) + " Perp." + mean(perp_bi));
		System.out.println("(Trigram, add:" + addk + ") Prob " + meanProbability(prob_tri) + " Perp." + mean(perp_tri));
		reader.close();
	}
	private double meanProbability(List<Double> llhd) {
		double res = 0.0;
		for (double prob : llhd) {
			res += prob; 
		}
		return res / llhd.size();
	}
	private double mean(List<Double> llhd) {
		double res = 0.0;
		for (double prob : llhd) {
			res += prob; 
//			System.out.print(res + " ");
		}
//		System.out.println("res: " + res + ", " + llhd.size());
		return res / llhd.size();
	}
	private double meanPerplexity(List<Double> llhd) {
		double res = 1.0;
		for (double prob : llhd) {
			if (prob > 0.0) // this can be avoided by add-one
				res *= Math.pow(prob, -1.0 / llhd.size());
		}
		return res;
	}
	
	public List<Double> calculateLineProbwithUnigramModel(StringBuilder sb) {
		List<Double> likelihood = new ArrayList<>();
		for (char c : sb.toString().toCharArray()) {
			String uni = String.valueOf(c);
			if (lm.get(uni) == null) {
				likelihood.add(Double.MIN_VALUE);
			} else {
				likelihood.add(1.0 * lm.get(uni) / sizeOfUniword);
			}
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithBigramModel(StringBuilder sb, double addk, double[] lambdas_bi) {
		List<Double> likelihood = new ArrayList<>();
		double[] lambdas = lambdas_bi == null ? new double[] {1.0, 0.0} : lambdas_bi;
		likelihood.add(1.0 * (lm.get(sb.substring(0, 1)) + addk) / (sizeOfUniword) + addk * this.V);
		while (sb.length() > 2) {
			String bi = sb.substring(0, 2);
			String uni = bi.substring(0, 1);
			String wn = bi.substring(1, 2); // used in linear interpolation
			int countBi = lm.get(bi) == null ? 0 : lm.get(bi);
			int countUni = lm.get(uni) == null ? 0 : lm.get(uni);
			int countwn = lm.get(wn) == null ? 0 : lm.get(wn);
			if (addk == 0 && (countUni == 0 || countBi == 0)) {
				likelihood.add(Double.MIN_VALUE);
			} else {
				likelihood.add(lambdas[0] * (countBi + addk) / (countUni + addk * this.V) + 
							   lambdas[1] * countwn / this.sizeOfBiword);				
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithTrigramModel(StringBuilder sb, double addk, double[] lambdas_tri) {
		List<Double> likelihood = new ArrayList<>();
		double[] lambdas = lambdas_tri == null ? new double[] {1.0, 0.0, 0.0} : lambdas_tri;
		String firstBi = sb.substring(0, 2);
		String firstUni = firstBi.substring(0, 1);
		if (lm.get(firstBi) != null && lm.get(firstUni) != null) {
			likelihood.add(1.0 * lm.get(firstUni) / sizeOfUniword);
			likelihood.add(1.0 * (lm.get(firstBi) + addk) / (lm.get(firstUni) + addk * this.V));
		}
		while (sb.length() > 3) {  				// say, sb is "hello, ..."
			String tri = sb.substring(0, 3); 	// tri: "hel"
			String bi = tri.substring(0, 2); 	// bi:  "he"
			String wn = tri.substring(0, 1); 	// wn:  "l"
			String wn_1 = tri.substring(1, 2); 	// wn-1: "e"
			String wn_1_2 = tri.substring(1, 3); // wn-1, wn-2: "el"
			int countBi = lm.get(bi) == null ? 0 : lm.get(bi);
			int countTri = lm.get(tri) == null ? 0 : lm.get(tri);
			int countWn = lm.get(wn) == null ? 0 : lm.get(wn);
			int countWn_1 = lm.get(wn_1) == null ? 0 : lm.get(wn_1);
			int countWn_1_2 = lm.get(wn_1_2) == null ? 0 : lm.get(wn_1_2);
			if (addk == 0 && (countBi == 0 || countTri == 0 || countWn_1 == 0)) {
				likelihood.add(Double.MIN_VALUE);
			} else {
				likelihood.add(lambdas[0] * (countTri + addk) / (countBi + addk * this.V) +
							   lambdas[1] * (countWn_1_2 + addk) / (countWn_1 + addk * this.V) +
							   lambdas[2] * countWn / this.sizeOfUniword);
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public void checkValid(String history) {
		int historyCount = lm.get(history);
		List<Integer> counts = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : this.tri.entrySet()) {
			if (entry.getKey().startsWith(history)) {
				counts.add(entry.getValue());
				System.out.print(entry.getKey() + " ");
			}
		}
		double prob = 0;
		for (int count : counts) {
			prob += 1.0 * count / historyCount;
		}
		System.out.println("The total prob. of prefix " + history + " is " + prob);
	}
	
	public void generate (String start) {
		// generate the second character
		int maxCount = 0;
		String selected = null;
		for (Map.Entry<String, Integer> entry : this.bi.entrySet()) {
			if (entry.getKey().startsWith(start) && entry.getValue() > maxCount) {
				selected = entry.getKey();
				maxCount = entry.getValue();
			}
		}
		start = selected;
		StringBuilder sb = new StringBuilder(start);
		while (sb.charAt(0) != '$') {
			System.out.print(sb.charAt(0));
			sb.deleteCharAt(0);
			maxCount = 0;
			for (Map.Entry<String, Integer> entry : this.tri.entrySet()) {
				if (entry.getKey().startsWith(sb.toString()) && entry.getValue() > maxCount) {
					selected = entry.getKey();
					maxCount = entry.getValue();
				}
			}
			sb = new StringBuilder(selected);
		}
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Main t = new Main();
		t.training(esTrainingDir, 0.7);
//		t.checkValid("AB");
		
//		t.training(enTrainingDir);
//		t.calculateScore(testDir, 0, null, null);
//		t.training(esTrainingDir);
//		t.calculateScore(testDir, 0, null, null);
//		t.training(deTrainingDir);
//		t.calculateScore(testDir, 0, null, null);
//		double k = 0.7;
//		t.training(enTrainingDir, 0.7);
//		t.calculateScore(testDir, k, null, null);
//		t.training(esTrainingDir);
//		t.calculateScore(testDir, k, null, null);
//		t.training(deTrainingDir);
//		t.calculateScore(testDir, k, null, null);
//		double[] lambdas_tri = new double[] {1.0 / 3, 1.0 / 3, 1.0 / 3};
//		double[] lambdas_bi = new double[] {1.0 / 2, 1.0 / 2};
//		double[] lambdas_tri = new double[] {0.5, 0.3, 0.2};
//		double[] lambdas_bi = new double[] {0.7, 0.3};
//		t.training(enTrainingDir, 0.7);
//		t.calculateScore(testDir, 0, lambdas_bi, lambdas_tri);
//		t.training(esTrainingDir);
//		t.calculateScore(testDir, 0, lambdas_bi, lambdas_tri);
//		t.training(deTrainingDir);
//		t.calculateScore(testDir, 0, lambdas_bi, lambdas_tri);
		t.generate("A");
	}

}
