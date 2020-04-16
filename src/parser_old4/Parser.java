package parser_old4;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import parser_old5.IStreamer;

public class Parser {

	private boolean compiled = true;
	private HashMap<String, Integer> states = new HashMap<String, Integer>();
	private ArrayList<ParserState> rules = new ArrayList<ParserState>();
	public ParserState rule(String name) {
		Integer i = states.get(name);
		if(i != null) {
			return rules.get(i);
		}else {
			compiled = false;
			int index = rules.size();
			ParserState r = new ParserState(name, index, this);
			states.put(name, index);
			rules.add(r);
			return r;
		}
	}
	public ParserState rule(Integer rule) {
		return rules.get(rule);
	}
	public ParserState rule() {
		compiled = false;
		int index = rules.size();
		ParserState r = new ParserState("", index, this);
		rules.add(r);
		return r;
	}

	public Parser() {
		
	}

	private Parser compile() {
		if(this.compiled) {
			return this;
		}
		// TODO
		return this;
	}

	private LinkedList<LinkedList<Integer>> state = new LinkedList<LinkedList<Integer>>();
	private Deque<Integer> stack() {
		return state.peek();
	}
	public void parse(IStreamer stream, ParserState rule) {
		this.compile();
		state = new LinkedList<LinkedList<Integer>>();
		LinkedList<Integer> initial = new LinkedList<Integer>();
		initial.add(rule.index);
		state.add(initial);
		try {
			while(true) {
				while(stack().size() > 0) {
					this.rules.get(stack().pop()).enter(stream, stack());
				}
				if(stream.hasNext()) {
					fail(stream);
				}else {
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void fail(IStreamer stream) {
		stream.fail();
		state.pop();
	}
	private void push(LinkedList<Integer> states) {
		push(stack(), states);
	}
	private void push(Deque<Integer> s, LinkedList<Integer> states) {
		Iterator<Integer> reverse = states.descendingIterator();
		while(reverse.hasNext()) {
			s.add(reverse.next());
		}
	}
	protected void branch(IStreamer stream, LinkedList<LinkedList<Integer>> options) {
		if(options.size() == 0) {
			return;
		}else if(options.size() == 1) {
			push(options.get(0));
		}
		LinkedList<Integer> enq_options[] = (LinkedList<Integer>[]) new LinkedList[options.size()];
		enq_options[0] = this.state.pop();
		for(int i = 1; i < enq_options.length; i++) {
			enq_options[i] = (LinkedList<Integer>) enq_options[0].clone();
		}
		Iterator<LinkedList<Integer>> reverse = options.descendingIterator();
		for(LinkedList<Integer> clone : enq_options) {
			push(clone, reverse.next());
			state.push(clone);
			stream.setFailPoint();
		}
	}

	public static void main(String[] args) {
		Parser p = new Parser();
		ParserState r1 = p.rule("top");
		r1.option("a1", "b1", "c1").option("a2", "b2", "c2");
		p.rule("a1").option().option("a2");
		p.rule("c2").option("here");
		System.out.println(p);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean needNewline = false;
		for(ParserState pr : rules) {
			if(needNewline) {
				sb.append("\n");
			}else {
				needNewline = true;
			}
			sb.append(pr.toString());
		}
		return sb.toString();
	}
}

//class TerminalParserState;

class ParserState {

	private static final int
	ZEROFEW		= 0,
	ZEROMANY	= 1,
	ONEFEW		= 2,
	ONEMANY		= 3,
	WANTEDOPT	= 4,
	UNWANTEDOPT	= 5,
	// keep this as the last value
	SIZE		= 6;
	private final ParserState[] modified = new ParserState[SIZE];

	private ParserState modify(int type) {
		if(type < 0 || type >= modified.length) {
			throw new IllegalArgumentException("Invalid modifier requested: " + type);
		}
		ParserState mod = modified[type];
		if(mod != null) {
			return mod;
		}
		switch(type) {
		case(ZEROFEW):
			// TODO
			break;
		case(ZEROMANY):
			break;
		case(ONEFEW):
			break;
		case(ONEMANY):
			break;
		case(WANTEDOPT):
			break;
		case(UNWANTEDOPT):
			break;
		default:
			throw new IllegalArgumentException("Invalid modifier requested: " + type);
		}
		modified[type] = mod;
		return mod;
	}

	protected final int index;
	private final Parser parent;
	public final String name;
	public ParserState(String name, int index, Parser parent) {
		this.name = name;
		this.index = index;
		this.parent = parent;
	}

	private final LinkedList<LinkedList<Integer>> options = new LinkedList<LinkedList<Integer>>();
	
	public void enter(IStreamer stream, Deque<Integer> stack) {
		this.parent.branch(stream, this.options);
	}

	public ParserState option(LinkedList<Integer> list) {
		options.add(list);
		return this;
	}
	public ParserState option(String... names) {
		LinkedList<Integer> list = new LinkedList<Integer>();
		for(String s : names) {
			list.add(parent.rule(s).index);
		}
		return this.option(list);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean needNewline = false;
		if(options.size() == 0) {
			sb.append("[no rule for " + this.name + "]");
		}
		for(LinkedList<Integer> o : options) {
			if(needNewline) {
				sb.append("\n");
			}else {
				needNewline = true;
			}
			sb.append(this.name + " ==>");
			if(o.size() == 0) {
				sb.append(" [accept]");
			}
			for(Integer i : o) {
				sb.append(" " + parent.rule(i).name);
			}
		}
		return sb.toString();
	}
}
