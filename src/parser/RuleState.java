package parser;

import java.util.HashMap;
import java.util.LinkedList;

import parser.utils.Utils;

public class RuleState<T> extends ParserState<T> {
	
	public RuleState(String name) {
		super(name);
	}

	private LinkedList<LinkedList<ParserState<T>>> options = new LinkedList<LinkedList<ParserState<T>>>();
	
	private static String counter(HashMap<String, Integer> count_map, String str) {
		Integer i = count_map.get(str);
		if(i == null) {
			i = 0;
		}
		count_map.put(str, i + 1);
		return str + "_" + i;
	}

	public void addRule(RuleGenerator.ITBuilder<T> endState, ParserState<T>... option) {
		LinkedList<ParserState<T>> o = new LinkedList<ParserState<T>>();
		for(ParserState<T> ps : option) {
			o.add(ps);
		}
		o.add(new TerminalState<T>(">>" + this.toString(), ParserState.toTerminalLambda(endState)));
		options.add(o);
	}

	protected LinkedList<LinkedList<ParserState<T>>> getCompiledOptions() {
		LinkedList<LinkedList<ParserState<T>>> results = this.options;
		//System.out.println(results);
		results = this.removeIndirectLeftRecursion();
		//System.out.println(results);
		results = this.removeDirectLeftRecursion(results);
		//System.out.println(results);
		return results;
	}

	private LinkedList<LinkedList<ParserState<T>>> compiled = null;
	protected LinkedList<LinkedList<ParserState<T>>> removeIndirectLeftRecursion() {
		if(this.compiled != null) {
			return this.compiled;
		}
		LinkedList<LinkedList<ParserState<T>>> temp = this.options;
		for(int i = 0; i < temp.size(); i++) {
			while(true) {
				// assume always at least one parser state,
				// valid because a terminal node is always added at the end of non terminal states
				ParserState<T> first = temp.get(i).get(0);
				if(first instanceof TerminalState) {
					break;
				}else if(first instanceof RuleState) {
					if(first == this) {
						break;
					}
					RuleState<T> ntps = (RuleState<T>) first;
					temp = Utils.insert(temp, i, 0, ntps.getCompiledOptions());
					//System.out.println(i);
					//System.out.println(String.format("%s|%s", this, first));
					//System.out.println(temp);
					//Utils.delay(1000);
				}else {
					throw new IllegalArgumentException("Failed to account for class type " + first.getClass().toString());
				}
			}
		}
		this.compiled = temp;
		return temp;
	}

	private final LinkedList<LinkedList<ParserState<T>>> removeDirectLeftRecursion(LinkedList<LinkedList<ParserState<T>>> indirectRemoved) {
		LinkedList<LinkedList<ParserState<T>>> base = new LinkedList<LinkedList<ParserState<T>>>(), recursive = new LinkedList<LinkedList<ParserState<T>>>();
		for(LinkedList<ParserState<T>> list : indirectRemoved) {
			((list.get(0) == this) ? recursive : base).add(list);
		}
		for(int i = recursive.size() - 1; i >= 0; i--) {
			recursive = Utils.insert(recursive, i, 0, base);
		}
		for(LinkedList<ParserState<T>> r : recursive) {
			base.add(r);
		}
		return base;
	}
	
	protected Iterable<LinkedList<ParserState<T>>> iterator() {
		LinkedList<LinkedList<ParserState<T>>> list = this.getCompiledOptions();
		return () -> {
			return list.iterator();
		};
	}
}
