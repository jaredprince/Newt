
public class RuntimeError extends RuntimeException {

	/**
	 * Not sure what this does, but it removes the warning.
	 */
	private static final long serialVersionUID = 6299130057608538903L;
	
	static final int UNDEFINED_VARIABLE = 0;
	static final int VARIABLE_ALREADY_DEFINED = 1;
	static final int CANNOT_CAST = 2;
	static final int MISMATCHED_ARGUMENTS = 3;
	static final int CANNOT_ASSIGN_TYPE = 4;
	
	public RuntimeError(Token t, int type){
		if(type == UNDEFINED_VARIABLE){
			System.err.println("Undefined variable: " + t.value);
		} else if (type == VARIABLE_ALREADY_DEFINED){
			System.err.println("Variable already defined: " + t.value);
		} else if(type == CANNOT_CAST) {
			System.err.println("Cannot cast to type: " + t.value);
		} else if(type == MISMATCHED_ARGUMENTS) {
			System.err.println("Function call does not have the correct number of parameters.");
		}
		
		if(t != null){
			System.err.println("Line: " + t.line_loc); 
		}
		
		System.exit(0);
	}
	
	public RuntimeError(Token name, String expected_type, Object value, int err_type){
		if(err_type == CANNOT_ASSIGN_TYPE){
			System.err.println("Cannot assign the given value to a variable of this type.");
			System.err.println("  Value Type: " + value.getClass().getName());
			System.err.println("  Variable Type: " + expected_type);
			System.err.println("  Variable Name: " + name.value);
		}
		
		System.exit(0);
	}
}
