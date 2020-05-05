package parser;

import java.util.LinkedList;
import java.util.Stack;
import parser.utils.Utils;

public class Parser<T extends LangObject> {
	
	public T parse(String stream, LangObjectParser<T> parser) {
		return parse(new stream.Stream(stream), parser);
	}
	public T parse(stream.IStream stream, LangObjectParser<T> parser) {
		Stack<LangObjectBuilder<?>> stack = new Stack<LangObjectBuilder<?>>();
		if(parse(stream, Utils.link(parser), stack)) {
			Stack<LangObject> objs = new Stack<LangObject>();
			for(LangObjectBuilder<?> builder : stack) {
				builder.build(objs);
			}
			if(objs.size() == 1) {
				return (T) objs.pop();
			}
		}
		return null;
	}
	private boolean parse(stream.IStream stream, LinkedList<LangObjectParser<?>> todo, Stack<LangObjectBuilder<?>> built) {
		while(todo.size() > 0) {
			LangObjectParser<?> p = todo.removeFirst();
			int size = built.size();
			LangObjectParser<?> fail_state = p.fail_state();
			if(fail_state != null) {
				stream.checkpoint();
			}
			if(parse(stream, p.requires(), built) && built.push(p.maker(stream)) != null) {
				if(fail_state == null || parse(stream, todo, built)) {
					continue;
				}
			}
			while(built.size() > size) {
				built.pop();
			}
			if(fail_state != null) {
				stream.fail();
				todo.addFirst(fail_state);
			}else {
				return false;
			}
		}
		return true;
	}
}
