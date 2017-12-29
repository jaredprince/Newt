
public class Token {
	
	static final String[] names = {
		"ERROR", "TYPE", "STATEMENT", "DELINEATOR", "COMMENT", "OPERATOR", "STRUCTURE", "IDENTIFIER", "GROUPING", "BLANK", "LITERAL",
		"BOOLEAN", "CHARACTER", "DOUBLE", "INTEGER", "OBJECT", "STRING", "MATHEMATICAL", "LOGICAL", "COMPARATIVE", "SPECIAL_OP",
		"ASSIGNMENT", "EOF", "SPECIAL_VALUE"
	};
	
	//basic types
	static final int ERROR = 0; //an unidentifiable sequence
	static final int TYPE = 1; //a type keyword (int, char, string, etc.)
	static final int STATEMENT = 2; //a statement keyword (break, return, continue, etc.)
	static final int DELINEATOR = 3; //a separating punctuation mark ({, (, :, etc.)
	static final int COMMENT = 4; //a comment (// or /**/)
	static final int OPERATOR = 5; //an operator (logical, mathematical, assignment, comparative)
	static final int STRUCTURE = 6; //a structure keyword (for, whil, switch, case, etc.)
	static final int IDENTIFIER = 7; //a variable name
	static final int GROUPING = 8; //used by parser to denote a block node
	static final int BLANK = 9; //used by the parser when a blank token is needed
	static final int LITERAL = 10; //a literal value (number, string, char, etc.)
	
	//subtypes
	
	//literals
	static final int BOOLEAN = 11; //true or false
	static final int CHARACTER = 12; //'c'
	static final int DOUBLE = 13; //123.123, .0123, 123., 0.123
	static final int INTEGER = 14; //123
	static final int OBJECT = 15; //currently unused
	static final int STRING = 16; //"string"
	
	static final int SPECIAL_VALUE = 23;
	
	//operators
	static final int MATHEMATICAL = 17; // +, -, *, /, %, etc.
	static final int LOGICAL = 18; // &&, ||, ~NOR, etc.
	static final int COMPARATIVE = 19; // <, ==, !=, >=, etc.
	static final int SPECIAL_OP = 20;
	static final int ASSIGNMENT = 21; // =, *=, +=, etc.
	
	static final int EOF = 22;
	
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
		String out = String.format("(%3d,%3d)  %10s : %s", char_loc, line_loc, names[type], value);
		return out;
	}
}
