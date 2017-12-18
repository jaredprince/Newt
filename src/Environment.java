import java.util.HashMap;
import java.util.Map;

public class Environment {

	private final Map<String, Object> values = new HashMap<>();
	
	/**
	 * Defines a new variable
	 * @param type The type of the variable.
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	public void define(Token type, Token name, Object value){
		//TODO: static typing
		
		if(values.containsKey(name)){
			throw new RuntimeError(name, RuntimeError.VARIABLE_ALREADY_DEFINED);
		} else {
			values.put(name.value, value);
		}
	}
	
	/**
	 * Retrieves a variable's value from the HashMap.
	 * @param name The token of the variable name.
	 * @return The value of the variable.
	 */
	public Object get(Token name){
		
		if(values.containsKey(name.value)){
			return values.get(name.value);
		} else {
			throw new RuntimeError(name, RuntimeError.UNDEFINED_VARIABLE);
		}
	}
	
	public void assign(Token name, Object value){
		
		if(values.containsKey(name.value)){
			values.put(name.value, value);
		} else {
			throw new RuntimeError(name, RuntimeError.UNDEFINED_VARIABLE);
		}
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
