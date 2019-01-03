package parser;

import java.util.HashMap;

public class Scope {

	/**
	 * A map containing the value (TypedObject) for each variable in the scope.
	 * The key is the variable's name.
	 */
	public HashMap<String, TypedObject> values;
	
	public Scope() {
		values = new HashMap<String, TypedObject>();
	}
	
	/**
	 * Checks if the given variable exists in the scope.
	 * @param name the name of the variable
	 * @return true if the variable exists, false otherwise
	 */
	public boolean containsVariable(String name) {
		return values.containsKey(name);
	}
	
	/**
	 * Retrieves the value of a variable.
	 * @param name the name of the variable
	 * @return the value of the variable
	 */
	public TypedObject getValue(String name) {
		return values.get(name);
	}
	
	/**
	 * Adds a new variable to the scope or updates the 
	 * current value.
	 * 
	 * @param name the variable name
	 * @param object the variable value
	 */
	public void assignVariable(String name, TypedObject object) {
		values.put(name, object);
	}
}
