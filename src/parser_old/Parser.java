package parser_old;

import java.util.Stack;

public class Parser implements IParser {

	private long localID = 0;
	
	public static boolean parse(IStreamer stream, IParser parser) {
		Parser.push(parser);
		try {
			while(Parser.stack().size() > 0) {
				System.out.println(stream.toString() + "\n" + Parser.parse_stack.size());
				Parser.stack().pop().parse(stream);
			}
		}catch(Exception e) {
			return false;
		}
		return !stream.hasNext();
	}

	public static IParser UnwantedOptional(IParser p) {
		return new Parser((IStreamer stream) -> {
			Parser.setFailPoint(stream).push(p);
		});
	}
	public static IParser WantedOptional(IParser p) {
		return new Parser((IStreamer stream) -> {
			Parser.setFailPoint(stream);
			Parser.push(p);
		});
	}
	public static IParser zeroOrMany(IParser p) {
		Parser temp = new Parser();
		temp.setLambda((IStreamer stream) -> {
			Parser.setFailPoint(stream);
			Parser.push(Parser.consecutive(p, temp));
		});
		return temp;
	}
	public static IParser oneOrMany(IParser p) {
		Parser temp = new Parser();
		temp.setLambda((IStreamer stream) -> {
			Parser.setFailPoint(stream).push(p);
			Parser.push(Parser.consecutive(p, temp));
		});
		return temp;
	}
	public static IParser zeroOrFew(IParser p) {
		Parser temp = new Parser();
		temp.setLambda((IStreamer stream) -> {
			Parser.setFailPoint(stream).push(Parser.consecutive(p, temp));
		});
		return temp;
	}
	public static IParser oneOrFew(IParser p) {
		Parser temp = new Parser();
		temp.setLambda((IStreamer stream) -> {
			Parser.setFailPoint(stream).push(Parser.consecutive(p, temp));
			Parser.push(p);
		});
		return temp;
	}
	public static IParser consecutive(IParser... parsers) {
		Parser temp = new Parser();
		temp.setLambda((IStreamer stream) -> {
			for(int i = parsers.length - 1; i >= 0; i--) {
				Parser.push(parsers[i]);
			}
		});
		return temp;
	}
	public static IParser or(IParser p1, IParser p2) {
		return new Parser((IStreamer stream) -> {
			Parser.setFailPoint(stream).push(p2);
			Parser.push(p1);
		});
	}
	public static IParser string(String string) {
		return new Parser((IStreamer stream) -> {
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < string.length(); i++) {
				sb.append(stream.next());
			}
			if(!sb.toString().equals(string)) {
				Parser.fail(stream);
			}
		});
	}
	public static IParser charclass(String chars) {
		return new Parser((IStreamer stream) -> {
			if(chars.indexOf(stream.next()) < 0) {
				Parser.fail(stream);
			}
		});
	}
	public static Parser temp() {
		return new Parser();
	}

	private static Stack<Stack<IParser>> parse_stack = new Stack<Stack<IParser>>();
	static {
		parse_stack.push(new Stack<IParser>());
	}
	IParser lambda = null;
	private Parser(IParser lambda) {
		this.lambda = lambda;
	}
	private Parser() { }
	public IParser setLambda(IParser lambda) {
		this.lambda = lambda;
		return this;
	}
	public void parse(IStreamer stream) {
		if(this.localID == stream.id() || this.lambda == null) {
			System.out.println("here " + this.localID + " " + stream.id());
			Parser.fail(stream);
		}else {
			if(this.localID < 0) {
				this.localID = stream.id();
			}else {
				this.localID = -stream.id();
			}
			lambda.parse(stream);
		}
	}
	private static void fail(IStreamer stream) {
		stream.fail();
		parse_stack.pop();
	}
	private static Stack<IParser> setFailPoint(IStreamer stream) {
		stream.setFailPoint();
		Stack<IParser> failstack = stack();
		parse_stack.push((Stack<IParser>) failstack.clone());
		return failstack;
	}
	private static Stack<IParser> stack() {
		return parse_stack.peek();
	}
	private static void push(IParser p) {
		Parser.stack().push(p);
	}
}
