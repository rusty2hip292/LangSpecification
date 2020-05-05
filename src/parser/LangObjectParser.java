package parser;

import java.util.LinkedList;
import java.util.Stack;

import stream.IStream;

public abstract class LangObjectParser<T extends LangObject> {

	public LangObjectParser<?> fail_state() {
		return null;
	}
	public LinkedList<LangObjectParser<? extends LangObject>> requires() {
		return parser.utils.Utils.link();
	}
	public LangObjectBuilder<? extends T> maker(stream.IStream stream) {
		return (Stack<LangObject> stack) -> { };
	}
	
	protected static LangObjectParser<LangObject> consec(LangObjectParser<?>... parsers) {
		return new ConsecParser<LangObject>(parsers);
	}
	protected static <T extends LangObject> LangObjectParser<? extends T> or(LangObjectParser<? extends T> o1, LangObjectParser<? extends T> o2) {
		return new OrParser<T>(o1, o2);
	}
	protected static LangObjectParser<?> ignore(LangObjectParser<?> p) {
		return consec(p, new PopParser());
	}
	protected static LangObjectParser<?> wantedOptional(LangObjectParser<? extends LangObject> p) {
		return new OrParser(p, new PlaceNullParser());
	}
	protected static LangObjectParser<?> unwantedOptional(LangObjectParser<? extends LangObject> p) {
		return new OrParser(new PlaceNullParser(), p);
	}
	private static LangObjectParser<?> zeroMany(LangObjectParser<?> p) {
		return or(consec())
	}

	private static class FlagParser extends LangObjectParser<LangObject> { }
	private static class PlaceNullParser extends LangObjectParser<LangObject> {
		
		public LangObjectBuilder<?> maker(stream.IStream stream) {
			return (Stack<LangObject> stack) -> {
				stack.push(null);
			};
		}
	}
	private static class PopParser extends LangObjectParser<LangObject> {
		
		public LangObjectBuilder<LangObject> maker(stream.IStream stream) {
			return (Stack<LangObject> stack) -> {
				stack.pop();
			};
		}
	}
	private static class ConsecParser<T extends LangObject> extends LangObjectParser<T> {

		private final LinkedList<LangObjectParser<?>> list;
		public ConsecParser(LangObjectParser<? extends T>... parsers) {
			this.list = parser.utils.Utils.link(parsers);
		}

		public LinkedList<LangObjectParser<?>> requires() {
			return list;
		}
	}

	private static class OrParser<T extends LangObject> extends LangObjectParser<T> {

		private final LangObjectParser<? extends T> o1, o2;
		public OrParser(LangObjectParser<? extends T> o1, LangObjectParser<? extends T> o2) {
			this.o1 = o1;
			this.o2 = o2;
		}

		public LangObjectParser<? extends T> fail_state() {
			return o2;
		}
		public LinkedList<LangObjectParser<?>> requires() {
			return parser.utils.Utils.link(o1);
		}
	}
}
