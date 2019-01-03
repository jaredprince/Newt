package interpreter;

public class NewtObject {
	/**
	 * The objects type, either primitive (int, double, etc.) or a class.
	 */
	public String type;
	
	/**
	 * The object itself.
	 */
	public Object object;
	
	/**
	 * True if the object's type can be dynamically changed.
	 * An object declared to be type 'var' will be dynamic.
	 * 
	 * This allows an object of type 'a' to be assigned to a variable of type 'b'.
	 */
	boolean dynamic;

	public NewtObject(String t, Object o) {
		type = t;
		object = o;
	}
}
