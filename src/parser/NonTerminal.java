package parser;

public abstract class NonTerminal<T extends NonTerminal<T>> implements LangObject {

	public final void visit() { }
}
