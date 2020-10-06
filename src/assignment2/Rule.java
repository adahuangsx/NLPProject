package assignment2;

import java.util.ArrayList;
import java.util.List;

public class Rule {
	String parent;
	String left;
	String right;
	List<String> mids = new ArrayList<>();
	double prob;
	
	public Rule(String p, String l, String r, double prob) {
		parent = p;
		left = l;
		right = r;
		this.prob = prob;
	}
	public Rule(String p, String l, String r, List<String> mids, double prob) {
		parent = p;
		left = l;
		right = r;
		this.mids.addAll(mids);
		this.prob = prob;
	}
	public Rule(String p, String l, String r, String mid, double prob) {
		parent = p;
		left = l;
		right = r;
		this.mids.add(mid);
		this.prob = prob;
	}
	
	@Override
	public String toString() {
		return parent + " -> " + left + ", " + right + " [" + prob + "]" + " mids: " + mids.toString();
	}
}
