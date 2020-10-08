package assignment2;

/**
 * Used for calculating precision and recall
 * form: "S-(7, 9)". 
 * Means S from word [7, 9).
 * @author Sixuan Huang
 *
 */
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