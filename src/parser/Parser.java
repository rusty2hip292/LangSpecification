package parser;

import java.io.Serializable;
import java.util.Stack;

import parser.generator.json.IWriteJSONLambda;
import parser.istreamer.IStreamer;
import parser.istreamer.Streamer;
import parser.utils.JSONWriter;
import parser.utils.Utils;

public final class Parser<T> implements Serializable {

	public static final long serialVersionUID = 0;
	
	private final ParseTree<T> tree;
	private Parser(ParserState<T> ps) {
		this.tree = ParseTree.tree(ps).simplify();
	}
	
	public String toString() {
		return tree.toString();
	}

	public static <T> Parser<T> compile(ParserState<T> ps) {
		return new Parser<T>(ps);
	}
	
	public T parse(IStreamer stream) {
		try {
			Stack<RuleGenerator.ITBuilder<T>> result = tree.parse(stream);
			T t = result.pop().build(result);
			if(result.size() == 0) {
				return t;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		var t1 = Utils.timer();
		parser.RuleGenerator<IWriteJSONLambda> gen = new parser.RuleGenerator<>(new parser.generator.json.JSONWriterBuilder());
		ParserState<IWriteJSONLambda> p = gen.string("+", "+"), m = gen.string("*", "*"), t = gen.oneMany(gen.charSet("term", "0123456789"));
		RuleState<IWriteJSONLambda> exp = gen.rule("exp"), add = gen.rule("add"), mult = gen.rule("mult");
		gen.add(exp, add);
		gen.add(add, add, p, add);
		gen.add(add, mult, p, mult);
		gen.add(add, mult);
		gen.add(mult, mult, m, mult);
		gen.add(mult, t, m, t);
		gen.add(mult, t);
		long first = t1.time();
		var t2 = Utils.timer();
		Parser<IWriteJSONLambda> compiled = Parser.compile(exp);
		IStreamer stream = new Streamer("1+2*3*4+5+678*91011");
		JSONWriter json = new JSONWriter();
		long second = t2.time();
		var t3 = Utils.timer();
		try {
			compiled.parse(stream).write(json);
		}catch(Exception e) {
			json.debug();
		}
		long third = t3.time();
		System.out.println(json);
		System.out.println(stream);
		System.out.println(String.format("%s %s %s", first, second, third));
	}
}
