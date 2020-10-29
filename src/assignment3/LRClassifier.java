package assignment3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.classify.LogisticClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;

public class LRClassifier {
	private String[] featureName;
	private static final int bowMinCount = 2; // bag of words lowest count threshold
	/* when this threshold is set to <X>, the length of vocabList will be:
	 * 1: 3742
	 * 2: 2457
	 * 3: 1869
	 * 4: 1492
	 * 5: 1230   (total vocab count: 7974)
	 */
	private static final int bias = 1; // means the intercept of LR model is not zero.
	LogisticClassifier<Integer, Integer> classifier;
	LogisticClassifier<Integer, Double> classifierDouble;
	RVFDataset<Integer, Integer> trainSet;
	RVFDataset<Integer, Integer> testSet;
	RVFDataset<Integer, Integer> dataset;
	
	Map<String, Double> idfMap;
	private int commentNum;
	
	public LRClassifier (List<Comment> comments, double split, int fold) {
		
		setVocab(comments);
		double sumScore = 0.0;
		for (int i = 0; i < fold; i++) {
			dataset = new RVFDataset<>();
			trainSet = new RVFDataset<>();
			testSet = new RVFDataset<>();
			Random r = new Random();
			for (Comment comment : comments) {
				int[] input = getCommentCount(comment);
				RVFDatum<Integer, Integer> RVFrow = new RVFDatum<Integer, Integer>(transToCounterInt(input), comment.isCons == true ? 1 : 0);
				dataset.add(RVFrow);
				if (r.nextInt(comments.size()) / (double)comments.size() < split) {
					trainSet.add(RVFrow);
				} else {
					testSet.add(RVFrow);
				}
			}
			train(trainSet);
			System.out.println("trained. Fold: " + i);
			System.out.println(dataset.size() + " " + trainSet.size() + "/" + testSet.size());
			sumScore += test(testSet);
		}
		double testScore = sumScore / fold;
		System.out.println(testScore);
	}
	
	public LRClassifier (List<Comment> comments, double split, int fold, String norm) {
	
		setVocab(comments);
		 // idfmap
		Map<String, Double> idfmap = new HashMap<>();
		for (String word : featureName) {
			int df = 0;
			for (Comment comment : comments) {
				if (comment.toMap().get(word) != null) {
					df++;
				}
			}
			double idf = Math.log10(((double) commentNum / df));
			idfmap.put(word, idf);
		}
		idfMap = idfmap;
		
		double sumScore2 = 0.0;
		for (int i = 0; i < fold; i++) {
			RVFDataset<Integer, Integer> dataset2 = new RVFDataset<>();
			RVFDataset<Integer, Integer> trainSet2 = new RVFDataset<>();
			RVFDataset<Integer, Integer> testSet2 = new RVFDataset<>();
			Random r = new Random();
			for (Comment comment : comments) {
				int[] input = getCommentCount(comment);
				double[] tfidfInput = new double[input.length];
				// tf
				for (int j = 0; j < input.length; j++) {
					double tf = comment.words.size() == 0 ? 0 : ((double)input[j])/* / (comment.words.size())*/;
					double idf = idfMap.getOrDefault(featureName[j], 0.0);
					tfidfInput[j] = tf * idf; 
//					tfidfInput[j] = (double)input[j];
//					if (Double.isNaN(tfidfInput[j])) {
//						System.out.print("[" + tf + " " + idf + "]");
//					}
				}
				Datum<Integer, Integer> row = new RVFDatum<Integer, Integer>(transToCounterDouble(tfidfInput), comment.isCons == true ? 1 : 0);
				dataset2.add(row);
				if (r.nextInt(comments.size()) / (double)comments.size() < split) {
					trainSet2.add(row);
				} else {
					testSet2.add(row);
				}
//				System.out.println(row.toString());
			}
			train(trainSet2);
			System.out.println("trained. " + norm + " Fold: " + i);
			
			System.out.println(dataset2.size() + " " + trainSet2.size() + "/" + testSet2.size());
			sumScore2 += test(testSet2);
		}
		
		double testScore2 = sumScore2 / fold;
		System.out.println(testScore2);
	}
	
	/**
	 * Wrapper function of LogisticClassifierFactory in coreNLP.
	 * @param dataset2
	 */
	public void train(RVFDataset<Integer, Integer> dataset2) {
		LogisticClassifierFactory<Integer, Integer> factory = new LogisticClassifierFactory<>();
		classifier = factory.trainClassifier(dataset2);
	}
	
	/**
	 * Wrapper function of LogisticClassifierFactory in coreNLP.
	 * @param dataset2
	 */
	public void trainDouble(Dataset<Integer, Integer> dataset2) {
		LogisticClassifierFactory<Integer, Integer> factory = new LogisticClassifierFactory<>();
		classifier = factory.trainClassifier(dataset2);
		System.out.println(classifier);
	}
	
	
	private double test(RVFDataset<Integer, Integer> testSet2) {
		if (classifier == null) {
			System.out.println("ERROR. Train it before testing!");
			return 0;
		}
		int correctPredictNum = 0;
		for (RVFDatum<Integer, Integer> test : testSet2) {
			int ob = test.label();
			int pre = classifier.classOf(test.asFeaturesCounter());
			if (ob == pre) { correctPredictNum++; }
			
		}
		return ((double) correctPredictNum) / testSet2.size();
	}
	
	private double testDouble(Dataset<Integer, Double> testSet2) {
		if (classifierDouble == null) {
			System.out.println("ERROR. Train it before testing!");
			return 0;
		}
		int correctPredictNum = 0;
		for (RVFDatum<Integer, Double> test : testSet2) {
			int ob = test.label();
			int pre = classifierDouble.classOf(test.asFeaturesCounter());
			if (ob == pre) { correctPredictNum++; }
			
		}
		return ((double) correctPredictNum) / testSet2.size();
	}
	
//	private Map<String, Double> getIDFMap(){
//		Map<String, Double> map = new HashMap<>();
//		if (classifier == null) {
//			System.out.println("ERROR. Train it before testing!");
//			return map;
//		}
//		
//	}
	
	/**
	 * Convert the count input array to Counter in coreNLP
	 * Counter is like a small map, with key being the index.
	 * @param input  [0, 1, 2, 5, 1, 2, 4, ...] means the word[3] appeared 5 times.
	 * @return Counter type
	 */
	public static Counter<Integer> transToCounterInt(int input[]) {
		// transfer the input array of a comment to the Counter object features
		// features[i+1] is the number of occurrence of the ith word in the commend
		Counter<Integer> features = new ClassicCounter<Integer>();
		features.setCount(0, bias);
		for (int i = 0; i < input.length; i++)
			features.setCount(i + 1, input[i]);
		return features;
	}
	/**
	 * Convert the count input array to Counter in coreNLP
	 * Counter is like a small map, with key being the index.
	 * @param input  [0, 1, 2, 5, 1, 2, 4, ...] means the word[3] appeared 5 times.
	 * @return Counter type
	 */
	public static Counter<Integer> transToCounterDouble(double input[]) {
		// transfer the input array of a comment to the Counter object features
		// features[i+1] is the number of occurrence of the ith word in the commend
		Counter<Integer> features = new ClassicCounter<>();
		features.setCount(0, bias);
		for (int i = 0; i < input.length; i++)
			features.setCount(i + 1, input[i]);
		return features;
	}
	
	private int[] getCommentCount(Comment comment) {
		// TODO Auto-generated method stub
		if (featureName == null) {
			System.out.println("ERROR. vocab is not inited");
		}
		
		int len = featureName.length;
		Map<String, Integer> commentMap = comment.toMap();
		int[] counts = new int[len];
		for (int i = 0; i < len; i++) { // for each vocab word
			String word = featureName[i];
			Integer count = commentMap.get(word);
			counts[i] = count == null ? 0 : count;
		}
		comment.setCount(counts);
		return counts;
	}

	private void setVocab(List<Comment> comments) {
		commentNum = comments.size();
		Map<String, Double> idfmap = new HashMap<>();
		Map<String, Integer> vocabCount = new HashMap<>();
		for (Comment comment : comments) {
			for (String word : comment.words) {
				if (!vocabCount.containsKey(word)) {
					vocabCount.put(word, 1);
				}
				else {
					vocabCount.put(word, vocabCount.get(word) + 1);
				}
			}
		}
		List<String> vocabList = new ArrayList<String>();
		for (String vocab : vocabCount.keySet()) {
			if (vocabCount.get(vocab) > bowMinCount) {
				vocabList.add(vocab);
			}
		}
		Collections.sort(vocabList);
		featureName = vocabList.toArray(new String[0]);
	}
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CommentReader t = new CommentReader(CommentReader.csvFile);
//		LRClassifier cla = new LRClassifier(t.comments, 0.7, 5);
		
		LRClassifier cla2 = new LRClassifier(t.comments, 0.7, 2, "tf-idf");
//		Dataset<Integer, Integer> set = cla.trainSet;
//		set.getRandomSubDataset(0.7, 2020);
	}

}
