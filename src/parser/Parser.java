package parser;

import java.util.Stack;

import parser.generator.IWriteJSONLambda;
import parser.istreamer.IStreamer;
import parser.istreamer.Streamer;
import parser.utils.JSONWriter;

public final class Parser<T> {

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
		parser.RuleGenerator<IWriteJSONLambda> gen = new parser.RuleGenerator<>(new parser.generator.JSONWriterBuilder());
		ParserState<IWriteJSONLambda> p = gen.string("+", "+"), m = gen.string("*", "*"), t = gen.string("term", "7");
		RuleState<IWriteJSONLambda> exp = gen.rule("exp"), add = gen.rule("add"), mult = gen.rule("mult");
		gen.add(exp, add);
		gen.add(exp, t);
		gen.add(mult, exp, m, exp);
		gen.add(mult, exp);
		gen.add(add, mult, p, mult);
		gen.add(add, mult);
		//System.out.println(exp.removeIndirectLeftRecursion());
		//System.out.println(exp.getCompiledOptions());
		Parser<IWriteJSONLambda> compiled = Parser.compile(exp);
		IStreamer stream = new Streamer("7+7*7*7*7+7*7");
		JSONWriter json = new JSONWriter();
		try {
			compiled.parse(stream).write(json);
			System.out.println(json);
		}catch(Exception e) {
			json.debug();
		}
	}
}
