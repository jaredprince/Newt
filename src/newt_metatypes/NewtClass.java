package newt_metatypes;

import java.util.ArrayList;

import interpreter.Interpreter;
import interpreter.Stmt;
import interpreter.Stmt.Function;

public class NewtClass implements NewtCallable {
	final String name;
	ArrayList<Function> methods;

	NewtClass(String name, ArrayList<Function> methods) {
		this.name = name;
		this.methods = methods;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return 0;
	}
}