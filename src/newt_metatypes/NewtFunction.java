package newt_metatypes;

import java.util.ArrayList;

import interpreter.Environment;
import interpreter.Interpreter;
import interpreter.Stmt;
import interpreter.Token;
import interpreter.Stmt.Function;

public class NewtFunction implements NewtCallable {
	private int arity;
	private ArrayList<Stmt> statements;
	private ArrayList<Token> params;
	private Function func;

	private final Environment closure;

	public NewtFunction(Function func, Environment closure) {
		this.arity = func.parameters.size();
		this.statements = func.body.statements;
		this.params = func.parameters;
		this.func = func;
		this.closure = closure;
	}

	public NewtFunction bind(NewtInstance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new NewtFunction(func, environment);
	}

	@Override
	public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
		
		// create a new environment scope
		interpreter.setEnvironment(new Environment(closure));
		Environment current = interpreter.getEnvironment();

		// define the arguments within the scope
		for (int i = 0; i < params.size(); i++) {
			current.define(params.get(i), arguments.get(i));
		}

		Object returnVal = null;
		
		try {
			// interpret the function statements
			interpreter.interpret(statements);
		} catch (NewtReturn value) {
			returnVal = value.value;
		}

		// reset the scope
		interpreter.setEnvironment(closure);

		return returnVal;
	}

	@Override
	public int arity() {
		return arity;
	}
	
	public ArrayList<Token> getParams(){
		return params;
	}
}