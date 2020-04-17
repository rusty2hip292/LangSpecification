package parser.generator;

import parser.RuleGenerator.ICollapser;
import parser.RuleGenerator.IList;
import parser.RuleGenerator.IRulePhase;
import parser.RuleGenerator.ITermPhase2;

public abstract class RuleGeneratorBuilder<T> {

	/**
	 * @return
	 */
	public abstract ITermPhase2<T> termRule();
	/**
	 * @return
	 */
	public abstract IRulePhase<T> ruleRule();
	/**
	 * @return
	 */
	public abstract IList<T> arrayRule();
	/**
	 * @return
	 */
	public abstract ICollapser<T> collapserRule();
}
