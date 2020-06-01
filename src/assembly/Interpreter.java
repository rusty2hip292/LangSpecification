package assembly;

import java.util.LinkedList;
import java.util.List;

public class Interpreter {
	
	public static void main(String[] args) {
		Interpreter.interpret(Loader.load("test_asm.asm"));
	}

	public static List<Instruction> load(List<List<String>> asm) {
		List<Instruction> loaded = new LinkedList<Instruction>();
		for(var line : asm) {
			for(var f : Instruction.factories) {
				Instruction i = f.produce(line);
				if(i != null) {
					loaded.add(i);
					break;
				}
			}
			new Exception("failed to parse line " + line).printStackTrace();
			return null;
		}
		return loaded;
	}
	public static void interpret(List<List<String>> asm) {
		var loaded = load(asm);
		if(loaded == null) {
			return;
		}
		List<Instruction> optimized;
		do {
			optimized = loaded;
			for(var o : Optimizer.optimizers) {
				optimized = o.optimize(optimized);
			}
		}while(!optimized.equals(loaded));
		if(Verifier.verify(optimized)) {
			Processor.run(optimized);
		}
	}
}
