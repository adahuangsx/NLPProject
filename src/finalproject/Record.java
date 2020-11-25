package finalproject;

public class Record {

	String storyID;
	String inputSentce;
	String rightEnd;
	String wrongEnd;
	
	public Record (String id, String input, String right, String wrong) {
		this.storyID = id;
		this.inputSentce = input;
		this.rightEnd = right;
		this.wrongEnd = wrong;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(storyID).append("\n");
		sb.append(inputSentce).append("\n");
		sb.append(rightEnd).append("||");
		sb.append(wrongEnd).append("\n");
		return sb.toString();
	}
}
