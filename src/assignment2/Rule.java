package assignment2;

public class Rule {
	String parent;
	String left;
	String right;
	double prob;
	
	public Rule(String p, String l, String r, double prob) {
		parent = p;
		left = l;
		right = r;
		this.prob = prob;
	}
	
	@Override
	public String toString() {
		return parent + " -> " + left + ", " + right + " [" + prob + "]";
	}
}
