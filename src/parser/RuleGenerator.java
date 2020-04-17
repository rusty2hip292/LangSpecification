package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import parser.ParserState.ITerminalLambda;
import parser.TerminalState.IReadStringFromStream;
import parser.generator.RuleGeneratorBuilder;
import parser.istreamer.IStreamer;
import parser.utils.Tuple;

public class RuleGenerator<T> {

	public interface ITBuilder<T> {
		T build(Stack<ITBuilder<T>> stack);
	}
	public static <T> ITBuilder<T> wrap(T t) {
		return t == null ? null : (Stack<ITBuilder<T>> stack) -> {
			return t;
		};
	}

	/**
	 * a lambda expression, (ParserState<T> listed_rule) -> T
	 */
	public static interface IList<T> {
		RuleGenerator.ITBuilder<T> list(ParserState<T> listed);
	}
	/**
	 * a lambda expression, (int num) -> T
	 * should merge the last num T states into a single state on the parse stack
	 */
	public static interface ICollapser<T> {
		RuleGenerator.ITBuilder<T> collapse(int num_to_collapse);
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
		RuleGenerator.ITBuilder<T> collapse0 = this.collapser.collapse(0), collapse2 = this.collapser.collapse(2);
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
	public ParserState<T> wanted(ParserState<T> state) {
		return makeOptional(state, true);
	}
	public ParserState<T> unwanted(ParserState<T> state) {
		return makeOptional(state, false);
	}
	private ParserState<T> makeOptional(ParserState<T> state, boolean wanted) {
		RuleState<T> opt = this.rule(state.toString() + "?" + (wanted ? "" : ""));
		opt.addRule(this.array.list(state), optional(state, wanted));
		return opt;
	}
	private ParserState<T> optional(ParserState<T> state, boolean wanted) {
		RuleState<T> opt = this.rule("[" + state.toString() + "]");
		RuleGenerator.ITBuilder<T> collapse0 = this.collapser.collapse(0), collapse1 = this.collapser.collapse(1);
		if(wanted) {
			opt.addRule(collapse1, state);
			opt.addRule(collapse0);
		}else {
			opt.addRule(collapse0);
			opt.addRule(collapse1, state);
		}
		return opt;
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
	public static final ITermPhase1 matchCharSet(boolean match, String acceptedchars) {
		char[] chars = acceptedchars.toCharArray();
		Arrays.sort(chars);
		if(chars.length == 0) {
			return matchCharSet(match);
		}
		ArrayList<Tuple<Character, Character>> ranges = new ArrayList<Tuple<Character, Character>>();
		char min = chars[0];
		char last = min;
		for(char c : chars) {
			if(c - last <= 1) {
				last = c;
				continue;
			}
			ranges.add(new Tuple<Character, Character>(min, last));
			min = c;
			last = min;
		}
		ranges.add(new Tuple<Character, Character>(min, last));
		Tuple<Character, Character>[] rs = new Tuple[ranges.size()];
		rs = ranges.toArray(rs);
		return matchCharSet(true, rs);
	}
	public static final ITermPhase1 matchCharSet(boolean match, Tuple<Character, Character>... ranges) {
		char[] starts = new char[ranges.length], ends = new char[ranges.length];
		for(int i = 0; i < starts.length; i++) {
			Tuple<Character, Character> range = ranges[i];
			if(range.left < range.right) {
				starts[i] = range.left;
				ends[i] = range.right;
			}else {
				starts[i] = range.right;
				ends[i] = range.left;
			}
		}
		return (String name) -> {
			return (IStreamer stream) -> {
				char c = ' ';
				if(stream.hasNext()) {
					c = stream.next();
					for(int i = 0; i < starts.length; i++) {
						System.out.println(String.format("%s %s %s", starts[i], c, ends[i]));
						if(c >= starts[i] && c <= ends[i]) {
							return match ? "" + c : null;
						}
					}
				}else {
					return null;
				}
				return match ? null : "" + c;
			};
		};
	}

	public ParserState<T> string(String name, String str) {
		return terminal(name, matchExactString(str));
	}
	public ParserState<T> charSet(String name, String str) {
		return terminal(name, matchCharSet(true, str));
	}
	public ParserState<T> notCharSet(String name, String str) {
		return terminal(name, matchCharSet(false, str));
	}

	public final ParserState<T> terminal(String name, ITermPhase1 t1) {
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
		ITBuilder<T> action(String name, ParserState<T>... option);
	}
	private static <T> ITermPhase3<T> compile_term(ITermPhase1 p1, ITermPhase2<T> p2) {
		return (String name) -> {
			return (IStreamer stream) -> {
				return RuleGenerator.wrap(p2.action(name, p1.read(name).read(stream)));
			};
		};
	}
}
