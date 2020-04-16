package parser_old;

public interface IStreamer {

	long id();
	boolean hasNext();
	char next();
	void setFailPoint();
	void fail();
}
