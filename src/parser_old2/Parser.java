package parser_old2;

import java.util.Stack;

public final class Parser {

	public static void parse(IStreamer stream, ParserState start) {
		Parser.stack().push(start);
		try {
			while(Parser.stack().size() > 0) {
				System.out.println(Parser.stack().size());
				taken.push(Parser.stack().pop()).enter(stream);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Stack<Stack<ParserState>> state;
	private static Stack<ParserState> taken;
	private static Stack<Integer> sizes;
	public static final ParserState lowercase, uppercase, numeric, alpha, alphanumeric, whitespace;
	static {
		state = new Stack<Stack<ParserState>>();
		state.push(new Stack<ParserState>());
		taken = new Stack<ParserState>();
		sizes = new Stack<Integer>();
		lowercase = Parser.charRange(Parser.range('a', 'z'));
		uppercase = Parser.charRange(Parser.range('A', 'Z'));
		numeric = Parser.charRange(Parser.range('0', '9'));
		alpha = Parser.charRange(Parser.range('a', 'z'), Parser.range('A', 'Z'), Parser.range('_', '_'));
		alphanumeric = Parser.charRange(Parser.range('a', 'z'), Parser.range('A', 'Z'), Parser.range('0', '9'), Parser.range('_', '_'));
		whitespace = Parser.zeroMany(Parser.charRange(Parser.range((char) 9, (char) 13), Parser.range(' ', ' ')));
	}
	
	protected static void setFailPoint(IStreamer stream) {
		stream.setFailPoint();
		sizes.push(taken.size());
	}
	
	protected static Stack<ParserState> stack() {
		return state.peek();
	}
	@SuppressWarnings("unchecked")
	protected static Tuple<Stack<ParserState>, Stack<ParserState>> branch(IStreamer stream) {
		Tuple<Stack<ParserState>, Stack<ParserState>> t = new Tuple<Stack<ParserState>, Stack<ParserState>>();
		t.left = state.peek();
		t.right = (Stack<ParserState>) t.left.clone();
		state.push(t.right);
		Parser.setFailPoint(stream);
		return t;
	}
	
	protected static void fail(IStreamer stream) {
		stream.fail();
		state.pop();
		int targetSize = sizes.pop();
		for(int i = taken.size(); i > targetSize; i--) {
			taken.pop();
		}
	}
	
	public static ParserState or(ParserState... states) {
		return or(0, states);
	}
	private static ParserState or(int index, ParserState... states) {
		int size = states.length - index;
		if(size == 1) {
			return states[index];
		}else {
			return new OrParserState(states[index], Parser.or(index + 1, states));
		}
	}
	
	public static ParserState identity(ParserState state) {
		return Parser.consecutive(state);
	}
	
	public static ParserState consecutive(ParserState... states) {
		return new ConsecutiveParserState(states);
	}

	public static ParserState zeroMany(ParserState state) {
		return new ListParserState(state, ListParserState.ListType.zeroMany);
	}
	public static ParserState zeroFew(ParserState state) {
		return new ListParserState(state, ListParserState.ListType.zeroFew);
	}
	public static ParserState oneMany(ParserState state) {
		return new ListParserState(state, ListParserState.ListType.oneMany);
	}
	public static ParserState oneFew(ParserState state) {
		return new ListParserState(state, ListParserState.ListType.oneFew);
	}

	public static ParserState wantedOptional(ParserState state) {
		return new OptionalParserState(state, true);
	}
	public static ParserState unwantedOptional(ParserState state) {
		return new OptionalParserState(state, false);
	}
	
	public static ParserState string(String string) {
		return new ExactStringParserState(string);
	}
	
	public static char[] range(char a, char b) {
		if(a <= b) {
			return new char[] {a, b};
		}
		return new char[] {b, a};
	}
	public static ParserState charRange(char[]... ranges) {
		return new CharacterRangeParserState(ranges);
	}
	
	public static UnsetParserState unset() {
		return new UnsetParserState();
	}
	
	public static ParserState alert(String alert) {
		return new MetaParserState((IStreamer stream) -> {
			System.out.println(alert);
		});
	}
}

abstract class ParserState {
	
	protected IStreamerLambda lambda;
	
	protected final void enter(IStreamer stream) {
		if(lambda != null) {
			lambda.lambda(stream);
		}else {
			Parser.fail(stream);
		}
	}
}

class MetaParserState extends ParserState {
	
	public MetaParserState(IStreamerLambda lambda) {
		this.lambda = lambda;
	}
}

class ConsecutiveParserState extends ParserState {
	
	private ParserState[] states;
	
	public ConsecutiveParserState(ParserState... states) {
		this.states = states;
		this.lambda = (IStreamer stream) -> {
			for(int i = states.length - 1; i >= 0; i--) {
				Parser.stack().push(states[i]);
			}
		};
	}
}

class OrParserState extends ParserState {
	
	private ParserState p1, p2;
	
	public OrParserState(ParserState p1, ParserState p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.lambda = (IStreamer stream) -> {
			Tuple<Stack<ParserState>, Stack<ParserState>> branch = Parser.branch(stream);
			if(p2 != null) {
				branch.left.push(p2);
			}
			if(p1 != null) {
				branch.right.push(p1);
			}
		};
	}
}

class ListParserState extends ParserState {
	protected enum ListType {
		zeroMany,
		zeroFew,
		oneMany,
		oneFew
	}
	
	private ListType type;
	private ParserState state;
	
	public ListParserState(ParserState ps, ListType type) {
		this.type = type;
		this.state = ps;
		this.lambda = (IStreamer stream) -> {
			ParserState or = null;
			switch(type) {
			case oneFew:
				or = Parser.or(ps, Parser.consecutive(ps, Parser.zeroFew(ps)));
				break;
			case oneMany:
				or = Parser.or(Parser.consecutive(ps, Parser.zeroMany(ps)), ps);
				break;
			case zeroFew:
				or = Parser.or(null, Parser.consecutive(ps, this));
				break;
			case zeroMany:
				or = Parser.or(Parser.consecutive(ps, this), null);
				break;
			}
			Parser.stack().push(or);
		};
	}
}

class OptionalParserState extends ParserState {
	
	private boolean wanted;
	private ParserState state;
	
	public OptionalParserState(ParserState state, boolean wanted) {
		this.state = state;
		this.wanted = wanted;
		this.lambda = (IStreamer stream) -> {
			ParserState or;
			if(this.wanted) {
				or = Parser.or(state, null);
			}else {
				or = Parser.or(null, state);
			}
			Parser.stack().push(or);
		};
	}
}

class CharacterRangeParserState extends ParserState {
	
	private char[][] ranges;
	
	public CharacterRangeParserState(char[][] ranges) {
		this.ranges = ranges;
		this.lambda = (IStreamer stream) -> {
			if(stream.hasNext()) {
				char c = stream.next();
				for(char[] range : ranges) {
					if(c >= range[0] && c <= range[1]) {
						return;
					}
				}
			}
			Parser.fail(stream);
		};
	}
}

class ExactStringParserState extends ParserState {
	
	private String str;
	
	public ExactStringParserState(String string) {
		str = string;
		this.lambda = (IStreamer stream) -> {
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < str.length() && stream.hasNext(); i++) {
				sb.append(stream.next());
			}
			if(!sb.toString().equals(str)) {
				Parser.fail(stream);
			}
		};
	}
}

class UnsetParserState extends ParserState {
	
	public ParserState set(ParserState state) {
		this.lambda = state.lambda;
		return this;
	}
}
