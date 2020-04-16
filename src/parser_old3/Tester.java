package parser_old3;

import parser.utils.JSONWriter;

public class Tester {

	public static ParserState specParser(JSONWriter json) {
		ParserState escapeSequence = Parser.consecutive(Parser.string("\\"), Parser.anychar);
		ParserState string = Parser.consecutive(Parser.string("\""), Parser.zeroMany(Parser.or(escapeSequence, Parser.exceptChar("\\"))), Parser.string("\""));
		ParserState charrange = Parser.consecutive(Parser.string("["), Parser.zeroMany(Parser.or(escapeSequence, Parser.exceptChar("]"))), Parser.string("]"));
		ParserState identifier = Parser.consecutive(Parser.alpha, Parser.zeroMany(Parser.alphanumeric));
		ParserState name = Parser.identity(identifier);
		ParserState left = Parser.consecutive(Parser.whitespace, name.saveToken(json, "rulename"), Parser.whitespace, Parser.string(":"), Parser.whitespace);
		ParserState modifier = Parser.or(Parser.string("??"), Parser.string("*?"), Parser.string("+?"), Parser.string("+"), Parser.string("*"), Parser.string("?"));
		UnsetParserState rule = Parser.unset();
		rule.set(Parser.consecutive(
				Parser.or(
						Parser.consecutive(Parser.string("("), rule, Parser.string(")")),
						identifier,
						string,
						charrange
				))
				).leftRecursive(Parser.or(modifier, Parser._whitespace));
		ParserState right = Parser.oneMany(rule);
		ParserState spec = Parser.oneMany(Parser.consecutive(left, right).object(json)).array(json);
		return spec;
	}
	
	public static void main(String[] args) {
		IStreamer paren = new Streamer(" a: (((a)+ +)) \nb : [a-z\\]] \"test\" ");
		JSONWriter json = new JSONWriter();
		Parser.parse(paren, specParser(json));
		System.out.println(json);
	}
	
	public static void test2() {
		IStreamer paren = new Streamer(" a: (((a)+ +)) \nb : [a-z\\]] \"test\" ");
		Parser.parse(paren, specParser(new JSONWriter()).textGrabber((String s) -> {System.out.println(s);}));
		System.out.println(paren);
	}
	
	public static void test1() {
		IStreamer stream = new Streamer("testaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		long start = System.currentTimeMillis();
		Parser.parse(stream, Parser.oneMany(Parser.or(Parser.oneMany(Parser.charRange(Parser.range('d', 'a'))), Parser.string("test"))));
		System.out.println(System.currentTimeMillis() - start);
	}
}
