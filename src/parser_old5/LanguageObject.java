package parser_old5;

import parser.utils.Utils;

public abstract class LanguageObject<T extends LanguageObject<? super T>> {

	public static int a() {
		return 5;
	}
	
	public static void read() {
		
	}
	
	protected LanguageObject(LanguageObject<?> n) { }
	
	public static void main(String[] args) {
		System.out.println(LanguageObject.a());
		System.out.println(If.a());
		System.out.println(If2.a());
	}
}

class If extends LanguageObject<If> {
	
	public If() {
		super(null);
	}
}

class If2 extends If {
	
	public static int a() {
		return 7;
	}
	
	public If2() {
		System.out.println("here");
	}
}
