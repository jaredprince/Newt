
public class RuntimeError extends RuntimeException {

	static final int UNDEFINED_VARIABLE = 0;
	static final int VARIABLE_ALREADY_DEFINED = 1;
	
	public RuntimeError(Token t, int type){
		if(type == UNDEFINED_VARIABLE){
			System.err.println("Undefined variable: " + t.value);
		} else if (type == VARIABLE_ALREADY_DEFINED){
			System.err.println("Variable already defined: " + t.value);
		}
	}
}
