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
	public int sizeOfUniword;
	public int sizeOfBiword;
	public int sizeOfTriword;
	
	String terminal = String.valueOf('$');

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
						totalCount++; // only count the de-duplicate.
					}
					else {
						count.put(cc, count.get(cc) + 1);
					}
				}
				 // unigram doesn't consider non-letter chars, like spaces or commas.
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
			// sb is established and preprocessed: "hello, this is a sentence."
			
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
		System.out.println("File " + fileName + "\'s 2, 3-gram model is ready.");
		reader.close();
	}
	
	public void calculateScore(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		StringBuilder sb = null;
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
			List<Double> likelihood_bi = calculateLineProbwithBigramModel(new StringBuilder(sb));
			double crtProb_bi = meanProbability(likelihood_bi);
			double crtPerp_bi = meanPerplexity(likelihood_bi);
			List<Double> likelihood_tri = calculateLineProbwithTrigramModel(new StringBuilder(sb));
			double crtProb_tri = meanProbability(likelihood_tri);
			double crtPerp_tri = meanPerplexity(likelihood_tri);
			prob_bi.add(crtProb_bi);
			perp_bi.add(crtPerp_bi);
			prob_tri.add(crtProb_tri);
			perp_tri.add(crtPerp_tri);
		}
		System.out.println("Test score:");
		System.out.println("Bigram: ");
		System.out.println("average probability of sentences: " + meanProbability(prob_bi));
		System.out.println("average perplexity of sentences: " + meanProbability(perp_bi));
		System.out.println("Trigram: ");
		System.out.println("average probability of sentences: " + meanProbability(prob_tri));
		System.out.println("average perplexity of sentences: " + meanProbability(perp_tri));
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
	
	public List<Double> calculateLineProbwithBigramModel(StringBuilder sb) {
		List<Double> likelihood = new ArrayList<>();
		while (sb.length() > 2) {
			String bi = sb.substring(0, 2);
			String uni = bi.substring(0, 1);
			if (lm.get(uni) == null || lm.get(bi) == null) {
				likelihood.add(0.0);
			} else {
				likelihood.add(1.0 * lm.get(bi) / lm.get(uni));				
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithTrigramModel(StringBuilder sb) {
		List<Double> likelihood = new ArrayList<>();
		while (sb.length() > 3) {
			String tri = sb.substring(0, 3);
			String bi = tri.substring(0, 2);
			if (lm.get(bi) == null || lm.get(tri) == null) {
				likelihood.add(0.0);
			} else {
				likelihood.add(1.0 * lm.get(tri) / lm.get(bi));
			}
			sb.deleteCharAt(0);
		}
		return likelihood;
	}
	public List<Double> calculateLineProbwithUnigramModel(StringBuilder sb) {
		List<Double> likelihood = new ArrayList<>();
		
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
	
	public void training(String fileName) throws IOException {
		lm = new HashMap<>();
		buildUnigramModel(fileName);
		buildMultigramModel(fileName, 2);
		buildMultigramModel(fileName, 3);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Main t = new Main();
		t.training(enTrainingDir);
		t.calculateScore(testDir);
		t.training(esTrainingDir);
		t.calculateScore(testDir);
		t.training(deTrainingDir);
		t.calculateScore(testDir);
	}

}
