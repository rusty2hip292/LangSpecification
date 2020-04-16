package old;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

public class RuleMatcher implements IRuleMatcher {

	private static Stack<RuleMatcher> rule_stack = new Stack<RuleMatcher>();
	private static Stack<FailureState> fail_stack = new Stack<FailureState>();
	private class FailureState {
		public final int rule_stack_size;
		public final IRuleMatcher fail_state;
		public FailureState(IRuleMatcher failure) {
			rule_stack_size = rule_stack.size();
			fail_state = failure;
		}
	}
	private static void fail(IStreamer stream) {
		stream.fail();
		FailureState f = fail_stack.pop();
		while(rule_stack)
	}
	
	private final IRuleMatcher matcher;
	private RuleMatcher(IRuleMatcher rm) {
		matcher = rm;
	}
	
	public static IRuleMatcher or(IRuleMatcher option1, IRuleMatcher option2) {
		return new RuleMatcher((IStreamer stream) -> {
			stream.setFailPoint();
			if(option1.attemptMatch(stream)) {
				return true;
			}
			stream.fail();
			return option2.attemptMatch(stream);
		});
	}
	
	public static IRuleMatcher wantedOptional(IRuleMatcher optional) {
		return new RuleMatcher((IStreamer stream) -> {
			stream.setFailPoint();
			if(option1)
		});
	}
	
	public static IRuleMatcher string(String text) {
		return new RuleMatcher((IStreamer stream) -> {
			StringBuffer next = new StringBuffer();
			for(int i = 0; i < text.length(); i++) {
				next.append(stream.next());
			}
			return next.toString().equals(text);
		});
	}
	
	public static IRuleMatcher chars(char[] range) {
		return chars(new char[][] {range});
	}
	public static IRuleMatcher chars(char[][] ranges) {
		HashSet<Character> chars = new HashSet<Character>();
		for(int i = 0; i < ranges.length; i++) {
			if(ranges[i].length != 2) {
				throw new IllegalArgumentException(String.format("Expected length 2 array for character range, found %s", Arrays.toString(ranges[i])));
			}
			for(char c = ranges[i][0]; c <= ranges[i][1]; c++) {
				chars.add(c);
			}
		}
		return chars(chars);
	}
	private static IRuleMatcher chars(Collection<Character> chars) {
		return new RuleMatcher((IStreamer stream) -> {
			return chars.contains(stream.next());
		});
	}
	
	public final boolean attemptMatch(IStreamer stream) {
		return matcher.attemptMatch(stream);
	}
}
