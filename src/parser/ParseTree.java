package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import parser.ParserState.ITerminalLambda;
import parser.istreamer.IStreamer;
import parser.utils.Utils;

abstract class ParseTree<T> {

	private static int count = 0;
	protected final int id = count++;

	protected static class ParseState<T> {
		public final IStreamer stream;
		private final Stack<Stack<ParseTree<T>>> parse;
		private final Stack<RuleGenerator.ITBuilder<T>> accepted;
		private final Stack<Integer> acceptedCount = new Stack<Integer>();
		protected ParseState(IStreamer stream) {
			this.stream = stream;
			this.parse = new Stack<Stack<ParseTree<T>>>();
			this.accepted = new Stack<RuleGenerator.ITBuilder<T>>();
		}
		public Stack<RuleGenerator.ITBuilder<T>> parse(ParseTree<T> entry) {
			Stack<ParseTree<T>> init = new Stack<ParseTree<T>>();
			init.push(entry);
			this.parse.push(init);
			return this.parse();
		}
		private Stack<RuleGenerator.ITBuilder<T>> parse() {
			while(this.parse.size() > 0) {
				try {
					Stack<ParseTree<T>> top = this.parse.peek();
					if(top.isEmpty() && this.stream.hasNext()) {
						this.fail();
					}else if(top.isEmpty()) {
						return this.results();
					}else {
						top.pop().parse(this);
					}
				}catch(Exception e) {
					e.printStackTrace();
					this.fail();
				}
			}
			return null;
		}
		protected Stack<RuleGenerator.ITBuilder<T>> results() {
			if(stream.hasNext()) {
				return null;
			}
			return accepted;
		}
		protected void branch(ParseTree<T> fail, ParseTree<T> next) {
			if(fail == null) {
				if(next != null) {
					this.parse.peek().push(next);
				}
			}else  {
				this.stream.setFailPoint();
				acceptedCount.push(this.accepted.size());
				Stack<ParseTree<T>> fail_state = this.parse.peek();
				Stack<ParseTree<T>> next_state = (Stack<ParseTree<T>>) fail_state.clone();
				fail_state.push(fail);
				if(next != null) {
					next_state.push(next);
				}
				this.parse.push(next_state);
			}
		}
		protected void fail() {
			if(parse.isEmpty()) {
				return;
			}
			parse.pop();
			int count = acceptedCount.isEmpty() ? 0 : acceptedCount.pop();
			while(accepted.size() > count) {
				accepted.pop();
			}
			this.stream.fail();
		}
		protected void accept(RuleGenerator.ITBuilder<T> t) {
			this.accepted.push(t);
		}
	}

	protected static <T> ParseTree<T> get(ParserState<T> ps, HashMap<ParserState<T>, ParseTree<T>> visited) {
		ParseTree<T> tree = visited.get(ps);
		if(tree != null) {
			return tree;
		}else {
			if(ps instanceof TerminalState) {
				TerminalParseTree<T> t = new TerminalParseTree<T>();
				visited.put(ps, t);
				t.make((TerminalState<T>) ps, visited);
				return t;
			}else if(ps instanceof RuleState) {
				SubParseTree<T> r = new SubParseTree<T>();
				visited.put(ps, r);
				r.make(((RuleState<T>) ps).iterator(), visited);
				return r;
			}else {
				throw new IllegalArgumentException("Failed to account for class type " + ps.getClass().toString());
			}
		}
	}
	protected static <T> ParseTree<T> tree(ParserState<T> ps) {
		HashMap<ParserState<T>, ParseTree<T>> map = new HashMap<ParserState<T>, ParseTree<T>>();
		SubParseTree<T> tree = new SubParseTree<T>();
		map.put(ps, tree);
		tree.make(ps.iterator(), map);
		return tree;
	}
	public final Stack<RuleGenerator.ITBuilder<T>> parse(IStreamer stream) {
		ParseState<T> state = new ParseState<T>(stream);
		return state.parse(this);
	}
	protected abstract void parse(ParseState<T> state);
	protected ParseTree<T> simplify() {
		return simplify(new HashSet<ParseTree<T>>());
	}
	protected abstract ParseTree<T> simplify(HashSet<ParseTree<T>> visited);
	protected static <T> ParseTree<T> simplify(ParseTree<T> tree, HashSet<ParseTree<T>> visited) {
		return tree == null ? null : (visited.add(tree) ? tree.simplify(visited) : tree);
	}

	public String toString() {
		return "" + this.id;
	}
	protected abstract String treeString(HashSet<Integer> visited);
}
class TerminalParseTree<T> extends ParseTree<T> {
	private ITerminalLambda<T> lambda;
	protected void make(TerminalState<T> t, HashMap<ParserState<T>, ParseTree<T>> visited) {
		lambda = t.accepting;
	}
	protected void parse(ParseState<T> state) {
		RuleGenerator.ITBuilder<T> t = lambda.accept(state.stream);
		if(t == null) {
			state.fail();
			return;
		}
		state.accept(t);
	}
	protected ParseTree<T> simplify(HashSet<ParseTree<T>> visited) {
		visited.add(this);
		return this;
	}

	protected String treeString(HashSet<Integer> visited) {
		visited.add(this.id);
		return "[" + this.id + "]";
	}
}
class SubParseTree<T> extends ParseTree<T> {
	protected ParseTree<T> simplify(HashSet<ParseTree<T>> visited) {
		this.me = ParseTree.simplify(this.me, visited);
		this.next = ParseTree.simplify(this.next, visited);
		this.fail = ParseTree.simplify(this.fail, visited);
		if(this.me == null && this.next != null) {
			throw new IllegalArgumentException("No current state, but has next state.");
		}
		if(this.me == null) {
			this.me = this.fail;
			this.fail = null;
		}
		if(this.me == null) {
			return null;
		}
		if(this.fail == null && this.next == null) {
			return this.me;
		}
		return this;
	}
	private ParseTree<T> me = null, next = null, fail = null;
	protected void make(Iterable<LinkedList<ParserState<T>>> itr, HashMap<ParserState<T>, ParseTree<T>> visited) {
		SubParseTree<T> s1 = null;
		for(LinkedList<ParserState<T>> list : itr) {
			SubParseTree<T> s2 = SubParseTree.list(list.iterator(), visited);
			s1 = SubParseTree.or(s1, s2);
		}
		this.me = s1;
	}
	private static <T> SubParseTree<T> or(ParseTree<T> t1, ParseTree<T> t2) {
		SubParseTree<T> tree = new SubParseTree<T>();
		if(t1 == null || t2 == null) {
			if(t1 == null && t2 == null) {
			}else if(t1 == null) {
				tree.me = t2;
			}else {
				tree.me = t1;
			}
			return tree;
		}
		if(t1 instanceof SubParseTree && t2 instanceof SubParseTree) {
			SubParseTree<T> s1 = (SubParseTree<T>) t1, s2 = (SubParseTree<T>) t2;
			if(s1.me == s2.me) {
				tree.me = s1.me;
				tree.next = or(s1.next, s2.next);
				return tree;
			}
		}
		tree.me = t1;
		tree.fail = t2;
		return tree;
	}
	private static <T> SubParseTree<T> list(Iterator<ParserState<T>> itr, HashMap<ParserState<T>, ParseTree<T>> visited) {
		if(!itr.hasNext()) {
			return null;
		}
		SubParseTree<T> tree = new SubParseTree<T>();
		tree.me = ParseTree.get(itr.next(), visited);
		tree.next = list(itr, visited);
		return tree;
	}
	protected void parse(ParseState<T> state) {
		state.branch(this.fail, this.next);
		state.branch(null, this.me);
	}
	protected String treeString(HashSet<Integer> visited) {
		if(visited.add(this.id)) {
			String left = (this.fail == null) ? "(" : String.format("((%s) <= ", this.fail.treeString(visited));
			String right = (this.next == null) ? ")" : String.format(" => (%s))", this.next.treeString(visited));
			String result = left + String.format("(%s-(%s))", this.id, this.me.treeString(visited));
			return result;
		}
		return "(" + this.id + ")";
	}
}