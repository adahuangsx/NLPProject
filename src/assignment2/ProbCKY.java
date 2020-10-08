package assignment2;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import assignment2.CNFParser.Bracket;
import assignment2.CNFParser.Cell;
import hw2.PCKY;

public class ProbCKY {
	
	private CNFParser sentenceParser;
	
	public  ProbCKY(String filePath, String sentence, String goldenStd) {
		try {
			sentenceParser = new CNFParser(filePath, sentence);
			if (sentenceParser.ifAccepted()) {
				System.out.println("Sentence Accepted! Sentence's Prob: " + sentenceParser.getSentenceProb());
				evaluateAndPrint(sentence, goldenStd);
			}
			else {
				System.out.println("Sentence Denied..");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void evaluateAndPrint(String sentence, String goldenStd) {
		CNFParser parser = this.sentenceParser;
		List<Bracket> goldBracs = parser.parseConstituency(sentence, goldenStd);
		Set<Bracket> goldBracSet = new HashSet<>(goldBracs);
		int ind = 0;
		for (Cell candidate : parser.getCandidates()) {
			ind++;
			List<Bracket> candBracs = parser.parseConstituency(sentence, candidate.tree);
			int matchedNum = 0;
			for (Bracket each : candBracs) {
				if (goldBracSet.contains(each)) {
					matchedNum++;
				}
			}
			double precision = 1.0 * matchedNum / candBracs.size();
			double recall = 1.0 * matchedNum / goldBracs.size();
			double F_1 = precision / recall;
			// print
			System.out.println();
			System.out.println("For candidate(" + ind + "): \n" + candidate.tree);
			System.out.println("Prob: " + candidate.prob);
			System.out.println("Precision: " + precision);
			System.out.println("Recall: " + recall);
			System.out.println("F-1 score: " + F_1);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProbCKY t = new ProbCKY("data//grammar.txt", "I book a flight to Houston", "[S [NP [Pronoun I]] [VP [Verb book] [NP [Det a] [Nominal [Noun flight]]] [PP [Preposition to] [NP [Proper-Noun houston]]]]]");
	    
	}

}
