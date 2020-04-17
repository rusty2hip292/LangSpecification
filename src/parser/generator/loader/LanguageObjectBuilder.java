package parser.generator.loader;

import java.util.Stack;

import parser.RuleGenerator.ICollapser;
import parser.RuleGenerator.IList;
import parser.RuleGenerator.IRulePhase;
import parser.RuleGenerator.ITermPhase2;

public final class LanguageObjectBuilder extends parser.generator.RuleGeneratorBuilder<IMakeLanguageObject> {

	public ITermPhase2<IMakeLanguageObject> termRule() {
		return null;
	}

	public IRulePhase<IMakeLanguageObject> ruleRule() {
		return null;
	}

	public IList<IMakeLanguageObject> arrayRule() {
		return null;
	}

	public ICollapser<IMakeLanguageObject> collapserRule() {
		return null;
	}
}
