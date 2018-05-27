package parser;
public class RuntimeError extends RuntimeException {

	/**
	 * Not sure what this does, but it removes the warning.
	 */
	private static final long serialVersionUID = 6299130057608538903L;
	
	static final int UNDEFINED_VARIABLE = 0;
	static final int VARIABLE_ALREADY_DEFINED = 1;
	static final int CANNOT_CAST = 2;
	static final int MISMATCHED_ARGUMENT_NUMBER = 3;
	static final int MISMATCHED_ARGUMENT_TYPE = 4;
	static final int CANNOT_ASSIGN_TYPE = 5;
	static final int NUMERIC_INPUT_EXPECTED = 6;
	static final int BOOLEAN_INPUT_EXPECTED = 7;
	
	static final int UNEXPECTED_CLASS_FIELD = 8;
	static final int UNEXPECTED_CONSTRUCT = 9;
	public static final int MISPLACED_MEMBERSHIP = 10;
	
	public RuntimeError(Token t, int type){
		if(type == UNDEFINED_VARIABLE){
			System.err.println("Undefined variable: " + t.value);
		} else if (type == VARIABLE_ALREADY_DEFINED){
			System.err.println("Variable already defined: " + t.value);
		} else if(type == CANNOT_CAST) {
			System.err.println("Cannot cast to type: " + t.value);
		} else if(type == NUMERIC_INPUT_EXPECTED){
			System.err.println("Numeric input expected after: " + t.value);
		} else if(type == BOOLEAN_INPUT_EXPECTED){
			System.err.println("Boolean input expected after: " + t.value);
		} else if(type == UNEXPECTED_CLASS_FIELD) {
			System.err.println("The class field \"" + t.value + "\" is not a function or variable.");
		} else if(type == UNEXPECTED_CONSTRUCT) {
			System.err.println("A construct can only be defined as a class field.");
		} else if(type == MISPLACED_MEMBERSHIP) {
			System.err.println("The membership operator ('.') can only be used on a class or and instance of a class.");
		}
		
		if(t != null){
			System.err.println("  Line: " + t.line_loc);
			System.err.println("  Character: " + t.char_loc);
		}
		
		System.exit(0);
	}
	
	public RuntimeError(int arity, int args, int type) {
		if(type == MISMATCHED_ARGUMENT_NUMBER) {
			System.err.println("Function call does not have the correct number of arguments.");
			System.err.println("Expected arguments: " + arity);
			System.err.println("Arguments given: " + args);
		}
		
		System.exit(0);
	}
	
	public RuntimeError(int argumentNum, String paramType, String argType, int type) {
		if(type == MISMATCHED_ARGUMENT_TYPE) {
			System.err.println("Mismatched type in function call.");
			System.err.println("Argument number: " + argumentNum);
			System.err.println("Expected type: " + paramType);
			System.err.println("Recieved type: " + argType);
		}
		
		System.exit(0);
	}
	
	public RuntimeError(Token name, String expected_type, TypedObject value, int err_type){
		if(err_type == CANNOT_ASSIGN_TYPE){
			System.err.println("Cannot assign the given value to a variable of this type.");
			System.err.println("  Value Type: " + value.type);
			System.err.println("  Variable Type: " + expected_type);
			System.err.println("  Variable Name: " + name.value);
		}
		
		System.exit(0);
	}
}
