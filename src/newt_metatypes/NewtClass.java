package newt_metatypes;

import java.util.ArrayList;

import interpreter.Interpreter;
import interpreter.Stmt.Declare;
import interpreter.Stmt.Function;

public class NewtClass implements NewtCallable {
	final String name;
	ArrayList<Function> methods;
	ArrayList<Declare> fields;

	public NewtClass(String name, ArrayList<Function> methods, ArrayList<Declare> fields) {
		this.name = name;
		this.methods = methods;
		this.fields = fields;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
		NewtInstance instance = new NewtInstance(this);
		return instance;
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public NewtFunction findMethod(NewtInstance instance, String name) {
//		if (methods.containsKey(name)) {
//			return methods.get(name);
//		}

		return null;
	}
}