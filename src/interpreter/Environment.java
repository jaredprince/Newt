package interpreter;

import java.util.HashMap;
import java.util.Map;

import newt_metatypes.NewtCallable;
import newt_metatypes.NewtInstance;
import newt_metatypes.NewtObject;

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
	private final Map<String, NewtObject> values = new HashMap<>();

	/**
	 * The constructor used by the interpreter to create the global environment.
	 */
	public Environment() {
		enclosing = null;
		define("$exit_flag", "int", 0);
	}

	/**
	 * Used by internal scopes (such as a block statement) to create an environment
	 * nested within the enclosing scope.
	 * 
	 * @param enclosing the enclosing Environment
	 */
	public Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	/**
	 * Adds a new variable to this environment.
	 * 
	 * @param name  The name of the variable to declare
	 * @param type  the declared type of the variable
	 * @param value the value to assign to the new variable
	 * 
	 *              TODO: Currently, null values are assigned to the object. Thus,
	 *              declaring a variable without initializing it and then printing
	 *              the variable will produce "null". Instead, trying to print a
	 *              variable which was not initialized should produce an error. I
	 *              need to find a way to distinguish between declarations (int i;)
	 *              and declaration/assignment to null (int i = null;).
	 * 
	 *              Additionally, I need to add a type parameter for type checking.
	 */
	public void define(Token name, String type, Object value) {

		/* throws an error if the name is used */
		if (values.containsKey(name.lexeme)) {
			throw new RuntimeError(name, "Variable '" + name.lexeme + "' already defined.");
		}
		
		if(value == null) {
			values.put(name.lexeme, new NewtObject(type, null, type.equals("var") ? true : false, false));
			return;
		}
		
		String valType = getType(value);
		
		if(type.equals("var")) {
			values.put(name.lexeme, new NewtObject(valType, value, true, true));
			return;
		}
		
		if(!type.equals(valType)) {
			throw new RuntimeError(name, "Incompatible types '" + type + "' and '" + valType + "'.");
		}

		values.put(name.lexeme, new NewtObject(valType, value));
	}

	public void define(String name, String type, Object value) {

		/* throws an error if the name is used */
		if (values.containsKey(name)) {
			throw new RuntimeError(null, "Variable '" + name + "' already defined.");
		}
		
		if(value == null) {
			values.put(name, new NewtObject(type, null, type.equals("var") ? true : false, false));
			return;
		}
		
		String valType = getType(value);
		
		if(type.equals("var")) {
			values.put(name, new NewtObject(valType, value, true, true));
			return;
		}
		
		if(!type.equals(valType)) {
			throw new RuntimeError(null, "Incompatible types '" + type + "' and '" + valType + "'.");
		}

		values.put(name, new NewtObject(valType, value));
	}
	
	public String getType(Object value) {
		
		if(value instanceof NewtInstance) {
			return ((NewtInstance) value).getClassName();
		}
		
		switch(value.getClass().getSimpleName()) {
		case "Integer": return "int";
		case "Double": return "double";
		case "Boolean": return "bool";
		case "Character": return "char";
		case "String": return "string";
		case "NewtClass": return "class";
		case "NewtFunction": return "function";
		}
		
		if(value instanceof NewtCallable) {
			return "function";
		}
		
		return null;
	}

	public void undefine(Token name) {
		values.remove(name.lexeme);
	}

	public void undefine(String name) {
		values.remove(name);
	}

	/**
	 * Assigns a value to a pre-defined variable.
	 * 
	 * @param name  the name of the variable
	 * @param value the value to be assigned
	 */
	public void assign(Token name, Object value) {

		/* update the value if the variable was found */
		if (values.containsKey(name.lexeme)) {
			String valType = getType(value);
			NewtObject var = values.get(name.lexeme);
			
			if(var.dynamic) {
				var.object = value;
				var.type = valType;
				var.initialized = true;
			} else if(valType.equals(var.type)) {
				var.object = value;
				var.initialized = true;
			} else
				throw new RuntimeError(name, "Incompatible types '" + var.type + "' and '" + valType + "'.");
			return;
		}

		/* try to assign the variable in the enclosing environment (recursively) */
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
			String valType = getType(value);
			NewtObject var = values.get(name);
			
			if(var.dynamic) {
				var.object = value;
				var.type = valType;
				var.initialized = true;
			} else if(valType.equals(var.type)) {
				var.object = value;
				var.initialized = true;
			} else
				throw new RuntimeError(null, "Incompatible types '" + var.type + "' and '" + valType + "'.");
			
			return;
		}

		/* try to assign the variable in the enclosing environment (recursively) */
		if (hasEnclosing()) {
			enclosing.assign(name, value);
			return;
		}

		/* the variable was not declared */
		throw new RuntimeError(null, "Undefined variable '" + name + "'.");
	}

	/**
	 * Retrieves the value of a variable. TODO: check if the variable was defined
	 * but not initialized (error)
	 * 
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public Object get(Token name) {

		/* return the value if the variable was found */
		if (values.containsKey(name.lexeme)) {
			NewtObject obj = values.get(name.lexeme);
			
			if(obj.initialized)
				return obj.object;
			else
				throw new RuntimeError(name, "Variable '" + name.lexeme + "' has not been initialized.");
		}

		/* search for the variable in the enclosing environment (recursively) */
		if (hasEnclosing()) {
			return enclosing.get(name);
		}

		/* the variable was not declared */
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
	
	/**
	 * Retrieves the value of a variable. TODO: check if the variable was defined
	 * but not initialized (error)
	 * 
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public Object get(String name) {

		/* return the value if the variable was found */
		if (values.containsKey(name)) {
			NewtObject obj = values.get(name);
			
			if(obj.initialized)
				return obj.object;
			else
				throw new RuntimeError(null, "Variable '" + name + "' has not been initialized.");
		}

		/* search for the variable in the enclosing environment (recursively) */
		if (hasEnclosing()) {
			return enclosing.get(name);
		}

		/* the variable was not declared */
		throw new RuntimeError(null, "Undefined variable '" + name + "'.");
	}

	public void assignAt(int distance, Token name, Object value) {
		ancestor(distance).assign(name, value);
	}

	public Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name).object;
	}

	public Environment ancestor(int distance) {
		Environment environment = this;
		for (int i = 0; i < distance; i++) {
			environment = environment.enclosing;
		}

		return environment;
	}

	/**
	 * Checks if the environment has an enclosing environment.
	 * 
	 * @return true if there is an enclosing environment, false otherwise
	 */
	public boolean hasEnclosing() {
		return enclosing != null;
	}
}
