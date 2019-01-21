package newt_metatypes;

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
	public boolean dynamic;
	public boolean initialized;

	public NewtObject(String t, Object o) {
		type = t;
		object = o;
		dynamic = false;
		initialized = true;
	}
	
	public NewtObject(String t, Object o, boolean d, boolean i) {
		type = t;
		object = o;
		dynamic = d;
		initialized = i;
	}
}
