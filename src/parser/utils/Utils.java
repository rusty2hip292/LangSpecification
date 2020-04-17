package parser.utils;

import java.util.LinkedList;
import java.util.Stack;

public class Utils {

	public static <T> LinkedList<T> link(T... ts) {
		LinkedList<T> temp = new LinkedList<T>();
		for(T t : ts) {
			temp.add(t);
		}
		return temp;
	}

	public static <T> T static_cast(Object o, T type) {
		return (T) o;
	}

	public static <T> LinkedList<LinkedList<T>> insert(LinkedList<LinkedList<T>> normal, int row, int col, LinkedList<LinkedList<T>> toInsert) {
		normal = (LinkedList<LinkedList<T>>) normal.clone();
		LinkedList<T> removed = normal.remove(row);
		for(LinkedList<T> option : toInsert) {
			LinkedList<T> newoption = new LinkedList<T>();
			for(int c = 0; c < removed.size(); c++) {
				if(c != col) {
					newoption.add(removed.get(c));
				}else {
					for(T t : option) {
						newoption.add(t);
					}
				}
			}
			normal.add(row++, newoption);
		}
		return normal;
	}

	public static <T> Stack<T> flip(Stack<T> stack, int num) {
		Stack<T> flipped = new Stack<T>();
		for(int i = 0; i < num; i++, flipped.push(stack.pop()));
		return flipped;
	}

	public static void main(String[] args) {
		LinkedList<LinkedList<Integer>> test_ints = link(link(1, 2, 3), link(2, 3, 4), link(3, 4, 5));
		System.out.println(insert(test_ints, 1, 1, test_ints));
		System.out.println(test_ints);
	}

	public static void printStackTrace() {
		try {
			throw new Exception();
		}catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static String escape(String string) {
		StringBuffer sb = new StringBuffer();
		for(char c : string.toCharArray()) {
			switch(c) {
			case('\"'):
				sb.append("\\\"");
			break;
			case('\\'):
				sb.append("\\\\");
			break;
			case('/'):
				sb.append("\\/");
			break;
			case('\b'):
				sb.append("\\b");
			break;
			case('\f'):
				sb.append("\\f");
			break;
			case('\n'):
				sb.append("\\n");
			break;
			case('\r'):
				sb.append("\\r");
			break;
			case('\t'):
				sb.append("\\t");
			break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static void delay(long millis) {
		try {
			Thread.sleep(millis);
		}catch(Exception e) { }
	}

	public interface IGetTime {
		long time();
	}
	public static IGetTime timer() {
		long start = System.currentTimeMillis();
		return () -> {
			return System.currentTimeMillis() - start;
		};
	}
	
	public static <T> void copyin(Object[] initial, T[] type) {
		if(type.length < initial.length) {
			throw new IllegalArgumentException("Array sizes are not the same.");
		}
		for(int i = 0; i < initial.length; i++) {
			type[i] = (T) initial[i];
		}
		for(int i = initial.length; i < type.length; i++) {
			type[i] = null;
		}
	}
}
