package interpreter;

import java.util.ArrayList;

public interface NewtCallable {
	abstract Object call(Interpreter interpreter, ArrayList<Object> arguments);
	abstract int arity();
}
