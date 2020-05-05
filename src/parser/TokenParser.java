package parser;

import java.util.LinkedList;
import java.util.Stack;

import stream.IStream;

public abstract class TokenParser<T extends Token<T>> extends LangObjectParser<T> {

	protected abstract T make(String text);
	
	private static void combineFragments(Stack<LangObject> stack, StringBuffer sb) {
		if(stack.size() == 0) {
			return;
		}
		if(stack.peek() instanceof Fragment) {
			Fragment f = (Fragment) stack.pop();
			combineFragments(stack, sb);
			sb.append(f.text);
		}else {
			return;
		}
	}

	public LangObjectParser<?> fail_state() {
		return null;
	}
	
	protected abstract LinkedList<LangObjectParser<Fragment>> _requires();

	private LinkedList<LangObjectParser<?>> list = null;;
	public final LinkedList<LangObjectParser<?>> requires() {
		if(list == null) {
			list = new LinkedList<LangObjectParser<?>>();
			for(LangObjectParser<Fragment> f : _requires()) {
				list.add(f);
			}
		}
		return list;
	}

	public LangObjectBuilder<T> maker(IStream stream) {
		return (Stack<LangObject> stack) -> {
			StringBuffer sb = new StringBuffer();
			combineFragments(stack, sb);
			stack.push(make(sb.toString()));
		};
	}
}
