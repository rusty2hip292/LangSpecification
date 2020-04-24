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
			LinkedList<LinkedList<ParseState>> no_direct = new LinkedList<LinkedList<ParseState>>();
			NonTerminalParseState base = new NonTerminalParseState("b_" + this.name), recursive = new NonTerminalParseState("r_" + this.name), zmany_recursive = new NonTerminalParseState("rec_" + this.name);
			zmany_recursive.newRule(); zmany_recursive.add(recursive); zmany_recursive.add(zmany_recursive); zmany_recursive.newRule();
			for(LinkedList<ParseState> list : no_indirect) {
				if(!list.isEmpty() && list.getFirst() == this) {
					LinkedList<ParseState> cleaned = new LinkedList<ParseState>();
					cleaned.add(base); cleaned.add(zmany_recursive);
					recursive.newRule();
					for(int i = 1; i < list.size(); i++) {
						ParseState ps = list.get(i);
						cleaned.add(ps);
						recursive.add(ps);
					}
					no_direct.add(cleaned);
				}else {
					base.states.add(list);
					no_direct.add(list);
				}
			}
			System.out.println(no_direct);
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
