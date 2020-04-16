package parser_old5;

import parser.utils.Utils;

public class Tester {

	public static void main(String[] args) {
		Parser p = new Parser();
		WrapperRule add = p.rule(), mult = p.rule(), exp = p.rule(), plus = p.rule(), mul = p.rule(), term = p.rule();
		add.set(p.rule(Utils.link(mult, plus, mult), Utils.link(mult)));
		add.setName("add");
		mult.set(p.rule(Utils.link(exp, mul, exp), Utils.link(exp)));
		mult.setName("mult");
		exp.set(p.rule(Utils.link(add), Utils.link(term)));
		exp.setName("exp");
		plus.setName("+");
		mul.setName("*");
		term.setName("term");
		System.out.println(p);
		p.compile(); // infinite recursion
		System.out.println(p);
	}
}
