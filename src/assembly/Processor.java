package assembly;

import java.util.List;

public class Processor {

	public final Memory memory = new Memory();
	public int pc = 0;
	private Processor() { }

	public static void run(List<Instruction> prgm) {
		var processor = new Processor();
		while(processor.execute(prgm));
	}

	boolean execute(List<Instruction> prgm) {
		return prgm.get(this.pc).execute(this);
	}
}

class Memory {
	
}
