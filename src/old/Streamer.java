package old;
import java.util.Stack;

public class Streamer {

	private final String toTokenize;
	private int index = 0;
	private long operation = 0;
	private Stack<TokenSlice> tokens = new Stack<TokenSlice>();
	
	public Streamer(String toTokenize) {
		this.toTokenize = toTokenize;
		tokens.push(new TokenSlice());
	}
	
	public Streamer fail(int index) {
		this.index = index;
		while(tokens.peek().start >= index) {
			tokens.pop();
		}
		tokens.push(new TokenSlice());
		return this;
	}
	
	public void endToken(StreamToJSON method) {
		TokenSlice ts = tokens.peek();
		ts.end = index;
		ts.method = method;
		tokens.push(new TokenSlice());
	}
	
	public interface StreamToJSON {
		String toJSON(String tokenContents);
	}
	
	private class TokenSlice {
		protected final int start;
		protected int end;
		protected StreamToJSON method;
		TokenSlice() {
			start = index;
		}
		protected String readToken() {
			return toTokenize.substring(start, end);
		}
	}
}
