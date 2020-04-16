package parser_old2;

public interface IStreamer {

	boolean hasNext();
	char next();
	void setFailPoint();
	void fail();
}
