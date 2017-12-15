
public class Token {
	
	//basic types
	static final int LITERAL = 0;
	static final int TYPE = 1;
	static final int STATEMENT = 2;
	static final int DELINEATOR = 3;
	static final int COMMENT = 4;
	static final int OPERATOR = 5;
	static final int STRUCTURE = 6;
	static final int IDENTIFIER = 7;
	static final int GROUPING = 8;
	static final int BLANK = 9;
	
	//subtypes
	
	//literals
	static final int STRING = 10;
	static final int BOOLEAN = 11;
	static final int CHARACTER = 12;
	static final int DOUBLE = 13;
	static final int INTEGER = 14;
	static final int OBJECT = 15;
	
	//operators
	static final int ASSIGNMENT = 16;
	static final int MATHMATICAL = 17;
	static final int LOGICAL = 18;	
	static final int COMPARATIVE = 19;
	
	String value;
	
	int type;
	int subtype;
	
	int char_loc;
	int line_loc;
	
	public Token(int t){
		type = t;
	}
	
	public Token(String v, int t){
		value = v;
		type = t;
	}
	
	public Token(int cl, int ll){
		char_loc = cl;
		line_loc = ll;
	}
	
	public Token(int cl, int ll, String v, int t){
		char_loc = cl;
		line_loc = ll;
		value = v;
		type = t;
	}
	
	public String toString(){
		return "type: " + type + "\t\tValue: " + value + "\t\tCharacter: " + char_loc + "\t\tLine: " + line_loc;
	}
}
