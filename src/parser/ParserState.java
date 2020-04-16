package parser;

import java.util.HashMap;
import java.util.LinkedList;
import parser.istreamer.IStreamer;
import parser.utils.Utils;

public abstract class ParserState<T> {
	
	interface ITerminalLambda<T> {
		T accept(IStreamer stream);
	}
	
	protected static <T> ITerminalLambda<T> toTerminalLambda(T t) {
		return (IStreamer stream) -> {
			return t;
		};
	}

	private final String name;
	public ParserState(String name) {
		this.name = name;
	}

	public final String toString() {
		return this.name;
	}

	protected abstract LinkedList<LinkedList<ParserState<T>>> getCompiledOptions();
	protected Iterable<LinkedList<ParserState<T>>> compiled() {
		LinkedList<LinkedList<ParserState<T>>> list = this.getCompiledOptions();
		return () -> {
			return list.iterator();
		};
	}
	protected abstract Iterable<LinkedList<ParserState<T>>> iterator();
	
}

class TerminalState<T> extends ParserState<T> {
	
	interface IReadStringFromStream {
		String read(IStreamer stream);
	}
	
	protected final ITerminalLambda<T> accepting;
	protected TerminalState(String name, ITerminalLambda<T> accepting) {
		super(name);
		this.accepting = accepting;
	}

	protected LinkedList<LinkedList<ParserState<T>>> getCompiledOptions() {
		return Utils.link(Utils.link(this));
	}
	
	protected Iterable<LinkedList<ParserState<T>>> iterator() {
		return this.compiled();
	}
}

class RuleState<T> extends ParserState<T> {
	
	public RuleState(String name) {
		super(name);
	}

	private LinkedList<LinkedList<ParserState<T>>> options = new LinkedList<LinkedList<ParserState<T>>>();
	
	private static String counter(HashMap<String, Integer> count_map, String str) {
		Integer i = count_map.get(str);
		if(i == null) {
			i = 0;
		}
		count_map.put(str, i + 1);
		return str + "_" + i;
	}

	public void addRule(T endState, ParserState<T>... option) {
		LinkedList<ParserState<T>> o = new LinkedList<ParserState<T>>();
		for(ParserState<T> ps : option) {
			o.add(ps);
		}
		o.add(new TerminalState<T>(">>" + this.toString(), ParserState.toTerminalLambda(endState)));
		options.add(o);
	}

	protected LinkedList<LinkedList<ParserState<T>>> getCompiledOptions() {
		return this.removeDirectLeftRecursion(this.removeIndirectLeftRecursion());
	}

	protected LinkedList<LinkedList<ParserState<T>>> removeIndirectLeftRecursion() {
		LinkedList<LinkedList<ParserState<T>>> temp = this.options;
		for(int i = 0; i < temp.size(); i++) {
			while(true) {
				// assume always at least one parser state,
				// valid because a terminal node is always added at the end of non terminal states
				ParserState<T> first = temp.get(i).get(0);
				if(first instanceof TerminalState) {
					break;
				}else if(first instanceof RuleState) {
					if(first == this) {
						break;
					}
					RuleState<T> ntps = (RuleState<T>) first;
					temp = Utils.insert(temp, i, 0, ntps.options);
				}else {
					throw new IllegalArgumentException("Failed to account for class type " + first.getClass().toString());
				}
			}
		}
		return temp;
	}

	private final LinkedList<LinkedList<ParserState<T>>> removeDirectLeftRecursion(LinkedList<LinkedList<ParserState<T>>> indirectRemoved) {
		LinkedList<LinkedList<ParserState<T>>> base = new LinkedList<LinkedList<ParserState<T>>>(), recursive = new LinkedList<LinkedList<ParserState<T>>>();
		for(LinkedList<ParserState<T>> list : indirectRemoved) {
			((list.get(0) == this) ? recursive : base).add(list);
		}
		for(int i = recursive.size() - 1; i >= 0; i--) {
			recursive = Utils.insert(recursive, i, 0, base);
		}
		return recursive;
	}
	
	protected Iterable<LinkedList<ParserState<T>>> iterator() {
		LinkedList<LinkedList<ParserState<T>>> list = this.options;
		return () -> {
			return list.iterator();
		};
	}
}

