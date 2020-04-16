package parser_old;

import java.util.Stack;

public class Streamer implements IStreamer {

	private int index = 0;
	private long id = 1;
	private Stack<Integer> fail_indexes = new Stack<Integer>();
	private final String stream;
	
	public Streamer(String toStream) {
		stream = toStream;
	}

	public long id() {
		if(id < 0) {
			id = 0;
		}
		return id;
	}
	public boolean hasNext() {
		return index < stream.length();
	}
	public char next() {
		id++;
		return stream.charAt(index++);
	}
	public void setFailPoint() {
		fail_indexes.push(index);
	}
	public void fail() {
		index = fail_indexes.pop();
		id++;
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
