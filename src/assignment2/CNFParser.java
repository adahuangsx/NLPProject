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
	}

	PCFGReader readInRules;
	String[] words;
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
	private boolean accepted = false;
	
	public CNFParser() {} // for test
	
	public CNFParser (String filePath, String sentence) throws IOException {
		this.readInRules = new PCFGReader(filePath);
		this.words = sentence.split("\\s+");
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
		this.accepted = checkAccepted();
		
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
									if (gram.parent.startsWith(this.readInRules.MID_RULE_PREFIX)) {
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
	
	private boolean checkAccepted() {
		int num = this.words.length;
		for (Cell finalCell : this.CKYTable.get(num - 1).get(0)) {
			if (finalCell.symbol.equals("S")) {
				return true;
			}
		}
		return false;
	}
	
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			
		// unit test
		CNFParser t = new CNFParser();
		List<String> mids = new ArrayList<>();
		mids.add("AP");
		mids.add("BP");
		System.out.println(t.getUnaryTree(new Rule ("S", "Iam", null, mids, 0.02), "Iam"));
	}

}
