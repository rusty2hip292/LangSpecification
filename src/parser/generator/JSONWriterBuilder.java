package parser.generator;

import java.util.Stack;

import parser.ParserState;
import parser.RuleGenerator;
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
			return (Stack<RuleGenerator.ITBuilder<IWriteJSONLambda>> stack) -> {
				IWriteJSONLambda[] lambdas = new IWriteJSONLambda[names.length];
				for(int i = lambdas.length - 1; i >= 0; lambdas[i] = stack.pop().build(stack), i--);
				return (JSONWriter writer) -> {
					writer.openObject();
					for(int i = 0; i < names.length; i++) {
						writer.addKey(names[i]);
						lambdas[i].write(writer);
					}
					writer.close();
				};
			};
		};
	}

	public IList<IWriteJSONLambda> arrayRule() {
		return (ParserState<IWriteJSONLambda> listed_rule) -> {
			return (Stack<RuleGenerator.ITBuilder<IWriteJSONLambda>> stack) -> {
				IWriteJSONLambda inside = stack.pop().build(stack);
				return (JSONWriter writer) -> {
					writer.openArray();
					inside.write(writer);
					writer.close();
				};
			};
		};
	}

	public ICollapser<IWriteJSONLambda> collapserRule() {
		return (int num) -> {
			return (Stack<RuleGenerator.ITBuilder<IWriteJSONLambda>> stack) -> {
				IWriteJSONLambda[] collapsed = new IWriteJSONLambda[num];
				for(int i = collapsed.length - 1; i >= 0; collapsed[i--] = stack.pop().build(stack));
				return (JSONWriter writer) -> {
					for(IWriteJSONLambda lambda : collapsed) {
						lambda.write(writer);
					}
				};
			};
		};
	}
}
