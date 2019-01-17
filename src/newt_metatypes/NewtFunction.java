package newt_metatypes;

import java.util.ArrayList;

import interpreter.Environment;
import interpreter.Interpreter;
import interpreter.Stmt;
import interpreter.Token;
import interpreter.Stmt.Function;

class NewtFunction implements NewtCallable {
	private int arity;
	private ArrayList<Stmt> statements;
	private ArrayList<Token> params;

	NewtFunction(Stmt.Function func) {
		this.arity = func.parameters.size();
		this.statements = func.body.statements;
		this.params = func.parameters;
	}

	@Override
	public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
		
		//create a new environment scope
		Environment previous = interpreter.getEnvironment();
		interpreter.setEnvironment(new Environment(previous));
		Environment current = interpreter.getEnvironment();
		
		//define the arguments within the scope
		for(int i = 0; i < params.size(); i++) {
			current.define(params.get(i), arguments.get(i));
		}
		
		//interpret the function statements
		interpreter.interpret(statements);
		
		//reset the scope
		interpreter.setEnvironment(previous);
		
		Object returnValue = interpreter.getReturnValue();
		interpreter.setReturnValue(null);
	
		return returnValue;
	}

	@Override
	public int arity() {
		return arity;
	}
}