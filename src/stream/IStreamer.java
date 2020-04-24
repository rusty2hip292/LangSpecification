package stream;

import resetable.IResetable;

public interface IStreamer extends resetable.IResetable {

	boolean hasNext();
	char next();
	char peek();
}
