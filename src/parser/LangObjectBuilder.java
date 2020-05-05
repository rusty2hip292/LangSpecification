package parser;

import java.util.Stack;

public interface LangObjectBuilder<T extends LangObject> {

	void build(Stack<LangObject> stack);
}
