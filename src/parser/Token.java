package parser;

public abstract class Token<T extends Token<T>> implements LangObject {
	
	public final void enter() { }
	public final void exit() { }
	
	public final String text;
	public Token(String grabbed) {
		this.text = grabbed;
	}
}
