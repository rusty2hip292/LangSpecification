package parser;

import java.util.Stack;

public interface Factory<T> {

	T make(Stack<Object> params);
}
