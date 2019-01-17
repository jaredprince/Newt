package newt_metatypes;

import java.util.ArrayList;

import interpreter.Interpreter;

public interface NewtCallable {
	abstract Object call(Interpreter interpreter, ArrayList<Object> arguments);
	abstract int arity();
}
