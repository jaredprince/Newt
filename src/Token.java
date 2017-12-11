
public class Token {
	String value;
	String type;
	int location;
	
	public Token(int l){
		location = l;
	}
	
	public Token(int l, String v, String t){
		location = l;
		value = v;
		type = t;
	}
	
	public String toString(){
		return type + " : " + value + " : " + location;
	}
}
