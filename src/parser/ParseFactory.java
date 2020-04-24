package parser;

public abstract class ParseFactory<T> implements Factory<T> {

	public static <J> Factory<J> consec(ParseFactory<?>... factories) {
		return null;
	}
}
