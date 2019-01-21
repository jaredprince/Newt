package newt_metatypes;

import java.util.HashMap;
import java.util.Map;

import interpreter.Interpreter;
import interpreter.RuntimeError;
import interpreter.Stmt.Declare;
import interpreter.Stmt.Function;
import interpreter.Token;

public class NewtInstance {

	private NewtClass newtClass;
	private final Map<String, Object> fields = new HashMap<>();

	public NewtInstance(NewtClass newtClass) {
		this.newtClass = newtClass;
		
		//make a new temporary interpreter
		Interpreter interpreter = new Interpreter();
		
		for(Declare declaration : newtClass.fields) {
			fields.put(declaration.name.lexeme, declaration.value == null ? null : interpreter.evaluate(declaration.value));
		}
	}

	@Override
	public String toString() {
		return newtClass.name + " instance";
	}

	public Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}
		
		NewtFunction method = newtClass.findMethod(this, name.lexeme);
	    if (method != null) return method; 

		throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
	}
	
	public void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}
	
	public String getClassName() {
		return newtClass.name;
	}
}
