package parser_old5;

import java.util.Stack;

public class Streamer implements IStreamer {

	private int index = 0;
	private Stack<Integer> fail_indexes = new Stack<Integer>();
	private final String stream;
	
	public Streamer(String toStream) {
		stream = toStream;
	}

	public String read(int start, int end) {
		return stream.substring(start, end);
	}
	public int index() {
		return index;
	}
	public boolean hasNext() {
		return index < stream.length();
	}
	public char next() {
		return stream.charAt(index++);
	}
	public void setFailPoint() {
		fail_indexes.push(index);
	}
	public void fail() {
		index = fail_indexes.pop();
	}
	
	public String toString() {
		if(index >= stream.length()) {
			return stream + "|";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(stream.substring(0, index));
		sb.append("|");
		sb.append(stream.substring(index));
		return sb.toString();
	}
}
