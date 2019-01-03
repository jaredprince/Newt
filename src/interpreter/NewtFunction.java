package interpreter;

import java.util.ArrayList;

class NewtFunction implements NewtCallable {
	private final Stmt.Function declaration;

	NewtFunction(Stmt.Function declaration) {
		this.declaration = declaration;
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