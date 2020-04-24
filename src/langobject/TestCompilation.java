package langobject;

import java.util.LinkedList;
import java.util.Stack;

import utils.Tuple;
import utils.Utils;

public class TestCompilation {

	public static void main(String[] args) {
		parser.Parser<AddExpression> p = parser.Parser.parser(new AddExpression());
		for(String s : new String[] {"77", "77", "5*5", "55*56", "5+5", "6*6", "69+420*69*69*420+69+420", "7+777*7*7*7+7*77"}) {
			System.out.println(p.parse(s));
		}
	}
}

class Expression extends LangObject.NonTerminalLangObject {

	public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
		return Utils.link(
				new utils.Tuple<LinkedList<LangObject>, Constructor>(
						Utils.link(new AddExpression()), (Stack<LangObject> stack) -> {
							return null;
						})
				);
	}
}

class AddExpression extends Expression {

	public AddExpression() { }
	private MultExpression mult;
	public AddExpression(MultExpression m1) {
		this.mult = m1;
	}
	private AddExpression a1, a2;
	public AddExpression(AddExpression a1, AddExpression a2) {
		this.a1 = a1;
		this.a2 = a2;
	}
	
	public String toString() {
		return String.format("(add %s %s %s)", a1, a2, mult);
	}
	
	public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
		return Utils.link(
				new utils.Tuple<LinkedList<LangObject>, Constructor>(
						Utils.link(new AddExpression(), Fragment.string("+"), new AddExpression()), (Stack<LangObject> stack) -> {
							var m1 = (AddExpression) stack.pop();
							stack.pop();
							var m2 = (AddExpression) stack.pop();
							return new AddExpression(m1, m2);
						}),
				new utils.Tuple<LinkedList<LangObject>, Constructor>(
						Utils.link(new MultExpression()), (Stack<LangObject> stack) -> {
							return new AddExpression((MultExpression) stack.pop());
						})
				);
	}
}

class MultExpression extends Expression {

	public MultExpression() { }
	private MultExpression m1, m2;
	public MultExpression(MultExpression m1, MultExpression m2) {
		this.m1 = m1;
		this.m2 = m2;
	}
	private Term t;
	public MultExpression(Term t) {
		this.t = t;
	}
	
	public String toString() {
		return String.format("(mult %s %s %s)", m1, m2, t);
	}
	
	public LinkedList<Tuple<LinkedList<LangObject>, Constructor>> parse_rules() {
		return Utils.link(
				new utils.Tuple<LinkedList<LangObject>, Constructor>(
						Utils.link((LangObject) new MultExpression(), (LangObject) Fragment.string("*"), (LangObject) new MultExpression()), (Stack<LangObject> stack) -> {
							var m1 = (MultExpression) stack.pop();
							stack.pop();
							var m2 = (MultExpression) stack.pop();
							return new MultExpression(m1, m2);
						}),
				new utils.Tuple<LinkedList<LangObject>, Constructor>(
						Utils.link(new Term()), (Stack<LangObject> stack) -> {
							return new MultExpression((Term) stack.pop());
						})
				);
	}
}

class Term extends LangObject.Token {

	public Term() {
		super(LangObject.omany(LangObject.Fragment.digit));
	}

	private String value;
	private Term(String value) {
		this.value = value;
	}
	protected Token make(String grabbed) {
		return new Term(grabbed);
	}
	public String toString() {
		return value;
	}
}
