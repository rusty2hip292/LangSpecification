package parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import parser.istreamer.IStreamer;
import parser.utils.Utils;

public abstract class ParserState<T> {
	
	interface ITerminalLambda<T> {
		RuleGenerator.ITBuilder<T> accept(IStreamer stream);
	}
	
	protected static <T> ITerminalLambda<T> toTerminalLambda(RuleGenerator.ITBuilder<T> t) {
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
		Iterator<LinkedList<ParserState<T>>> itr = this.getCompiledOptions().iterator();
		return () -> {
			return itr;
		};
	}
}
