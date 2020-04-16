package parser_old;

public class BootstrapMain {

	public static void main(String[] args) {
		IStreamer stream = new Streamer(
			"\n" +
			"spec : rule_line+\n" +
			"rule_line : name \":\" rule_list\n" +
			"rule_list : \n" +
			"rule : \"(\" rule_list \")\" | \"(\" rule \")\" | rule modifier | base_rule\n" +
			"modifier : \"*?\" | \"+?\" | \"??\" | \"*\" | \"+\" | \"?\"\n" +
			"base_rule : name | string | regex+ \n" +
			"name : [a-zA-Z_][a-zA-Z_0-9]\n" +
			"string : \"\\\"\" .* \"\\\"\"\n" +
			"regex : [\\S]?.*?[\\S]\n"
		);
		
	}
}
