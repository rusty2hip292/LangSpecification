package parser;

public class Fragment implements LangObject {

	public final void enter() { }
	public final void visit() { }
	public final void exit() { }
	
	public final String text;
	public Fragment(String grabbed) {
		this.text = grabbed;
	}
}
