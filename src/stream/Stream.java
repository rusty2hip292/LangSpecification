package stream;

import java.util.HashSet;
import java.util.Stack;

public class Stream implements IStream {

	private final String stream;
	private int index = 0;
	private Stack<Integer> checkpoints = new Stack<Integer>();
	private HashSet<Character> ignore = new HashSet<Character>();
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
		char c = stream.charAt(index);
		if(ignore.contains(c)) {
			do {
				index++;
				if(!hasNext()) {
					return (char) 0;
				}
				c = stream.charAt(index);
			}while(ignore.contains(c));
			index--;
			return (char) 0;
		}
		return c;
	}

	public char next() {
		char c = peek();
		index++;
		peek();
		return c;
	}

	public boolean hasNext() {
		return index < stream.length();
	}

	public Stream ignore(char... chars) {
		for(char c : chars) {
			ignore.add(c);
		}
		if(peek() == (char) 0) {
			next();
		}
		return this;
	}
	
	public Stream ignoreWhitespace() {
		return ignore(' ', '\r', '\n', '\t');
	}
	
	public static void main(String[] args) {
		Stream stream = new Stream("    this is a test   testing some more   ").ignore(' ');
		while(stream.hasNext()) {
			char c = stream.next();
			switch(c) {
			case(0):
				System.out.println();
				break;
			default:
				System.out.print(c);
			}
		}
	}
}
