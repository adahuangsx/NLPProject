package assignment2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CNFParser {
	class Cell {
		String symbol;
		String tree;
		double prob;
		
		public Cell (String symbol, String tree, double prob) {
			this.symbol = symbol;
			this.tree = tree;
			this.prob = prob;
		}
		@Override
		public String toString() {
			return "--" + symbol + " " + prob + " " + tree;
		}
	}

	class Bracket {
		String symbol;
		int leftInc;
		int rightExc;
		public Bracket(String sym, int i, int j) {
			symbol = sym;
			leftInc = i;
			rightExc = j;
		}
		
		@Override
		public boolean equals(Object that) {
			if (this == that) {
				return true;
			}
			if (that == null) {
				return false;
			}
			if (that instanceof Bracket) {
				Bracket thatBrac = (Bracket) that;
				if (this.symbol.equals(thatBrac.symbol) &&
						this.leftInc == thatBrac.leftInc &&
						this.rightExc == thatBrac.rightExc) {
					return true;
				}
			}
			return false;
		}
		
		@Override
	    public int hashCode() {
	        int result = 17;
	        result = 31 * result + (symbol == null ? 0 : symbol.hashCode());
	        result = 31 * result + leftInc * 13;
	        result = 31 * result + rightExc * 19;
	        return result;
		}
		
		@Override
		public String toString() {
			return symbol + "-[" + leftInc + ", " + rightExc + ")";
		}
	}
	
	PCFGReader readInRules;
	String[] words;
	private boolean accepted = false;
	private double sentenceProb;
	private List<List<List<Cell>>> CKYTable;
	/*
	 * The table structure:
	 * 			j = 0	j = 1	j = 2
	 * i = 0	(0, 0)
	 * i = 1	(1, 0)	(1, 1)
	 * i = 2	(2, 0)	(2, 1)	(2, 2)
	 * ...
	 * 
	 */
	
	public boolean ifAccepted() {
		return accepted;
	}
	public double getSentenceProb() {
		return this.sentenceProb;
	}
	
	public List<Cell> getCandidates() {
		List<Cell> valids = new ArrayList<>();
		for (Cell each : this.CKYTable.get(words.length - 1).get(0)) {
			if (each.symbol.equals("S")) {
				valids.add(each);
			}
		}
		return valids;
	}
	
	public CNFParser() {} // for test
	
	public CNFParser (String filePath, String sentence) throws IOException {
		this.readInRules = new PCFGReader(filePath);
		this.words = sentence.toLowerCase().split("\\s+");
		int num = words.length;
		this.CKYTable = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			CKYTable.add(new ArrayList<>());
			for (int j = 0; j <= i; j++) {
				CKYTable.get(i).add(new ArrayList<>());
			}
		}
		fillTableBottom();
		fillTableUpper();
		setAcceptedAndProb();
		
	}
	
	/**
	 * Fill the bottom layer, which is the (0, 0),(1, 1),(2, 2),... word layer.
	 */
	private void fillTableBottom() {
		int num = this.words.length;
		for (int i = 0; i < num; i++) {
			String word = words[i];
			for (Rule lex : this.readInRules.lexicons) {
				if (word.equals(lex.left)) {
					this.CKYTable.get(i).get(i).add(new Cell(lex.parent, getUnaryTree(lex, lex.left), lex.prob));
				}
			}
		}
	}
	
	/**
	 * Fill the non-bottom layer. Each cell needs to calculate every possible path.
	 */
	private void fillTableUpper() {
		int num = this.words.length;
		for (int i = 0; i < num; i++) {
			for (int j = i - 1; j >= 0; j--) {
				// from bottom to upper level
				// for each cell (i, j) (this cell is any non-bottom cell).
				List<Cell> crts = CKYTable.get(i).get(j);  // to store all the possibilities in the crt grid.
				for (int k = 1; k <= i - j; k++) { // k is the offset arm from cell (i, j).
					int k1 = k;				 // k1 k2 are clearer.
					int k2 = i - j + 1 - k1; // another arm should be k's compensate.
					// pair: [i - k2; j] and [i, j + k1]
					List<Cell> ones = CKYTable.get(i - k2).get(j);
					List<Cell> twos = CKYTable.get(i).get(j + k1); // ones and twos are two grids. Ones is front; twos is after it.
					for (Cell one : ones) {
						for (Cell two : twos) {
							for (Rule gram : this.readInRules.grammars) {
								if (one.symbol.equals(gram.left) && two.symbol.equals(gram.right)) {
									// a grammar rule is matched
									String crtTree = one.tree + two.tree;
									if (gram.parent.startsWith(PCFGReader.MID_RULE_PREFIX)) {
										// this grammar rule is a middle rule
										// do nothing, just save the crtTree
									}
									else {
										// this grammar rule is a normal one
										crtTree = getUnaryTree(gram, crtTree); // get all the mids
									}
									crts.add(new Cell (gram.parent, crtTree.toString(), gram.prob * one.prob * two.prob));
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * If the top of the table has an "S", then accept the sentence. And calculate the prob P(s) meanwhile.
	 */
	private void setAcceptedAndProb() {
		boolean accepted = false;
		double prob = 0;
		int num = this.words.length;
		for (Cell finalCell : this.CKYTable.get(num - 1).get(0)) {
			if (finalCell.symbol.equals("S")) {
				prob += finalCell.prob;
				accepted = true;
			}
		}
		this.accepted = accepted;
		sentenceProb = prob;
	}
	
	/**
	 * Used when one-child lexicon or grammar rules form tree-strings with all the mids
	 * @param rule
	 * @param child
	 * @return the constructed tree string
	 */
	private String getUnaryTree(Rule rule, String child) {
		StringBuilder sb = new StringBuilder();
		StringBuilder rightBraces = new StringBuilder("]");
		sb.append("[");
		sb.append(rule.parent + " ");
		for (String mid : rule.mids) {
			sb.append("[");
			sb.append(mid + " ");
			rightBraces.append("]");
		}
		sb.append(child);
		sb.append(rightBraces);
		return sb.toString();
	}
	
	/**
	 * Parse the S-expression into brackets (for calculating the precision and recall)
	 * @param sentence	like "Nominal-[1, 2), NP-[0, 2), ..."
	 * @param sExpr		like "[S [NP [Pronoun I]] [VP [Verb book] ..."
	 * @return list of brackets
	 */
	public List<Bracket> parseConstituency(String sentence, String sExpr) {
		List<Bracket> bracs = new ArrayList<Bracket>();
		int[] index = new int[1];
		int[] wordIndex = new int[] {0};
		String[] words = sentence.toLowerCase().split("\\s+");
		parseConstituencyRecur(sExpr, words, index, wordIndex, bracs);
		return bracs;
	}
	private void parseConstituencyRecur (String expr, String[] words, int[] ind, int[] wordInd, List<Bracket> bracs) {
		while (expr.charAt(ind[0]) == ' ') { // remove the leading spaces
			ind[0]++;
		}
		int crtWordInd = wordInd[0];
		int crtInd = ind[0];
		char crtChar = expr.charAt(crtInd);
		if (crtChar == '[') {
			ind[0] = crtInd + 1;
			while (ind[0] < expr.length()) {
				if (expr.charAt(ind[0]) != '[' && expr.charAt(ind[0]) != ']') {
					ind[0]++;
				}
				else if (expr.charAt(ind[0]) == '[') {
					parseConstituencyRecur(expr, words, ind, wordInd, bracs);
				}
				else if (expr.charAt(ind[0]) == ']') {
					String inside = expr.substring(crtInd + 1, ind[0]);
					int firstLeftBracePos = inside.indexOf('[');
					if (-1 == firstLeftBracePos) { // base case
						String word = inside.trim().split("\\s+")[1];
						if (word.toLowerCase().equals(words[wordInd[0]])) {
							// word matched (not necessary check it, in fact)
							wordInd[0]++;
						}
						else { // for debug
							System.out.println("WRONG WORD INDEX: " + word + ", " + words[wordInd[0]]);
						}
					}
					else {
						String symbol = expr.substring(crtInd + 1, crtInd + firstLeftBracePos + 1).trim();
						bracs.add(new Bracket(symbol, crtWordInd, wordInd[0]));
					}
					ind[0]++;
					return; // do nothing but increment the global index and global word index.
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			
		// unit test
		CNFParser t = new CNFParser("data\\grammar.txt", "I book a flight to Houston");
		System.out.println(t.getCandidates());
//		List<String> mids = new ArrayList<>();
//		mids.add("AP");
//		mids.add("BP");
//		System.out.println(t.getUnaryTree(new Rule ("S", "Iam", null, mids, 0.02), "Iam"));
		System.out.println(t.parseConstituency("the flight includes a meal", "[S[NP[Det the][Nominal[Noun flight]]][VP[Verb includes][NP[Det a][Nominal[Noun meal]]]]]"));
	}

}
