package istreamer;

public interface IStreamer extends iresetable.IResetable {

	boolean hasNext();
	char next();
	char peek();
}
