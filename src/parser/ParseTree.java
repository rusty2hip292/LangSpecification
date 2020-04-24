package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import langobject.LangObject;
import langobject.LangObject.NonTerminalLangObject.Constructor;
import parser.Parser.IStackLambda;

public class ParseTree {

	public String toString() {
		return this.root.toString();
	}
	
	private interface Lambda {
		void lambda(Object data);
	}
	protected LangObject parse(stream.IStreamer stream) {
		Stack<Parser.IStackLambda> parse = new Stack<Parser.IStackLambda>();
		Stack<ParseTreeNode> taken = new Stack<ParseTreeNode>();
		Stack<Integer> parse_size = new Stack<Integer>();
		Stack<Integer> taken_size = new Stack<Integer>();
		Stack<Stack<ParseTreeNode>> next_state = new Stack<Stack<ParseTreeNode>>();
		next_state.push(new Stack<ParseTreeNode>());
		Stack<ParseTreeNode> fail_state = new Stack<ParseTreeNode>();
		Lambda checkpoint = (Object o) -> {
			if(o != null) {
				parse_size.push(parse.size());
				taken_size.push(taken.size());
				stream.setFailPoint();
				fail_state.push((ParseTreeNode) o);
				next_state.push((Stack<ParseTreeNode>) next_state.peek().clone());
			}
		};
		Lambda fail = (Object o) -> {
			if(parse_size.size() == 0) {
				throw new IllegalArgumentException("Bad parse: " + stream);
			}
			int size = parse_size.pop();
			while(parse.size() > size) {
				parse.pop();
			}
			size = taken_size.pop();
			while(taken.size() > size) {
				taken.pop();
			}
			stream.fail();
			next_state.pop();
			next_state.peek().push(fail_state.pop());
		};
		Lambda success = (Object o) -> {
			if(o == null) {
				fail.lambda(o);
			}else {
				parse.push((parser.Parser.IStackLambda) o);
			}
		};
		Lambda next = (Object o) -> {
			if(o != null) {
				next_state.peek().push((ParseTreeNode) o);
			}
		};
		next.lambda(this.root);
		while(true) {
			try {
				//Thread.sleep(200);
			}catch(Exception e) { }
			if(!stream.hasNext() && next_state.peek().isEmpty()) {
				break;
			}else if(!next_state.peek().isEmpty()) {
				ParseTreeNode node = next_state.peek().pop();
				taken.push(node);
				node.parse(stream, checkpoint, success, next);
			}else {
				fail.lambda(null);
			}
		}
		Stack<LangObject> objs = new Stack<LangObject>();
		for(parser.Parser.IStackLambda lamda : parse) {
			lamda.parse(objs);
		}
		if(objs.size() == 1) {
			return objs.pop();
		}
		throw new IllegalArgumentException("Bad parse, ended up with " + objs.toString());
	}
	
	protected abstract static class ParseTreeNode {
		
		protected abstract void parse(stream.IStreamer stream, Lambda checkpoint, Lambda success, Lambda next);
		
		private static int counter = 0;
		
		protected String name;
		protected ParseTreeNode(String name) {
			this.name = name;
		}
		protected ParseTreeNode() {
			this(String.format("(%02d)", counter++));
		}
		public String toString() {
			return this.walk(new HashSet<ParseTreeNode>());
		}
		protected abstract String walk(HashSet<ParseTreeNode> visited);
		protected ParseTreeNode simplify() {
			return this.simplify(new HashSet<ParseTreeNode>());
		}
		protected abstract ParseTreeNode simplify(HashSet<ParseTreeNode> visited);
		
		protected static class TerminalParseTreeNode extends ParseTreeNode {
			protected Parser.IStreamToStackLambda me = null;
			protected TerminalParseTreeNode(String name) {
				super(name);
			}
			protected TerminalParseTreeNode() { super(); }
			protected String walk(HashSet<ParseTreeNode> nodes) {
				return "";
			}
			protected ParseTreeNode simplify(HashSet<ParseTreeNode> visited) {
				return this;
			}
			protected void parse(stream.IStreamer stream, Lambda checkpoint, Lambda success, Lambda next) {
				success.lambda(this.me.tokenize(stream));
			}
		}
		protected static class NonTerminalParseTreeNode extends ParseTreeNode {
			
			protected void parse(stream.IStreamer stream, Lambda checkpoint, Lambda success, Lambda next) {
				checkpoint.lambda(this.fail);
				next.lambda(this.next);
				next.lambda(this.me);
			}
			
			protected ParseTreeNode me = null, next = null, fail = null;
			protected NonTerminalParseTreeNode(String name) {
				super(name);
			}
			protected NonTerminalParseTreeNode() { super(); }
			protected String walk(HashSet<ParseTreeNode> visited) {
				if(this.me == null || visited.contains(this)) {
					return "";
				}
				visited.add(this);
				if(this.next == null) {
					if(this.fail == null) {
						return String.format("%s : %s\n%s", this.name, this.me.name, this.me.walk(visited));
					}else {
						return String.format("%s : %s | %s\n%s%s", this.name, this.me.name, this.fail.name, this.me.walk(visited), this.fail.walk(visited));
					}
				}else {
					if(this.fail == null) {
						return String.format("%s : %s %s\n%s%s", this.name, this.me.name, this.next.name, this.me.walk(visited), this.next.walk(visited));
					}else {
						return String.format("%s : %s %s | %s\n%s%s%s", this.name, this.me.name, this.next.name, this.fail.name, this.me.walk(visited), this.next.walk(visited), this.fail.walk(visited));
					}
				}
			}
			protected ParseTreeNode simplify(HashSet<ParseTreeNode> visited) {
				if(visited.contains(this)) {
					return this;
				}
				visited.add(this);
				if(this.fail != null) {
					this.fail = this.fail.simplify(visited);
				}
				if(this.next != null) {
					this.next = this.next.simplify(visited);
				}
				if(this.me != null) {
					this.me = this.me.simplify(visited);
				}
				if(this.me == null) {
					this.me = this.next;
					this.next = null;
				}
				if(this.me == null) {
					return this.fail;
				}
				if(this.next == null && this.fail == null) {
					return this.me;
				}
				return this;
			}
		}
	}
	private ParseTreeNode root;
	
	private HashMap<LangObject, ParseState> states = new HashMap<LangObject, ParseState>();
	
	private static Parser.IStreamToStackLambda toParserLambda(utils.Tuple<LinkedList<LangObject>, LangObject.NonTerminalLangObject.Constructor> rule) {
		int size = rule.left.size();
		LangObject.NonTerminalLangObject.Constructor constructor = rule.right;
		return (stream.IStreamer stream) -> {
			return (Stack<LangObject> stack) -> {
				Stack<LangObject> temp = utils.Utils.flip(stack, size);
				LangObject lo = constructor.construct(temp);
				if(lo != null) {
					stack.push(lo);
				}
			};
		};
	}
	private static Parser.IStreamToStackLambda toParserLambda(LangObject.TerminalLangObject.Terminal terminal) {
		return (stream.IStreamer stream) -> {
			langobject.LangObject.TerminalLangObject.Constructor constructor = terminal.construct(stream);
			if(constructor == null) {
				return null;
			}
			return (Stack<LangObject> stack) -> {
				stack.push(constructor.construct());
			};
		};
	}
	private ParseState getState(LangObject object) {
		ParseState ps = states.get(object);
		if(ps == null) {
			if(object instanceof LangObject.TerminalLangObject) {
				ps = new ParseState.TerminalParseState(object.name(), ParseTree.toParserLambda(((LangObject.TerminalLangObject) object).terminal()));
				return ps;
			}else if(object instanceof LangObject.NonTerminalLangObject) {
				LangObject.NonTerminalLangObject ntlo = (LangObject.NonTerminalLangObject) object;
				ParseState.NonTerminalParseState ntps = new ParseState.NonTerminalParseState(object.name());
				states.put(object, ntps);
				for(utils.Tuple<LinkedList<LangObject>, LangObject.NonTerminalLangObject.Constructor> rule : ntlo.parse_rules()) {
					ntps.newRule();
					for(LangObject lo : rule.left) {
						ntps.add(getState(lo));
					}
					ntps.add(new ParseState.TerminalParseState("[" + object.name() + "]", ParseTree.toParserLambda(rule)));
				}
				return ntps;
			}else {
				throw new IllegalArgumentException("All language objects must inherit from TerminalLangObject or NonTerminalLangObject.");
			}
		}
		return ps;
	}
	
	protected void build(LangObject prototype) {
		this.root = this.getState(prototype).compile().simplify();
	}
}
