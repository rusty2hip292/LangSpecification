package parser.generator.loader;

public abstract class LanguageObject<T extends ILanguageObject> implements ILanguageObject {
	
	
	
	public static void main(String[] args) {
		Object[] arr = new String[]{"A"};
		Class<?> c = arr.getClass();
		arr = (Object[]) arr;
	}
	public abstract String getText();
}

interface ILanguageObject { }

interface IMakeLanguageObject {
	/**
	 * @return a LanguageObject or a LanguageObject[], 
	 */
	Object make();
}
