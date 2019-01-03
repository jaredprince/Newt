package interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * The Environment class stores the variables of a program in a specific scope.
 * Environments are linked together to achieve different nested scopes.
 * 
 * @author Jared
 */
public class Environment {

	/**
	 * The environment immediately enclosing this one. The environment representing
	 * the global scope has no enclosing environment.
	 */
	final Environment enclosing;

	/**
	 * The values of the variables, contained in a Map with a name key.
	 */
	private final Map<String, Object> values = new HashMap<>();

	/**
	 * The constructor used by the interpreter to create the global environment.
	 */
	public Environment() {
		enclosing = null;
		define(new Token(TokenType.IDENTIFIER, "$exit_flag", 0, 0, 0), 0);
	}

	/**
	 * Used by internal scopes (such as a block statement) to create an environment
	 * nested within the enclosing scope.
	 * 
	 * @param enclosing
	 *            the enclosing Environment
	 */
	public Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	/**
	 * Adds a new variable to this environment.
	 * 
	 * @param name
	 *            The name of the variable to declare
	 * @param value
	 *            the value to assign to the new variable
	 * 
	 *            TODO: Currently, null values are assigned to the object. Thus,
	 *            declaring a variable without initializing it and then printing the
	 *            variable will produce "null". Instead, trying to print a variable
	 *            which was not initialized should produce an error. I need to find
	 *            a way to distinguish between declarations (int i;) and
	 *            declaration/assignment to null (int i = null;).
	 *            
	 *            Additionally, I need to add a type parameter for type checking.
	 */
	public void define(Token name, Object value) {

		/* throws an error if the name is used */
		if (values.containsKey(name.lexeme)) {
			throw new RuntimeError(name, "Variable '" + name.lexeme + "' already defined.");
		}

		values.put(name.lexeme, value);
	}
	
	public void define(String name, Object value) {

		/* throws an error if the name is used */
		if (values.containsKey(name)) {
			throw new RuntimeError(null, "Variable '" + name + "' already defined.");
		}

		values.put(name, value);
	}
	
	public void undefine(Token name) {
		values.remove(name.lexeme);
	}

	/**
	 * Assigns a value to a pre-defined variable.
	 * @param name the name of the variable
	 * @param value the value to be assigned
	 */
	public void assign(Token name, Object value) {

		/* update the value if the variable was found */
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}

		/* try to assign the variable in the enclosing environment (recursively)*/
		if (hasEnclosing()) {
			enclosing.assign(name, value);
			return;
		}

		/* the variable was not declared */
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
	
	public void assign(String name, Object value) {

		/* update the value if the variable was found */
		if (values.containsKey(name)) {
			values.put(name, value);
			return;
		}

		/* try to assign the variable in the enclosing environment (recursively)*/
		if (hasEnclosing()) {
			enclosing.assign(name, value);
			return;
		}

		/* the variable was not declared */
		throw new RuntimeError(null, "Undefined variable '" + name + "'.");
	}

	/**
	 * Retrieves the value of a variable.
	 * TODO: check if the variable was defined but not initialized (error)
	 * 
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public Object get(Token name) {

		/* return the value if the variable was found */
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		/* search for the variable in the enclosing environment (recursively) */
		if (hasEnclosing()) {
			return enclosing.get(name);
		}

		/* the variable was not declared */
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
	
//	public Object getGlobal(Token name) {
//
//		if (hasEnclosing()) {
//			return enclosing.get(name);
//		}
//		
//		/* return the value if the variable was found */
//		if (values.containsKey(name.lexeme)) {
//			return values.get(name.lexeme);
//		}
//
//		/* the variable was not declared */
//		throw new RuntimeError(name, "Cannot retrieve undefined variable '" + name.lexeme + "'.");
//	}
//	
//	public void assignGlobal(Token name, Object value) {
//
//		if (hasEnclosing()) {
//			enclosing.assign(name, value);
//			return;
//		}
//		
//		/* update the value if the variable was found */
//		if (values.containsKey(name.lexeme)) {
//			values.put(name.lexeme, value);
//			return;
//		}
//
//		/* the variable was not declared */
//		throw new RuntimeError(name, "Cannot assign undefined variable '" + name.lexeme + "'.");
//	}

	/**
	 * Checks if the environment has an enclosing environment.
	 * @return true if there is an enclosing environment, false otherwise
	 */
	public boolean hasEnclosing() {
		return enclosing != null;
	}
}
