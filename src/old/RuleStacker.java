package old;
import java.util.ArrayList;
import java.util.Stack;

public abstract class RuleStacker {
	
	private long lastoperation = -1;
	private static Stack<Integer> parseStack = new Stack<Integer>();
	private ArrayList<RuleStacker> registered = new ArrayList<RuleStacker>();
	
	/**
	 * this is <<stream position, parse state>, size to pop parse stack to>
	 */
	private Stack<Tuple<Tuple<Integer, Integer>, Integer>> failStack = new Stack<Tuple<Tuple<Integer, Integer>, Integer>>();
	
	private class Tuple<L, R> {
		protected L l;
		protected R r;
		public Tuple(L l, R r) {
			this.l = l;
			this.r = r;
		}
	}
	
	public Streamer match(Streamer streamer) {
		return streamer;
	}
	private Streamer fail(Streamer streamer) {
		Tuple<Tuple<Integer, Integer>, Integer> failure = failStack.pop();
		while(parseStack.size() > failure.r) {
			parseStack.pop();
		}
		parseStack.push(failure.l.r);
		return streamer.fail(failure.l.l);
	}
}

class CharacterRuleMatch extends RuleStacker {
	
	public CharacterRuleMatch() {
		
	}
}
