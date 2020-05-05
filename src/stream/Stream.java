package stream;

import java.util.HashSet;
import java.util.Stack;

public class Stream implements IStream {

	private final String stream;
	private int index = 0;
	private Stack<Integer> checkpoints = new Stack<Integer>();
	public Stream(String stream) {
		this.stream = stream;
	}
	
	public void fail() {
		index = checkpoints.pop();
	}

	public void checkpoint() {
		checkpoints.push(index);
	}

	public char peek() {
		if(!hasNext()) {
			return (char) 0;
		}
		return stream.charAt(index);
	}

	public char next() {
		char c = peek();
		index++;
		return c;
	}

	public boolean hasNext() {
		return index < stream.length();
	}
}
