package parser.tool;

public interface LangBuilder<S extends Stack<?, ?>, L> {
	<T1, T2> Stack<Stack<T1, T2>, S> build(Stack<T1, T2> stack);
	<T> Stack<T, L> collapse(Stack<T, S> prev);
}
