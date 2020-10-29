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
import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.classify.LogisticClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

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
	RVFDataset<Integer, Integer> trainSet;
	RVFDataset<Integer, Integer> testSet;
	RVFDataset<Integer, Integer> dataset;
	
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
				RVFDatum<Integer, Integer> RVFrow = new RVFDatum<Integer, Integer>(transToCount(input), comment.isCons == true ? 1 : 0);
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
	
	

	/**
	 * Wrapper function of LogisticClassifierFactory in coreNLP.
	 * @param dataset2
	 */
	public void train(RVFDataset<Integer, Integer> dataset2) {
		LogisticClassifierFactory<Integer, Integer> factory = new LogisticClassifierFactory<>();
		classifier = factory.trainClassifier(dataset2);
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
	
	/**
	 * Convert the count input array to Counter in coreNLP
	 * Counter is like a small map, with key being the index.
	 * @param input  [0, 1, 2, 5, 1, 2, 4, ...] means the word[3] appeared 5 times.
	 * @return Counter type
	 */
	public static Counter<Integer> transToCount(int input[]) {
		// transfer the input array of a comment to the Counter object features
		// features[i+1] is the number of occurrence of the ith word in the commend
		Counter<Integer> features = new ClassicCounter<Integer>();
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
		for (int i = 0; i < len; i++) {
			String word = featureName[i];
			Integer count = commentMap.get(word);
			counts[i] = count == null ? 0 : count;
		}
		comment.setCount(counts);
		return counts;
	}

	private void setVocab(List<Comment> comments) {
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
		LRClassifier cla = new LRClassifier(t.comments, 0.7, 5);
//		Dataset<Integer, Integer> set = cla.trainSet;
//		set.getRandomSubDataset(0.7, 2020);
	}

}
