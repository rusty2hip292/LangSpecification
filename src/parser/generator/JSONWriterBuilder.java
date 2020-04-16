package parser.generator;

import java.util.Stack;

import parser.ParserState;
import parser.RuleGenerator.ICollapser;
import parser.RuleGenerator.IList;
import parser.RuleGenerator.IRulePhase;
import parser.RuleGenerator.ITermPhase2;
import parser.utils.JSONWriter;
import parser.utils.Utils;

public class JSONWriterBuilder extends RuleGeneratorBuilder<parser.generator.IWriteJSONLambda> {

	public ITermPhase2<IWriteJSONLambda> termRule() {
		return (String name, String matched) -> {
			return (JSONWriter writer) -> {
				writer.value(matched);
			};
		};
	}

	public IRulePhase<IWriteJSONLambda> ruleRule() {
		return (String rulename, ParserState<IWriteJSONLambda>... options) -> {
			String[] names = new String[options.length];
			for(int i = 0; i < names.length; names[i] = options[i].toString(), i++);
			return (JSONWriter writer, Stack<IWriteJSONLambda> stack) -> {
				Stack<IWriteJSONLambda> stack2 = Utils.flip(stack, names.length);
				writer.openObject();
				for(String name : names) {
					writer.addKey(name);
					stack2.pop().write(writer, stack);
				}
				writer.close();
			};
		};
	}

	public IList<IWriteJSONLambda> arrayRule() {
		return (ParserState<IWriteJSONLambda> listed_rule) -> {
			return(JSONWriter writer, Stack<IWriteJSONLambda> stack) -> {
				writer.openArray();
				stack.pop().write(writer, stack);
				writer.close();
			};
		};
	}

	public ICollapser<IWriteJSONLambda> collapserRule() {
		return (int num) -> {
			return(JSONWriter writer, Stack<IWriteJSONLambda> stack) -> {
				Stack<IWriteJSONLambda> grabbed = Utils.flip(stack, num);
				for(int i = 0; i < num; i++) {
					grabbed.pop().write(writer, stack);
				}
			};
		};
	}
}
