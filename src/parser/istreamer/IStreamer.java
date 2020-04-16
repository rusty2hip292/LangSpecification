package parser.istreamer;

public interface IStreamer {

	boolean hasNext();
	char next();
	char peek();
	void setFailPoint();
	void fail();
}
