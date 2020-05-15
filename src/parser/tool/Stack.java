package parser.tool;

public class Stack<S, T> {

	private S below;
	private T top;
	
	private static Stack<None, None> none = new Stack<None, None>(null, null);
	
	public static Stack<None, None> stack() {
		return none;
	}
	public <Type> Stack<Stack<S, T>, Type> push(Type t) {
		return new Stack<Stack<S, T>, Type>(this, t);
	}
	public Tuple<S, T> pop() {
		return new Tuple<S, T>(this.below, this.top);
	}
	
	private Stack(S s, T t) {
		this.top = t;
		this.below = s;
	}
}
