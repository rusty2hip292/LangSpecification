package parser_old5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import parser.utils.Utils;

public abstract class ParserState {

	private final Parser_ParserStateChannel channel;
	private static int count = 0;
	protected String name;
	public ParserState(Parser_ParserStateChannel channel) {
		this.channel = channel;
		this.name = "" + count++;
	}
	public ParserState setName(String name) {
		this.name = name;
		return this;
	}
	public String name() {
		return this.name;
	}
	public abstract LinkedList<LinkedList<ParserState>> enter(IStreamer stream, Stack<AcceptingState> accepted, ILambda fail);
	public void compileParser() {
		this.channel.compile();
	}
	protected abstract void removeIndirectRecursion();
	protected abstract void removeDirectRecursion();
	public String toString() {
		return this.name();
	}
	protected String toString(String name) {
		return name;
	}
}

class AcceptingState extends ParserState {

	interface IBuildLanguageObjectStack {
		void build(Stack<LanguageObject<?>> stack);
	}

	private final IBuildLanguageObjectStack builder;
	public AcceptingState(Parser_ParserStateChannel channel, IBuildLanguageObjectStack builder) {
		super(channel);
		this.builder = builder;
	}
	public final LinkedList<LinkedList<ParserState>> enter(IStreamer stream, Stack<AcceptingState> accepted, ILambda fail) {
		this.enter(accepted);
		return null;
	}
	private final void enter(Stack<AcceptingState> accepted) {
		accepted.push(this);
	}
	public final void build(Stack<LanguageObject<?>> stack) {
		this.builder.build(stack);
	}	

	protected void removeIndirectRecursion() { }
	protected void removeDirectRecursion() { }
	public String name() {
		return "[" + this.name + "]";
	}
}

abstract class RuleState extends ParserState {

	public RuleState(Parser_ParserStateChannel channel) {
		super(channel);
	}

	protected abstract void removeIndirectRecursion(HashMap<ParserState, LinkedList<LinkedList<ParserState>>> replaceMap, HashSet<ParserState> visited);
}

abstract class TerminalRuleState extends RuleState {

	interface ITerminalLambda {
		AcceptingState terminal(IStreamer stream, ILambda fail);
	}

	private final ITerminalLambda accepting;

	private TerminalRuleState(Parser_ParserStateChannel channel, ITerminalLambda accepting) {
		super(channel);
		this.accepting = accepting;
	}

	public LinkedList<LinkedList<ParserState>> enter(IStreamer stream, Stack<AcceptingState> accepted, ILambda fail) {
		AcceptingState as = this.accepting.terminal(stream, fail);
		if(as == null) {
			return null;
		}
		return Utils.link(Utils.link(as));
	}

	protected void removeIndirectRecursion() { }
	protected void removeDirectRecursion() { }
	protected void removeIndirectRecursion(HashMap<ParserState, LinkedList<LinkedList<ParserState>>> replaceMap, HashSet<ParserState> visited) {
		visited.add(this);
	}
}

class NonTerminalRuleState extends RuleState {

	protected LinkedList<LinkedList<ParserState>> options;

	public NonTerminalRuleState(Parser_ParserStateChannel channel) {
		super(channel);
		this.options = new LinkedList<LinkedList<ParserState>>();
	}
	public NonTerminalRuleState(Parser_ParserStateChannel channel, LinkedList<LinkedList<ParserState>> options) {
		super(channel);
		this.options = options;
	}

	public final LinkedList<LinkedList<ParserState>> enter(IStreamer stream, Stack<AcceptingState> accepted, ILambda fail) {
		return options;
	}

	protected void removeDirectRecursion() { }
	protected void removeIndirectRecursion() {
		this.removeIndirectRecursion(new HashMap<ParserState, LinkedList<LinkedList<ParserState>>>(), new HashSet<ParserState>());
	}
	protected void removeIndirectRecursion(HashMap<ParserState, LinkedList<LinkedList<ParserState>>> replaceMap, HashSet<ParserState> visited) {
		if(visited.contains(this)) {
			replaceMap.put(this, this.options);
			return;
		}
		visited.add(this);
		for(int i = 0; i < this.options.size(); i++) {
			LinkedList<ParserState> option = this.options.get(i);
			for(int j = 0; j < option.size(); j++) {
				ParserState s = option.get(j);
				//System.out.println(s.name());
				//System.out.println(this.options);
				try {
					Thread.sleep(100);
				}catch(Exception e) { }
				if(s instanceof RuleState) {
					RuleState r = (RuleState) s;
					if(r != this && visited.contains(r)) {
						if(replaceMap.containsKey(r)) {
							this.options = parser.utils.Utils.insert(this.options, i, j, replaceMap.get(r));
						}
					}else {
						//System.out.println("visited=" + visited);
						//System.out.println("r=" + r.getClass());
						r.removeIndirectRecursion(replaceMap, visited);
					}
					break;
				}
			}
		}
	}

	private String optionToString(String name, LinkedList<ParserState> option) {
		StringBuffer sb = new StringBuffer();
		sb.append(name + " :");
		for(ParserState ps : option) {
			sb.append(" " + ps.name());
		}
		return sb.toString();
	}
	protected String toString(String name) {
		boolean line = false;
		StringBuffer sb = new StringBuffer();
		for(LinkedList<ParserState> option : this.options) {
			String s = optionToString(name, option);
			if(line && !s.equals("")) {
				sb.append("\n");
			}
			sb.append(s);
			line = true;
		}
		return sb.toString();
	}
	public String toString() {
		return toString(this.name());
	}
}

class WrapperRule extends NonTerminalRuleState {

	protected WrapperRule(Parser_ParserStateChannel channel) {
		super(channel);
	}
	public RuleState set(RuleState rule) {
		this.options = Utils.link(Utils.link(rule));
		return this;
	}
}
