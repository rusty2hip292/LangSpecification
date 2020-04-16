package parser;

import java.util.Stack;

import parser.ParserState.ITerminalLambda;
import parser.TerminalState.IReadStringFromStream;
import parser.generator.RuleGeneratorBuilder;
import parser.istreamer.IStreamer;

public class RuleGenerator<T> {

	public interface ITBuilder<T> {
		T build(Stack<T> stack);
	}
	public static <T> ITBuilder<T> wrap(T t) {
		return (Stack<T> stack) -> {
			return t;
		};
	}
	
	/**
	 * a lambda expression, (ParserState<T> listed_rule) -> T
	 */
	public static interface IList<T> {
		T list(ParserState<T> listed);
	}
	/**
	 * a lambda expression, (int num) -> T
	 * should merge the last num T states into a single state on the parse stack
	 */
	public static interface ICollapser<T> {
		T collapse(int num_to_collapse);
	}

	public ParserState<T> zeroMany(ParserState<T> state) {
		return makeArray(state, false, true);
	}
	public ParserState<T> zeroFew(ParserState<T> state) {
		return makeArray(state, false, false);
	}
	public ParserState<T> oneMany(ParserState<T> state) {
		return makeArray(state, true, true);
	}
	public ParserState<T> oneFew(ParserState<T> state) {
		return makeArray(state, true, false);
	}
	private ParserState<T> makeArray(ParserState<T> state, boolean require1, boolean many) {
		RuleState<T> arr = this.rule("[" + state.toString() + "]");
		arr.addRule(this.array.list(state), list(state, require1, many));
		return arr;
	}
	private ParserState<T> list(ParserState<T> state, boolean require1, boolean many) {
		RuleState<T> inside_arr = this.rule(state.toString() + (require1 ? "+" : "*") + (many ? "" : "?"));
		T collapse0 = this.collapser.collapse(0), collapse2 = this.collapser.collapse(2);
		if(require1) {
			inside_arr.addRule(collapse2, state, list(state, false, many));
			return inside_arr;
		}
		if(many) {
			inside_arr.addRule(collapse2, state, inside_arr);
			inside_arr.addRule(collapse0);
		}else {
			inside_arr.addRule(collapse0);
			inside_arr.addRule(collapse2, state, inside_arr);
		}
		return inside_arr;
	}
	
	public static final ITermPhase1 matchExactString(String str) {
		return (String name) -> {
			return (IStreamer stream) -> {
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < str.length() && stream.hasNext(); i++, sb.append(stream.next()));
				String s = sb.toString();
				return s.equals(str) ? s : null;
			};
		};
	}

	public ParserState<T> string(String name, String str) {
		return terminal(name, matchExactString(str));
	}
	private ParserState<T> terminal(String name, ITermPhase1 t1) {
		return new TerminalState<T>(name, compile_term(t1, this.term).name(name));
	}
	public RuleState<T> rule(String name) {
		return new RuleState<T>(name);
	}
	public RuleGenerator<T> add(RuleState<T> rule, ParserState<T>... option) {
		rule.addRule(this.rule.action(rule.toString(), option), option);
		return this;
	}

	private final ITermPhase2<T> term;
	private final IRulePhase<T> rule;
	private final IList<T> array;
	private final ICollapser<T> collapser;
	private RuleGenerator(ITermPhase2<T> term, IRulePhase<T> rule, IList<T> array, ICollapser<T> collapser) {
		this.term = (String name, String matched) -> {
			return matched == null ? null : term.action(name, matched);
		};
		this.rule = rule;
		this.array = array;
		this.collapser = collapser;
	}
	public RuleGenerator(RuleGeneratorBuilder<T> builder) {
		this(builder.termRule(), builder.ruleRule(), builder.arrayRule(), builder.collapserRule());
	}

	/**
	 * a lambda expression, (String rulename) -> ((IStreamer stream) -> String)
	 */
	public static interface ITermPhase1 {
		IReadStringFromStream read(String name);
	}
	/**
	 * a lambda expression, (String rulename, String matched) -> T
	 */
	public static interface ITermPhase2<T> {
		T action(String name, String read);
	}
	private static interface ITermPhase3<T> {
		ITerminalLambda<T> name(String name);
	}
	/**
	 * a lambda expression, (String rulename, ParserState<T>... options) -> T
	 */
	public static interface IRulePhase<T> {
		T action(String name, ParserState<T>... option);
	}
	private static <T> ITermPhase3<T> compile_term(ITermPhase1 p1, ITermPhase2<T> p2) {
		return (String name) -> {
			return (IStreamer stream) -> {
				return p2.action(name, p1.read(name).read(stream));
			};
		};
	}
}
