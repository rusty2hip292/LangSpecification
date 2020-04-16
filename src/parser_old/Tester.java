package parser_old;

public class Tester {

	public static void main(String[] args) {
		IStreamer s = new Streamer("aa aa");
		Parser recursiveA = Parser.temp();
		recursiveA.setLambda(Parser.or(Parser.consecutive(recursiveA, recursiveA), Parser.string("a")));
		Parser.parse(s,
				recursiveA
			);
		System.out.println(s);
	}
}
