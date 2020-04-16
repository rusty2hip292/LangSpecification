package parser_old5;

public interface IStreamer {

	boolean hasNext();
	char next();
	void setFailPoint();
	void fail();
}
