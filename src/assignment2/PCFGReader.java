package assignment2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PCFGReader {

	private static final String GRAMMAR_START = "Grammar";
	private static final String LEXICON_START = "Lexicon";
	private static final String ARROW = "->";
	private static final String MID_RULE_PREFIX = "@";
	
	private int ruleNum;
	
	List<Rule> grammars;
	List<Rule> lexicons;
	
	public PCFGReader() {
		ruleNum = 0;
	}
	
	public void readIn(String filePath) throws IOException {
		ruleNum = 0;
		List<Rule> grammarRules = new ArrayList<>();
		List<Rule> lexiconRules = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line = null;
		boolean isGrammar = false;
		boolean isLexicon = false;
		while ((line = reader.readLine()) != null) {
			if (line.equals(GRAMMAR_START)) {
				isGrammar = true;
				isLexicon = false;
			}
			else if (line.equals(LEXICON_START)) {
				isGrammar = false;
				isLexicon = true;
			}
			else {
				if (isGrammar && !isLexicon) {
					// read grammar rules
					grammarRules.addAll(parseGrammarRule(line.trim()));
				}
				else if (!isGrammar && isLexicon) {
					// read lexicon rules
					lexiconRules.addAll(parseLexiconRule(line.trim()));
				}
			}
		}
		grammarRules = transformAllBinary(grammarRules, lexiconRules);
		reader.close();
		printRules(grammarRules, lexiconRules);
		this.grammars = new ArrayList<>(grammarRules);
		this.lexicons = new ArrayList<>(lexiconRules);
	}
	
	private void printRules (List<Rule> grammars, List<Rule> lexicons) {
		System.out.println("Grammars: ");
		for (Rule each : grammars) {
			System.out.println(each);
		}
		System.out.println("Lexicons: ");
		for (Rule each : lexicons) {
			System.out.println(each);
		}
	}
	
	private List<Rule> transformAllBinary(List<Rule> grammars, List<Rule> lexicons) {
		while (true) {
			boolean settled = true;
			for (int i = 0; i < grammars.size(); i++) {
				// for each grammar rule
				Rule crt = grammars.get(i);
				if (crt.right != null) {
					continue;
				}
				// if unary
				boolean foundTheRule = false;
				for (int j = 0; j < grammars.size(); j++) {
					// to find a matched rule
					Rule theRule = grammars.get(j);
					if (crt.left.equals(theRule.parent)) {
						// matched
						settled = false; // means there are changes in rules, not settled.
						foundTheRule = true;
						if (theRule.right != null) {
							// find a binary matched rule
							List<String> mids = new ArrayList<>(theRule.mids);
							mids.add(theRule.parent);
							grammars.add(new Rule(crt.parent, theRule.left, theRule.right, mids, crt.prob * theRule.prob));
						}
						else {
							// not binary. so find a lexicon to connect [crt -- theRule -- theLex] chain
							boolean hasLex = false;
							for (Rule theLex : lexicons) {
								if (theRule.left.equals(theLex.parent)) {
									hasLex = true;
									List<String> mids = new ArrayList<>();
									mids.add(theRule.parent);
									mids.add(theLex.parent);
									grammars.add(new Rule(crt.parent, theLex.left, null, mids, crt.prob * theRule.prob * theLex.prob));
								}
							}
							if (!hasLex) {
								grammars.add(new Rule(crt.parent, theRule.left, null, theRule.parent, crt.prob * theRule.prob));
							}
						}
					}
				}
				if (!foundTheRule) { // connect to a lex directly
					for (Rule lex : lexicons) {
						if (crt.left.equals(lex.parent)) {
							foundTheRule = true;
							grammars.add(new Rule(crt.parent, lex.left, null, lex.parent, crt.prob * lex.prob));
						}
					}
				}
				if (foundTheRule) {
					// found a rule and successfully binarized it.
					grammars.remove(i);
				}
			}
			if (settled) {
				break;
			}
		}
		return grammars;
	}

	private List<Rule> parseGrammarRule(String line) {
		List<Rule> grammar = new ArrayList<>();
		line = line.replaceAll("\\s+", " ");
		int firstSpace = line.indexOf(' ');
		if (firstSpace != -1) {
			double prob = Double.parseDouble(line.substring(0, firstSpace));
			String rule = line.substring(firstSpace + 1);
			String[] parts = rule.split(ARROW);
			String parent = parts[0].trim();
			String children = parts[1].trim();
			String[] child = children.split(" ");
			int num = child.length;
			if (num == 1) {
				// unary
				grammar.add(new Rule(parent, child[0], null, prob));
			}
			else if (num == 2) {
				// binary
				grammar.add(new Rule(parent, child[0], child[1], prob));
			}
			else if (num > 2) {
				// more than binary. Say "S -> AP BP CP DP EP"
				String midRule = MID_RULE_PREFIX + ruleNum++;
				grammar.add(new Rule(midRule, child[num - 2], child[num - 1], 1.0)); // "@0 -> DP EP"
				for (int i = num - 3; i > 0; i--) {			// "@1 -> CP @0", "@2 -> BP @1", ...
					midRule = MID_RULE_PREFIX + ruleNum++;
					grammar.add(new Rule(midRule, child[i], MID_RULE_PREFIX + (num - 2), 1.0));
				}
				grammar.add(new Rule(parent, child[0], midRule, prob)); // "S -> AP @2"
			}
		}
		return grammar;
	}
	
	private List<Rule> parseLexiconRule(String line) {
		List<Rule> lexicon = new ArrayList<>();
		line = line.replaceAll("\\s+", " ");
		int firstSpace = line.indexOf(' ');
		if (firstSpace != -1) {
			double prob = Double.parseDouble(line.substring(0, firstSpace));
			String rule = line.substring(firstSpace + 1);
			String[] parts = rule.split(ARROW);
			String parent = parts[0].trim();
			String word = parts[1].trim();
			lexicon.add(new Rule (parent, word.toLowerCase(), null, prob));
		}
		return lexicon;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		// unit test
		PCFGReader t = new PCFGReader();
		List<Rule> grammars = t.parseGrammarRule("0.10 VP->Verb NP PP AP CP BP");
		System.out.println(grammars);
		List<Rule> lexicons = t.parseLexiconRule("0.60 Proper-Noun->Houston");
		System.out.println(lexicons);
		
		t.readIn("data\\grammar.txt");
	}

}
