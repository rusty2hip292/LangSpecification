package assembly;

import java.util.LinkedList;
import java.util.List;

public class Optimizer {

	private static final List<Optimizer> _all = new LinkedList<Optimizer>();
	public static final Iterable<Optimizer> optimizers = _all;
	
	private static interface OptimizationPass {
		List<Instruction> pass(List<Instruction> instructions);
	}
	
	private final OptimizationPass pass;
	public Optimizer(OptimizationPass pass) {
		Optimizer._all.add(this);
		this.pass = pass;
	}
	
	public List<Instruction> optimize(List<Instruction> instructions) {
		return this.pass.pass(instructions);
	}
}
