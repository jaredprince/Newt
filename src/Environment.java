import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Environment {

	private LinkedList<Map<String, Object>> variables = new LinkedList<Map<String, Object>>();
	
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
		//TODO: static typing
		
		//define the variable in the innermost scope
		if(variables.getFirst().containsKey(name.value)){
			throw new RuntimeError(name, RuntimeError.VARIABLE_ALREADY_DEFINED);
		} else {
			variables.getFirst().put(name.value, value);
		}
	}
	
	/**
	 * Retrieves a variable's value from the HashMap.
	 * @param name The token of the variable name.
	 * @return The value of the variable.
	 */
	public Object get(Token name){
		
		//for each scope starting with the innermost
		for(int i = 0; i < variables.size(); i++){
			//get the variables
			Map<String, Object> scope = variables.get(i);
			
			//check if the map contains the key
			if(scope.containsKey(name.value)){
				return scope.get(name.value);
			}
		}
		
		throw new RuntimeError(name, RuntimeError.UNDEFINED_VARIABLE);
	}
	
	/**
	 * Changes the value of a variable.
	 * @param name The name of the variable.
	 * @param value The value to assign to the variable.
	 */
	public void assign(Token name, Object value){
		boolean found = false;
		
		//for each scope starting with the innermost
		for(int i = 0; i < variables.size(); i++){
			//get the variables
			Map<String, Object> scope = variables.get(i);
			
			//check if the map contains the key
			if(scope.containsKey(name.value)){
				scope.put(name.value, value);
				found = true;
				break;
			}
		}
		
		//if no variable was found
		if(!found){
			throw new RuntimeError(name, RuntimeError.UNDEFINED_VARIABLE);
		}
	}
	
	/**
	 * Discards the Map representing the innermost scope.
	 * Called when a scope is exited.
	 */
	public void enterScope(){
		variables.addFirst(new HashMap<>());
	}
	
	/**
	 * Creates a new Map representing variables for the new innermost scope.
	 * Called when a new scope is entered.
	 */
	public void exitScope(){
		variables.removeFirst();
	}
	
	/**
	 * Finds the map representing the scope which is a given depth from the innermost.
	 * A depth of 0 returns the innermost (local) scope.
	 * 
	 * @param depth The depth of the scope from the innermost.
	 * @return The map representing the scope.
	 */
	public Map<String, Object> getScopeFromInner(int depth){
		return variables.get(depth);
	}
	
	/**
	 * Finds the map representing the scope which is a given depth from the outermost.
	 * A depth of 0 returns the outermost (global) scope.
	 * 
	 * @param depth The depth of the scope from the outermost.
	 * @return The map representing the scope.
	 */
	public Map<String, Object> getScopeFromOuter(int depth){
		return variables.get(variables.size() - 1 - depth);
	}
	
//	private class TypeCheck {
//		String type;
//		Object object;
//		
//		public TypeCheck(String t, Object o){
//			type = t;
//			object = o;
//		}
//	}
}
