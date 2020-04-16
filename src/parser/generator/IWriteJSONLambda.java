package parser.generator;

import java.util.Stack;
import parser.utils.JSONWriter;

/**
 * a lambda expression, (JSONWriter writer, Stack<>IWriteJSONLambda<> stack) -> void
 */
public interface IWriteJSONLambda {
	void write(JSONWriter writer);
}
