package filegen;

import java.util.ArrayList;

public class LangToken extends ClassFileGenerator {
	
	private final String name;
	private final ArrayList<LangFragment> fragments = new ArrayList<LangFragment>();
	public LangToken(String name) {
		this.name = name;
	}
	public void addFragment(LangFragment frag) {
		this.fragments.add(frag);
	}
	
	public static void main(String[] args) {
		try {
			new LangToken("test_token").generate("src", "test");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	protected String name() {
		return this.name;
	}
}
