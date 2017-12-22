import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Environment {

	private LinkedList<Map<String, TypedVariable>> variables = new LinkedList<Map<String, TypedVariable>>();
	public int depth;
	
	public Environment(){
		enterScope();
	}
	
	/**
	 * Defines a new variable
	 * @param type The type of the variable.
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	public void define(Token type, Token name, Object value){
		
		//TODO: overloading methods
		
		//define the variable in the innermost scope
		if(variables.getFirst().containsKey(name.value)){
			throw new RuntimeError(name, RuntimeError.VARIABLE_ALREADY_DEFINED);
		} else {
			variables.getFirst().put(name.value, new TypedVariable(type.value, null));
			assign(name, value);
		}
	}
	
	/**
	 * Retrieves a variable's value from the HashMap.
	 * @param name The token of the variable name.
	 * @return The value of the variable.
	 */
	public Object get(Token name){
		return findVariable(name).object;
	}
	
	/**
	 * Changes the value of a variable.
	 * @param name The name of the variable.
	 * @param value The value to assign to the variable.
	 */
	public void assign(Token name, Object value){
		TypedVariable var = findVariable(name);
		
		//TODO: enforce type
		
		switch(var.type){
		
		case "int":
			if(value instanceof Double){
				value = new Integer(((Double) value).intValue());
			} else if (!(value instanceof Integer)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "double":
			if(value instanceof Integer){
				value = new Double(((Integer) value).intValue());
			} else if (!(value instanceof Double)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "string":
			if(!(value instanceof String)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "boolean":
			if(!(value instanceof Boolean)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "char":
			if(!(value instanceof Character)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "func":
			if(!(value instanceof Function)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
		}
		
		var.object = value;
	}
	
	//TODO: Decide what happens when the type of a variable is changed. Right now, nothing happens to the object already in the variable.
	/**
	 * Changes the type of a variable.
	 * @param name The name of the variable.
	 * @param type The new type to assign.
	 */
	public void changeType(Token name, Token type){
		TypedVariable var = findVariable(name);
		var.type = type.value;
	}
	
	public TypedVariable findVariable(Token name){
		
		//for each scope starting with the innermost
		for(int i = 0; i < variables.size(); i++){
			//get the variables
			Map<String, TypedVariable> scope = variables.get(i);
			
			//check if the map contains the key
			if(scope.containsKey(name.value)){
				return scope.get(name.value);
			}
		}
		
		//if no variable was found
		throw new RuntimeError(name, RuntimeError.UNDEFINED_VARIABLE);
	}
	
	/**
	 * Discards the Map representing the innermost scope.
	 * Called when a scope is exited.
	 */
	public void enterScope(){
		variables.addFirst(new HashMap<>());
		depth++;
	}
	
	/**
	 * Creates a new Map representing variables for the new innermost scope.
	 * Called when a new scope is entered.
	 */
	public void exitScope(){
		variables.removeFirst();
		depth--;
	}
	
	/**
	 * Finds the map representing the scope which is a given depth from the innermost.
	 * A depth of 0 returns the innermost (local) scope.
	 * 
	 * @param depth The depth of the scope from the innermost.
	 * @return The map representing the scope.
	 */
	public Map<String, TypedVariable> getScopeFromInner(int depth){
		return variables.get(depth);
	}
	
	/**
	 * Finds the map representing the scope which is a given depth from the outermost.
	 * A depth of 0 returns the outermost (global) scope.
	 * 
	 * @param depth The depth of the scope from the outermost.
	 * @return The map representing the scope.
	 */
	public Map<String, TypedVariable> getScopeFromOuter(int depth){
		return variables.get(variables.size() - 1 - depth);
	}
}
