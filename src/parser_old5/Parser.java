package parser_old5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public class Parser {

	private final Parser_ParserStateChannel channel = new Parser_ParserStateChannel(this);
	private boolean compiled = false;
	private ArrayList<ParserState> states = new ArrayList<ParserState>();
	public Parser() { }
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean line = false;
		for(ParserState state : states) {
			String s = state.toString();
			if(line && !s.equals("")) {
				sb.append("\n");
			}
			sb.append(s);
			line = true;
		}
		return sb.toString();
	}
	
	private <T extends ParserState> T register(T t) {
		t = this.compiled ? null : t;
		this.states.add(t);
		return t;
	}
	public WrapperRule rule() {
		return register(new WrapperRule(channel));
	}
	public RuleState rule(LinkedList<LinkedList<ParserState>> options) {
		return register(new NonTerminalRuleState(channel, options));
	}
	public RuleState rule(LinkedList<ParserState>... options) {
		return register(new NonTerminalRuleState(channel, parser.utils.Utils.link(options)));
	}
	public RuleState rule(ParserState... option) {
		return register(new NonTerminalRuleState(channel, parser.utils.Utils.link(parser.utils.Utils.link(option))));
	}
	
	protected void compile() {
		if(this.compiled) {
			return;
		}
		for(ParserState state : states) {
			state.removeIndirectRecursion();
		}
		for(ParserState state : states) {
			state.removeDirectRecursion();
		}
		this.compiled = true;
	}
	public static LanguageObject parse(IStreamer stream, RuleState state) {
		Stack<AcceptingState> accepted = new Stack<AcceptingState>();
		Stack<Integer> accepted_size = new Stack<Integer>();
		accepted_size.push(0);
		Stack<Stack<ParserState>> paths = new Stack<Stack<ParserState>>();
		Stack<ParserState> stack = new Stack<ParserState>();
		stack.push(state);
		paths.push(stack);
		ILambda fail = () -> {
			fail(stream, paths, accepted, accepted_size);
		};
		try {
			while(true) {
				stack = paths.peek();
				if(stack.size() == 0) {
					break;
				}
				branch(stream, paths, stack.pop().enter(stream, accepted, fail), accepted, accepted_size);
			}
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		Stack<LanguageObject<?>> objs = new Stack<LanguageObject<?>>();
		try {
			for(AcceptingState as : (AcceptingState[]) accepted.toArray()) {
				as.build(objs);
			}
			if(objs.size() != 1) {
				throw new Exception("Accepting States failed to properly accept all language objects.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return objs.pop();
	}
	
	private static void fail(IStreamer stream, Stack<Stack<ParserState>> paths, Stack<AcceptingState> accepted, Stack<Integer> accepted_size) {
		stream.fail();
		int size = accepted_size.pop();
		while(accepted.size() > size) {
			accepted.pop();
		}
		paths.pop();
	}
	
	private static void branch(IStreamer stream, Stack<Stack<ParserState>> paths, LinkedList<LinkedList<ParserState>> next_options, Stack<AcceptingState> accepted, Stack<Integer> accepted_size) {
		if(next_options == null || next_options.size() == 0) {
			return;
		}
		if(next_options.size() == 1) {
			Iterator<ParserState> itr = next_options.get(0).descendingIterator();
			Stack<ParserState> top = paths.peek();
			while(itr.hasNext()) {
				top.push(itr.next());
			}
			return;
		}
		Iterator<LinkedList<ParserState>> litr = next_options.descendingIterator();
		while(litr.hasNext()) {
			Iterator<ParserState> itr = litr.next().descendingIterator();
			Stack<ParserState> top = paths.peek();
			while(itr.hasNext()) {
				top.push(itr.next());
			}
			stream.setFailPoint();
			accepted_size.push(accepted.size());
		}
	}
}

class Parser_ParserStateChannel {
	
	private final Parser parser;
	public Parser_ParserStateChannel(Parser parser) {
		this.parser = parser;
	}
	public void compile() {
		this.parser.compile();
	}
}
