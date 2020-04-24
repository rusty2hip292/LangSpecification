package parser;

import java.util.Stack;

import langobject.LangObject;

public class Parser<T extends LangObject> {

	public interface IStreamToStackLambda {
		IStackLambda tokenize(stream.IStreamer input_stream);
	}
	public interface IStackLambda {
		void parse(Stack<LangObject> stack);
	}
	
	private final ParseTree tree = new ParseTree();
	private Parser() { }
	
	public T parse(String stream) {
		return parse(new parser.Streamer(stream));
	}
	public T parse(stream.IStreamer stream) {
		return (T) this.tree.parse(stream);
	}
	public String toString() {
		return this.tree.toString();
	}
	
	public static <T extends LangObject> Parser<T> parser(T prototype) {
		Parser p = new Parser();
		p.tree.build(prototype);
		return p;
	}
}
