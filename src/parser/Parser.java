package parser;

public class Parser<T> {

	private final ParseFactory<T> factory;
	public Parser(ParseFactory<T> factory) {
		this.factory = factory;
	}
	
	T parse(stream.IStream stream) {
		return null;
	}
	
	T parse(String stream) {
		return parse(new stream.Stream(stream).ignoreWhitespace());
	}
}
