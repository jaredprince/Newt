package parser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Environment {
	
	//TODO: make assignments happen in the nodes
	//in other words, in an assignment node, the visit function would retrieve the typedobject from the environment and chenge it internally

	private LinkedList<Map<String, TypedObject>> variables = new LinkedList<Map<String, TypedObject>>();
	public int depth;
	
	public Environment(){
		enterScope();
	}
	
	public void define(Token type, Token name) {
		if(variables.getFirst().containsKey(name.value)){
			throw new RuntimeError(name, RuntimeError.VARIABLE_ALREADY_DEFINED);
		} else {
			TypedObject o = new TypedObject(type.value, null);
			
			if(type.value.equals("var")){
				o.dynamic = true;
			}
			
			variables.getFirst().put(name.value, o);
		}
	}
	
	/**
	 * Defines a new variable
	 * @param type The type of the variable.
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	public void define(Token type, Token name, TypedObject value){
		
		//TODO: overloading methods
		
		//define the variable in the innermost scope
		if(variables.getFirst().containsKey(name.value)){
			throw new RuntimeError(name, RuntimeError.VARIABLE_ALREADY_DEFINED);
		} else {
			TypedObject o = new TypedObject(type.value, null);
			
			if(type.value.equals("var")){
				o.dynamic = true;
			}
			
			variables.getFirst().put(name.value, o);
			assign(name, value);
		}
	}
	
	/**
	 * Retrieves a variable's value from the HashMap.
	 * @param name The token of the variable name.
	 * @return The value of the variable.
	 */
	public TypedObject get(Token name){
		return findVariable(name);
	}
	
	/**
	 * Changes the value of a variable.
	 * @param name The name of the variable.
	 * @param value The value to assign to the variable.
	 */
	public void assign(Token name, TypedObject value){
		TypedObject var = findVariable(name);
		
		//dynamic variables can be any type
		if(var.dynamic){
			var.object = value.object;
			var.type = value.type;
			return;
		}
		
		switch(var.type){
			
		case "int":
			if(value.type.equals("double")){
				value.object = new Integer(((Double) value.object).intValue());
			} else if (!(value.type.equals("int"))){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		case "double":
			if(value.type.equals("int")){
				value.object = new Double(((Integer) value.object).intValue());
			} else if (!(value.type.equals("double"))){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
			break;
			
		default:
			if(!value.type.equals(var.type)){
				throw new RuntimeError(name, var.type, value, RuntimeError.CANNOT_ASSIGN_TYPE);
			}
		}
		
		var.object = value.object;
	}
	
	//TODO: Decide what happens when the type of a variable is changed. Right now, nothing happens to the object already in the variable.
	//I probably want to cast the variable to the new type (if it can be cast) or else set it to a default value for that type
	/**
	 * Changes the type of a variable.
	 * @param name The name of the variable.
	 * @param type The new type to assign.
	 */
	public void changeType(Token name, Token type){
		TypedObject var = findVariable(name);
		var.type = type.value;
	}
	
	public TypedObject findVariable(Token name){
		
		//for each scope starting with the innermost
		for(int i = 0; i < variables.size(); i++){
			//get the variables
			Map<String, TypedObject> scope = variables.get(i);
			
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
	public Map<String, TypedObject> getScopeFromInner(int depth){
		return variables.get(depth);
	}
	
	/**
	 * Finds the map representing the scope which is a given depth from the outermost.
	 * A depth of 0 returns the outermost (global) scope.
	 * 
	 * @param depth The depth of the scope from the outermost.
	 * @return The map representing the scope.
	 */
	public Map<String, TypedObject> getScopeFromOuter(int depth){
		return variables.get(variables.size() - 1 - depth);
	}
	
	public void appendScope(Map<String, TypedObject> scope) {
		variables.addFirst(scope);
	}
}
