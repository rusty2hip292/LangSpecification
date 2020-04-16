package parser.utils;

import java.util.ArrayList;
import java.util.Stack;

public class JSONWriter {
	private ArrayList<IJSONLambda> log = new ArrayList<IJSONLambda>();
	private StringBuffer output = new StringBuffer();
	private String indent = "";
	private void indent() {
		indent += "\t";
	}
	private void dedent() {
		if(indent.length() > 0) {
			indent = indent.substring(1);
		}
	}
	private void bad() {
		throw new IllegalArgumentException(state().toString());
	}
	private static String json(String value) {
		return "\"" + Utils.escape(value) + "\"";
	}
	public String toString() {
		return this.output.toString();
	}

	/**
	 * this is what I currently have
	 */
	private static enum JSONState {
		INITIAL,
		EMPTY_OBJ,
		MULTI_OBJ,
		EMPTY_ARR,
		MULTI_ARR,
		KEY,
		INVALID,
	}
	private Stack<JSONState> state = new Stack<JSONState>();
	private JSONState state() {
		if(state.size() == 0) {
			state.push(JSONState.INITIAL);
		}
		return state.peek();
	}
	private void swap(JSONState currentState) {
		state.pop();
		state.push(currentState);
	}
	private void swap(JSONState returnState, JSONState currentState) {
		swap(returnState);
		state.push(currentState);
	}

	private JSONWriter write(String toWrite) {
		this.output.append(toWrite);
		return this;
	}

	private void open(boolean obj) {
		this.log.add((JSONWriter json) -> {
			json.open(obj);
		});
		String brace = obj ? "{" : "[";
		JSONState returnState = state();
		switch(state()) {
		case INITIAL:
			this.write(brace);
			returnState = JSONState.INVALID;
			break;
		case KEY:
			this.write(brace);
			returnState = JSONState.MULTI_OBJ;
			break;
		case EMPTY_ARR:
			this.write("\n" + indent + brace);
			returnState = JSONState.MULTI_ARR;
			break;
		case MULTI_ARR:
			this.write(",\n" + indent + brace);
			returnState = JSONState.MULTI_ARR;
			break;
		default:
			bad();
		}
		indent();
		swap(returnState, obj ? JSONState.EMPTY_OBJ : JSONState.EMPTY_ARR);
	}
	public void openObject() {
		open(true);
	}
	public void openArray() {
		open(false);
	}

	public void addKey(String key) {
		this.log.add((JSONWriter json) -> {
			json.addKey(key);
		});
		String format = null;
		switch(state()) {
		case EMPTY_OBJ:
			format = "\n%s%s : ";
			break;
		case MULTI_OBJ:
			format = ",\n%s%s : ";
			break;
		default:
			bad();
		}
		swap(JSONState.KEY);
		this.write(String.format(format, indent, json(key)));
	}

	public void close() {
		this.log.add((JSONWriter json) -> {
			json.close();
		});
		dedent();
		switch(state()) {
		case EMPTY_OBJ:
			this.write(indent + "}");
			break;
		case EMPTY_ARR:
			this.write(indent + "]");
			break;
		case MULTI_OBJ:
			this.write("\n" + indent + "}");
			break;
		case MULTI_ARR:
			this.write("]");
			break;
		default:
			bad();
		}
		state.pop();
	}

	public void value(String val) {
		this.log.add((JSONWriter json) -> {
			json.value(val);
		});
		switch(state()) {
		case KEY:
			this.write(json(val));
			swap(JSONState.MULTI_OBJ);
			break;
		case EMPTY_ARR:
			this.write(json(val));
			swap(JSONState.MULTI_ARR);
			break;
		case MULTI_ARR:
			this.write(", " + json(val));
			break;
		default:
			bad();
		}
	}

	public void debug() {
		JSONWriter json = new JSONWriter();
		int i = 0;
		try {
			for(IJSONLambda op : this.log) {
				System.out.println(json.state);
				op.lambda(json);
				System.out.println(i++ + " : " + json);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

interface IJSONLambda {
	void lambda(JSONWriter j);
}

/*
public class JSONWriter {

	private StringBuffer output = new StringBuffer();
	private Stack<Boolean> needComma = new Stack<Boolean>(), object = new Stack<Boolean>();
	private String indent = "";

	public JSONWriter clone() {
		JSONWriter clone = new JSONWriter();
		clone.output.append(output.toString());
		clone.needComma = Utils.static_cast(this.needComma.clone(), this.needComma);
		clone.object = Utils.static_cast(this.object.clone(), this.object);
		clone.indent = this.indent;
		return clone;
	}

	private static void ok(boolean ok) {
		if(!ok) {
			throw new IllegalArgumentException("Attempted to add value to object, or key-value pair to array.");
		}
	}
	private boolean inObject() {
		return object.peek();
	}
	private void open(boolean object) {
		if(this.needComma.size() != 0) {
			output.append((this.needComma(false) ? ",\n" : "\n") + indent);
		}
		needComma.push(false);
		this.object.push(object);
		indent += "\t";
	}
	private boolean needComma(boolean swap) {
		if(swap) {
			boolean b = needComma.pop();
			needComma.push(true);
			return b;
		}
		return needComma.peek();
	}
	private void indent() {
		if(needComma(true)) {
			output.append(",\n" + indent);
		}else {
			output.append("\n" + indent);
		}
	}
	public void addKey(String key) {
		addKey(key, "");
	}
	private void addKey(String key, String value) {
		ok(inObject());
		indent();
		output.append("\"" + key + "\" : " + value);
	}
	public void value(String key, String str) {
		addKey(key, "\"" + escape(str) + "\"");
	}
	private void addValue(String str) {
		System.out.println(this);
		ok(!inObject());
		indent();
		output.append(str);
	}
	public void value(String str) {
		addValue("\"" + escape(str) + "\"");
	}
	public void value(String key, boolean b) {
		value(key, "" + b);
	}
	public void value(boolean b) {
		addValue("" + b);
	}
	public void value(String key, double d) {
		value(key, "" + d);
	}
	public void value(double d) {
		addValue("" + d);
	}
	public void openObject() {
		open(true);
		output.append("{");
	}
	public void openArray() {
		open(false);
		output.append("[");
	}
	public void close() {
		indent = indent.substring(1);
		if(needComma(true)) {
			output.append("\n" + indent);
		}
		needComma.pop();
		if(object.pop()) {
			output.append("}");
		}else {
			output.append("]");
		}
	}

	public String toString() {
		return output.toString();
	}

	public static IJSONLambda pushKeyBoolean(String key, boolean b) {
		return (JSONWriter j) -> {
			j.value(key, b);
		};
	}
	public static IJSONLambda pushKeyString(String key, String s) {
		return (JSONWriter j) -> {
			j.value(key, s);
		};
	}
	public static IJSONLambda pushKeyNumber(String key, double b) {
		return (JSONWriter j) -> {
			j.value(key, b);
		};
	}

	public static IJSONLambda pushBoolean(boolean b) {
		return (JSONWriter j) -> {
			j.value(b);
		};
	}
	public static IJSONLambda pushString(String s) {
		return (JSONWriter j) -> {
			j.value(s);
		};
	}
	public static IJSONLambda pushNumber(double b) {
		return (JSONWriter j) -> {
			j.value(b);
		};
	}
	public static IJSONLambda pushKey(String key) {
		return (JSONWriter j) -> {
			j.addKey(key);
		};
	}
	public static IJSONLambda object() {
		return (JSONWriter j) -> {
			j.openObject();
		};
	}
	public static IJSONLambda array() {
		return (JSONWriter j) -> {
			j.openArray();
		};
	}
	public static IJSONLambda end() {
		return (JSONWriter j) -> {
			j.close();
		};
	}

	private static IJSONLambda list(IJSONLambda... lambdas) {
		return (JSONWriter j) -> {
			for(IJSONLambda lambda : lambdas) {
				lambda.lambda(j);
			}
		};
	}
	private static final IJSONLambda[] testlambdas;
	static {
		IJSONLambda emptyObject = list(object(), end());
		IJSONLambda emptyArray = list(array(), end());
		IJSONLambda addKeyString = list(pushKeyString("key", "value"));
		IJSONLambda addString = list(pushString("value"));
		IJSONLambda addStringInObject = list(object(), addString, end());
		IJSONLambda fullarray = list(array(), pushString("string1"), pushBoolean(true), pushNumber(1), object(), end(), pushNumber(420.69), object(), addKeyString, end(), end());
		IJSONLambda nested = list(object(), pushKey("obj"), object(), pushKey("arr"), array(), end(), end(), pushKey("arr"), array(), object(), end(), array(), array(), end(), end(), end(), end());
		IJSONLambda nestedarray = list(array(), array(), array(), end(), array(), end(), end(), array(), end(), end());
		IJSONLambda objectsibs = list(array(), object(), end(), object(), end(), end());
		testlambdas = new IJSONLambda[] {
				emptyObject,
				emptyArray,
				addStringInObject,
				nested,
				nestedarray,
				objectsibs,
				fullarray,
		};
	}

//	public static void main(String[] args) {
//		for(IJSONLambda lambda : testlambdas) {
//			JSONWriter j = new JSONWriter();
//			try {
//				lambda.lambda(j);
//				System.out.println(j);
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
 */