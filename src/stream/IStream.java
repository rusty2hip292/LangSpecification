package stream;

public interface IStream extends IResetable {

	char peek();
	char next();
	boolean hasNext();
}
