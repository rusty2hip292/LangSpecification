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
		tree.make(ps.compiled(), map);
		return tree;
	}
	public Stack<RuleGenerator.ITBuilder<T>> parse(IStreamer stream) {
		Stack<RuleGenerator.ITBuilder<T>> stack = new Stack<RuleGenerator.ITBuilder<T>>();
		if(this.parse(stream, stack)) {
			return stack;
		}
		return null;
	}
	protected abstract boolean parse(IStreamer stream, Stack<RuleGenerator.ITBuilder<T>> ast);
	protected ParseTree<T> simplify() {
		return simplify(new HashSet<ParseTree<T>>());
	}
	protected abstract ParseTree<T> simplify(HashSet<ParseTree<T>> visited);
	protected static <T> ParseTree<T> simplify(ParseTree<T> tree, HashSet<ParseTree<T>> visited) {
		return tree == null ? null : (visited.add(tree) ? tree.simplify(visited) : tree);
	}
	
	public String toString() {
		return this.id + " : " + treeString(new HashSet<Integer>());
	}
	protected abstract String treeString(HashSet<Integer> visited);
}
class TerminalParseTree<T> extends ParseTree<T> {
	private ITerminalLambda<T> lambda;
	protected void make(TerminalState<T> t, HashMap<ParserState<T>, ParseTree<T>> visited) {
		lambda = t.accepting;
	}
	protected boolean parse(IStreamer stream, Stack<RuleGenerator.ITBuilder<T>> stack) {
		RuleGenerator.ITBuilder<T> t = lambda.accept(stream);
		if(t != null) {
			stack.push(t);
			return true;
		}
		return false;
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
		//if(true) {
		//	return this;
		//}
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
	protected boolean parse(IStreamer stream, Stack<RuleGenerator.ITBuilder<T>> stack) {
		int size = stack.size();
		if(fail != null) {
			stream.setFailPoint();
		}
		if(me.parse(stream, stack)) {
			return (next == null) ? true : next.parse(stream, stack);
		}
		if(fail != null) {
			stream.fail();
			while(stack.size() > size) {
				stack.pop();
			}
			return fail.parse(stream, stack);
		}
		return false;
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