package parser;

import utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class ParseState {
	
	protected final String name;
	public ParseState(String name) {
		this.name = name;
	}
	public String toString() {
		return this.name;
	}
	
	private static ParseTree.ParseTreeNode _makeNode(ParseState state, HashMap<ParseState, ParseTree.ParseTreeNode> nodes) {
		ParseTree.ParseTreeNode node = nodes.get(state);
		if(node != null) {
			return node;
		}
		if(state instanceof ParseState.TerminalParseState) {
			ParseTree.ParseTreeNode.TerminalParseTreeNode terminal = new ParseTree.ParseTreeNode.TerminalParseTreeNode(state.name);
			terminal.me = ((ParseState.TerminalParseState) state).lambda;
			nodes.put(state, terminal);
			return terminal;
		}else {
			ParseTree.ParseTreeNode.NonTerminalParseTreeNode nonterminal = new ParseTree.ParseTreeNode.NonTerminalParseTreeNode(state.name);
			nodes.put(state, nonterminal);
			nonterminal.me = makeNode(state.removeRecursion(), nodes);
			return nonterminal;
		}
	}
	private static ParseTree.ParseTreeNode _makeNode(List<ParseState> rule, HashMap<ParseState, ParseTree.ParseTreeNode> nodes) {
		ParseTree.ParseTreeNode.NonTerminalParseTreeNode node = new ParseTree.ParseTreeNode.NonTerminalParseTreeNode();
		switch(rule.size()) {
		default:
			node.next = _makeNode(rule.subList(1, rule.size()), nodes);
		case(1):
			node.me = _makeNode(rule.get(0), nodes);
		case(0):
			return node;
		}
	}
	private static ParseTree.ParseTreeNode makeNode(List<LinkedList<ParseState>> rules, HashMap<ParseState, ParseTree.ParseTreeNode> nodes) {
		ParseTree.ParseTreeNode.NonTerminalParseTreeNode node = new ParseTree.ParseTreeNode.NonTerminalParseTreeNode();
		switch(rules.size()) {
		default:
			node.fail = makeNode(rules.subList(1, rules.size()), nodes);
		case(1):
			node.me = _makeNode(rules.get(0), nodes);
		case(0):
			return node;
		}
	}
	public ParseTree.ParseTreeNode compile() {
		return makeNode(this.removeRecursion(), new HashMap<ParseState, ParseTree.ParseTreeNode>());
	}
	
	protected abstract LinkedList<LinkedList<ParseState>> removeRecursion();
	
	public static class TerminalParseState extends ParseState {

		protected LinkedList<LinkedList<ParseState>> removeRecursion() {
			return Utils.link(Utils.link(this));
		}

		protected final Parser.IStreamToStackLambda lambda;
		public TerminalParseState(String name, Parser.IStreamToStackLambda lambda) {
			super(name);
			this.lambda = lambda;
		}
	}

	public static class NonTerminalParseState extends ParseState {
		
		private boolean is_compiled = false;
		private LinkedList<LinkedList<ParseState>> compiled_rule;
		
		protected LinkedList<LinkedList<ParseState>> removeRecursion() {
			return removeRecursion(new HashSet<ParseState>());
		}
		private LinkedList<LinkedList<ParseState>> removeRecursion(HashSet<ParseState> visited) {
			if(!this.is_compiled) {
				this.compiled_rule = removeDirectRecursion(removeIndirectRecursion(visited));
				this.is_compiled = true;
			}
			return this.compiled_rule;
		}
		
		private LinkedList<LinkedList<ParseState>> removeIndirectRecursion(HashSet<ParseState> visited) {
			visited.add(this);
			LinkedList<LinkedList<ParseState>> no_indirect = new LinkedList<LinkedList<ParseState>>();
			for(LinkedList<ParseState> list : this.states) {
				if(list.size() == 0) {
					no_indirect.add(list);
					continue;
				}
				ParseState ps = list.getFirst();
				if(visited.contains(ps) || ps instanceof ParseState.TerminalParseState) {
					no_indirect.add(list);
					continue;
				}
				for(LinkedList<ParseState> unwrap : ((ParseState.NonTerminalParseState) ps).removeRecursion(visited)) {
					if(list.size() > 1) {
						LinkedList<ParseState> temp = new LinkedList<ParseState>();
						temp.addAll(unwrap);
						temp.addAll(list.subList(1, list.size()));
						no_indirect.add(temp);
					}else {
						no_indirect.add(unwrap);
					}
				}
			}
			return no_indirect;
		}
		private LinkedList<LinkedList<ParseState>> removeDirectRecursion(LinkedList<LinkedList<ParseState>> no_indirect) {
			NonTerminalParseState base = new NonTerminalParseState("b_" + this.name);
			NonTerminalParseState after_base = new NonTerminalParseState("+" + this.name);
			NonTerminalParseState repeat = new NonTerminalParseState("r_" + this.name);
			repeat.newRule(); repeat.add(after_base); repeat.add(repeat); repeat.newRule();
			LinkedList<LinkedList<ParseState>> no_direct = new LinkedList<LinkedList<ParseState>>();
			for(LinkedList<ParseState> list : no_indirect) {
				if(list.isEmpty() || list.getFirst() != this) {
					base.states.add(list);
					no_direct.add(list);
				}else if(list.size() > 1) {
					after_base.newRule();
					LinkedList<ParseState> temp = new LinkedList<ParseState>();
					temp.add(base); temp.add(repeat);
					for(var v : list.subList(1, list.size())) {
						after_base.add(v);
						temp.add(v);
					}
					no_direct.add(temp);
				}
			}
			System.out.println(no_direct);
			System.out.println(repeat.states);
			System.out.println(after_base.states);
			System.out.println(base.states);
			return no_direct;
		}
		
		private LinkedList<LinkedList<ParseState>> states = new LinkedList<LinkedList<ParseState>>();
		public NonTerminalParseState(String name) {
			super(name);
		}
		
		public void newRule() {
			this.is_compiled = false;
			states.add(new LinkedList<ParseState>());
		}
		public void add(ParseState state) {
			this.is_compiled = false;
			states.getLast().add(state);
		}
	}
}
