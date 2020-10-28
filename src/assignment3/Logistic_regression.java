package assignment3;

import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.classify.LogisticClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

public class Logistic_regression {
	private static final int bias = 1;
	public static Counter<Integer> transToCount(int input[]) {
		// transfer the input array of a comment to the Counter object features
		// features[i+1] is the number of occurrence of the ith word in the commend
		Counter<Integer> features = new ClassicCounter<Integer>();
		features.setCount(0, bias);
		for (int i = 0; i < input.length; i++)
			features.setCount(i + 1, input[i]);
		return features;
	}
	
	public static RVFDatum<Integer, Integer> transToDatum(int input[], int label) {
		// transfer the input array and label of a word to its RVFDatum
		return new RVFDatum<>(transToCount(input), label);
	}
	
	public static void main(String[] args) throws Exception {
		// the input is an array where the ith element contains 
		// how many time the ith word in vocabulary exists in this comment
		int input[] = {0, 1, 2};
		RVFDataset<Integer, Integer> dataset = new RVFDataset<>();
		// in this example all data has the sample input and most of them is in class 1
		// so the same input will be predicted to be class 1
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 1));
		dataset.add(transToDatum(input, 0));
		LogisticClassifierFactory<Integer, Integer> factory = new LogisticClassifierFactory<>();
		LogisticClassifier<Integer, Integer> classifier = factory.trainClassifier(dataset);
		System.out.println("The trained classfier:");
		System.out.println(classifier);
		int testSample[] = {0, 1, 2};
		Counter<Integer> features = transToCount(testSample);
		double prob = classifier.probabilityOf(new RVFDatum<>(transToCount(input), 1));
		System.out.println("prob of the input to be in class 1: " + prob);
		System.out.println("predicted label is:" + classifier.classOf(features));
	}
}
