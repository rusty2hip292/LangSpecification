package parser_old3;

public interface IStreamer {

	boolean hasNext();
	int index();
	char next();
	void setFailPoint();
	void fail();
	String read(int startindex, int endindex);
}
