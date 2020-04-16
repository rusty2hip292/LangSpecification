package parser.generator.loader;

import java.util.HashMap;

import parser.istreamer.IStreamer;

public abstract class LanguageObject<T extends ILanguageObject> {

	public final LanguageObject<T> make(IStreamer stream) {
		return null;
	}
}

interface ILanguageObject {
	
}

interface IVoidLambda {
	void make();
}
