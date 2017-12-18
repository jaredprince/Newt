
public class RuntimeError extends RuntimeException {

	/**
	 * Not sure what this does, but it removes the warning.
	 */
	private static final long serialVersionUID = 6299130057608538903L;
	
	static final int UNDEFINED_VARIABLE = 0;
	static final int VARIABLE_ALREADY_DEFINED = 1;
	static final int CANNOT_CAST = 2;
	
	public RuntimeError(Token t, int type){
		if(type == UNDEFINED_VARIABLE){
			System.err.println("Undefined variable: " + t.value);
		} else if (type == VARIABLE_ALREADY_DEFINED){
			System.err.println("Variable already defined: " + t.value);
		} else if(type == CANNOT_CAST) {
			System.err.println("Cannot cast to type: " + t.value);
		}
		
		System.err.println("Line: " + t.line_loc); 
	}
}
