package parser;

import java.util.ArrayList;

public interface LangObject {
	void enter();
	void visit();
	void exit();
}

class ArrayLangObject<T extends LangObject> implements LangObject {
	
	public void enter() { }
	public void exit() { }
	public void visit() {
		for(T t : list) {
			t.enter();
			t.visit();
			t.exit();
		}
	}
	
	private final ArrayList<T> list;
	public ArrayLangObject(ArrayList<T> list) {
		this.list = list;
	}
}
