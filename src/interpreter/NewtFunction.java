package interpreter;

import java.util.ArrayList;

class NewtFunction implements NewtCallable {
	private int arity;
	private ArrayList<Stmt> statements;
	private ArrayList<Token> params;

	NewtFunction(Stmt.Function func) {
		this.arity = func.parameters.size();
		this.statements = func.block.statements;
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
		
		//TODO: Handle return parameters. This will be tricky since the interpret function returns void. That may need to be changed. We could maybe get around that by declaring the return param as a special variable, only accessed when the function is called.
		
		return null;
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return arity;
	}
}