package parser;

public class TypedObject {
	public String type;
	public Object object;
	boolean dynamic;

	public TypedObject(String t, Object o) {
		type = t;
		object = o;
	}
}
