package parser;

import java.util.LinkedList;
import java.util.Stack;

import stream.IStream;

public abstract class FragmentParser extends LangObjectParser<Fragment> {

	protected abstract String grab(IStream stream);
	
	public LangObjectParser<?> fail_state() {
		return null;
	}
	
	public LinkedList<LangObjectParser<?>> requires() {
		return parser.utils.Utils.link();
	}
	public LangObjectBuilder<Fragment> maker(IStream stream) {
		Fragment f = new Fragment(grab(stream));
		return (Stack<LangObject> stack) -> {
			return f;
		};
	}
}
