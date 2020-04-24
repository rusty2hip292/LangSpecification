package langobject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import utils.Tuple;
import utils.Utils;

public abstract class LangObject {
	
	public String name() {
		return this.getClass().getName();
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		return this.getClass().getName().equals(o.getClass().getName());
	}
	public int hashCode() {
		return this.getClass().getName().hashCode();
	}
	
	public static abstract class TerminalLangObject extends LangObject {

		public interface Constructor {
			LangObject construct();
		}
		public interface Terminal {
			Constructor construct(stream.IStreamer stream);
		}

		public abstract Terminal terminal();
	}
	
	public static class Consecutive extends NonTerminalLangObject {
		
		private LangObject[] parts;
		public Consecutive(LangObject... parts) {
			this.parts = parts;
		}
		
		public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
			Constructor repop = (Stack<LangObject> stack) -> {
				return stack.pop();
			};
			return utils.Utils.link(new utils.Tuple<LinkedList<LangObject>, Constructor>(utils.Utils.link(parts), (Stack<LangObject> stack) -> {
				LangObject.LangObjectListFactory.LangObjectList<LangObject> list = new LangObject.LangObjectListFactory.LangObjectList<LangObject>();
				while(stack.size() > 0) {
					list.add(stack.pop());
				}
				return list;
			}));
		}
	}
	
	public static class Or extends NonTerminalLangObject {
		
		private LangObject[] options;
		public Or(LangObject... options) {
			this.options = options;
		}

		public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
			Constructor repop = (Stack<LangObject> stack) -> {
				return stack.pop();
			};
			LinkedList<Tuple<LinkedList<LangObject>, Constructor>> list = new LinkedList<Tuple<LinkedList<LangObject>, Constructor>>();
			for(LangObject o : options) {
				list.add(new utils.Tuple<LinkedList<LangObject>, Constructor>(utils.Utils.link(o), repop));
			}
			return list;
		}
	}
	
	public static class Fragment extends TerminalLangObject {

		public static final LangObject lower = Fragment.charRange('a', 'z');
		public static final LangObject upper = Fragment.charRange('A', 'Z');
		public static final LangObject digit = Fragment.charRange('0', '9');
		public static final LangObject alpha = or(lower, upper, Fragment.any("_"));
		public static final LangObject alphanumeric = or(alpha, digit);
		public static final LangObject escape = consec(Fragment.string("\\"), Fragment.charRange((char) 0, (char) -1));
		public static final LangObject string = consec(Fragment.string("\""), or(escape, Fragment.none("\\")), Fragment.string("\""));
		
		private String grabbed;
		private Terminal terminal;
		private Fragment(Terminal terminal) {
			this.terminal = terminal;
		}
		private Fragment(String grabbed) {
			this.grabbed = grabbed;
		}
		public String toString() {
			return grabbed;
		}
		public Terminal terminal() {
			return this.terminal;
		}

		public static Fragment any(String options) {
			return new Fragment((stream.IStreamer stream) -> {
				if(stream.hasNext()) {
					char c = stream.next();
					return options.indexOf(c) >= 0 ? () -> { return new Fragment("" + c); } : null;
				}
				return null;
			});
		}
		public static Fragment none(String options) {
			return new Fragment((stream.IStreamer stream) -> {
				if(stream.hasNext()) {
					char c = stream.next();
					return options.indexOf(c) < 0 ? () -> { return new Fragment("" + c); } : null;
				}
				return null;
			});
		}
		public static Fragment string(String string) {
			return new Fragment((stream.IStreamer stream) -> {
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < string.length() && stream.hasNext(); i++) {
					sb.append(stream.next());
				}
				return sb.toString().equals(string) ? () -> { return new Fragment(string); } : null;
			});
		}
		public static Fragment charRange(char c1, char c2) {
			char low, high;
			if(c1 < c2) {
				low = c1;
				high = c2;
			}else {
				low = c2;
				high = c1;
			}
			return new Fragment((stream.IStreamer stream) -> {
				if(stream.hasNext()) {
					char c = stream.next();
					if(c >= low && c <= high) {
						return () -> { return new Fragment("" + c); };
					}
				}
				return null;
			});
		}
	}
	
	public abstract static class Token extends NonTerminalLangObject {
		
		private LangObject[] fragments;
		public Token(LangObject... fragments) {
			this.fragments = fragments;
		}
		
		protected abstract Token make(String grabbed);
		
		public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
			LinkedList<LangObject> frags = new LinkedList<LangObject>();
			for(LangObject f : fragments) {
				frags.add(f);
			}
			return utils.Utils.link(new Tuple<LinkedList<LangObject>, Constructor>(frags, (Stack<LangObject> stack) -> {
				StringBuffer sb = new StringBuffer();
				while(stack.size() > 0) {
					sb.append(stack.pop().toString());
				}
				return make(sb.toString());
			}));
		}
	}

	public static abstract class NonTerminalLangObject extends LangObject {

		public interface Constructor {
			LangObject construct(Stack<LangObject> stack);
		}

		public abstract LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules();
	}

	protected static <T extends LangObject> LangObject wopt(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.WANTED_OPTIONAL, t);
	}
	protected static <T extends LangObject> LangObject uopt(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.UNWANTED_OPTIONAL, t);
	}
	protected static <T extends LangObject> LangObject zmany(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.ZERO_MANY, t);
	}
	protected static <T extends LangObject> LangObject zfew(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.ZERO_FEW, t);
	}
	protected static <T extends LangObject> LangObject omany(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.ONE_MANY, t);
	}
	protected static <T extends LangObject> LangObject ofew(T t) {
		return new LangObjectListFactory<T>(LangObjectListFactory.Type.ONE_FEW, t);
	}
	protected static LangObject or(LangObject... options) {
		return new Or(options);
	}
	protected static LangObject consec(LangObject... parts) {
		return new Consecutive(parts);
	}

	private static class LangObjectListFactory<T extends LangObject> extends NonTerminalLangObject {

		public boolean equals(Object o) {
			if(o == null) {
				return false;
			}
			if(o instanceof LangObjectListFactory) {
				LangObjectListFactory<?> f = (LangObjectListFactory<?>) o;
				return f.type.equals(this.type) && f.object.equals(this.object);
			}
			return false;
		}
		public int hashCode() {
			return this.type.hashCode() + this.object.hashCode();
		}

		public static class LangObjectList<T extends LangObject> extends NonTerminalLangObject {

			public boolean equals(Object o) {
				if(o == null) {
					return false;
				}
				if(o instanceof LangObjectList) {
					return this.list.equals(((LangObjectList<?>) o).list);
				}
				return false;
			}
			public int hashCode() {
				return this.list.hashCode();
			}
			
			public String toString() {
				StringBuffer sb = new StringBuffer();
				for(T t : list) {
					sb.append(t.toString());
				}
				return sb.toString();
			}

			private final ArrayList<T> list = new ArrayList<T>();
			public LangObjectList() { }

			private void add(LangObject object) {
				if(object instanceof LangObjectList) {
					for(T t : ((LangObjectList<T>) object).list) {
						this.list.add(t);
					}
				}else {
					this.list.add((T) object);
				}
			}

			public final LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() { return null; }
		}

		protected static enum Type {
			UNWANTED_OPTIONAL,
			WANTED_OPTIONAL,
			ZERO_FEW,
			ZERO_MANY,
			ONE_FEW,
			ONE_MANY
		}

		public String name() {
			return String.format("%s(%s)", type.toString(), object.toString());
		}
		
		private final Type type;
		private final T object;

		public LangObjectListFactory(Type type, T object) {
			this.type = type;
			this.object = object;
		}

		public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
			switch(this.type) {
			case ONE_FEW:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object, new LangObjectListFactory<T>(Type.ZERO_FEW, object)), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							list.add(stack.pop());
							return list;
						}));
			case ONE_MANY:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object, new LangObjectListFactory<T>(Type.ZERO_MANY, object)), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							list.add(stack.pop());
							return list;
						}));
			case UNWANTED_OPTIONAL:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(), (Stack<LangObject> stack) -> {
							return new LangObjectList<T>();
						}),
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							return list;
						}));
			case WANTED_OPTIONAL:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							return list;
						}),
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(), (Stack<LangObject> stack) -> {
							return new LangObjectList<T>();
						}));
			case ZERO_FEW:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(), (Stack<LangObject> stack) -> {
							return new LangObjectList<T>();
						}),
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object, this), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							list.add(stack.pop());
							return list;
						}));
			case ZERO_MANY:
				return Utils.link(
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(object, this), (Stack<LangObject> stack) -> {
							LangObjectList<T> list = new LangObjectList<T>();
							list.add(stack.pop());
							list.add(stack.pop());
							return list;
						}),
						new Tuple<LinkedList<LangObject>, Constructor>(Utils.link(), (Stack<LangObject> stack) -> {
							return new LangObjectList<T>();
						}));
			default:
				return null;
			}
		}
	}
}
