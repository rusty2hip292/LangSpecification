package parser_old2;

public class Tester {

	public static ParserState specParser() {
		ParserState identifier = Parser.consecutive(Parser.alpha, Parser.zeroMany(Parser.alphanumeric));
		ParserState name = Parser.identity(identifier);
		ParserState left = Parser.consecutive(Parser.whitespace, name, Parser.whitespace, Parser.string(":"));
		ParserState modifier = Parser.or(Parser.string("??"), Parser.string("*?"), Parser.string("+?"), Parser.string("+"), Parser.string("*"), Parser.string("?"));
		UnsetParserState rule = Parser.unset();
		rule.set(Parser.or(
				Parser.consecutive(Parser.string("("), rule, Parser.string(")")),
				identifier,
				Parser.consecutive(Parser.alert("here!!"), rule, modifier),
				Parser.consecutive(Parser.whitespace, rule, Parser.whitespace)
		));
		ParserState right = Parser.oneMany(rule);
		ParserState spec = Parser.oneMany(Parser.consecutive(left, right));
		return spec;
	}
	
	public static void main(String[] args) {
		IStreamer paren = new Streamer(" a: (((a)))");
		Parser.parse(paren, specParser());
	}
	
	public static void test1() {
		IStreamer stream = new Streamer("testaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		long start = System.currentTimeMillis();
		Parser.parse(stream, Parser.oneMany(Parser.or(Parser.oneMany(Parser.charRange(Parser.range('d', 'a'))), Parser.string("test"))));
		System.out.println(System.currentTimeMillis() - start);
	}
}
