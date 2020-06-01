package assembly;

import java.util.LinkedList;
import java.util.List;

public abstract class Instruction {

	public static final Iterable<InstructionFactory> factories = InstructionFactory.all;
	
	/**
	 * @return false if program terminated
	 */
	public abstract boolean execute(Processor processor);
	
	public boolean equals(Object o) {
		return this == o;
	}

	public static class InstructionFactory {
		
		private static final List<InstructionFactory> all = new LinkedList<InstructionFactory>();
		
		private static interface InstructionBuilder {
			Instruction build(List<String> line);
		}
		
		private final InstructionBuilder builder;
		protected InstructionFactory(InstructionBuilder builder) {
			InstructionFactory.all.add(this);
			this.builder = builder;
		}
		public Instruction produce(List<String> line) {
			return builder.build(line);
		}
	}
}
