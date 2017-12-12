
public class Token {
	String value;
	int type;
	
	static final int LITERAL = 0;
	static final int TYPE = 1;
	static final int STATEMENT = 2;
	static final int DELINEATOR = 3;
	static final int COMMENT = 4;
	static final int OPERATOR = 5;
	static final int STRUCTURE = 6;
	static final int IDENTIFIER = 7;
	static final int STRING = 8;
	
	int char_loc;
	int line_loc;
	
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
