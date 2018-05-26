package parser;


public class Token {
	
	public static final String[] names = {
		"ERROR", "TYPE", "STATEMENT", "DELINEATOR", "COMMENT", "OPERATOR", "STRUCTURE", "IDENTIFIER", "GROUPING", "BLANK", "LITERAL",
		"BOOLEAN", "CHARACTER", "DOUBLE", "INTEGER", "OBJECT", "STRING", "MATHEMATICAL", "LOGICAL", "COMPARATIVE", "SPECIAL_OP",
		"ASSIGNMENT", "EOF", "SPECIAL_VALUE", "MEMBERSHIP"
	};
	
	//basic types
	public static final int ERROR = 0; //an unidentifiable sequence
	public static final int DATA_TYPE = 1; //a type keyword (int, char, string, etc.)
	public static final int STATEMENT = 2; //a statement keyword (break, return, continue, etc.)
	public static final int DELINEATOR = 3; //a separating punctuation mark ({, (, :, etc.)
	public static final int COMMENT = 4; //a comment (// or /**/)
	public static final int OPERATOR = 5; //an operator (logical, mathematical, assignment, comparative)
	public static final int STRUCTURE = 6; //a structure keyword (for, while, switch, case, etc.)
	public static final int IDENTIFIER = 7; //a variable name
	public static final int GROUPING = 8; //used by parser to denote a block node
	public static final int BLANK = 9; //used by the parser when a blank token is needed
	public static final int LITERAL = 10; //a literal value (number, string, char, etc.)
	
	//subtypes
	
	//literals
	public static final int BOOLEAN = 11; //true or false
	public static final int CHARACTER = 12; //'c'
	public static final int DOUBLE = 13; //123.123, .0123, 123., 0.123
	public static final int INTEGER = 14; //123
	public static final int OBJECT = 15; //currently unused
	public static final int STRING = 16; //"string"
	
	public static final int SPECIAL_VALUE = 23;
	
	//operators
	public static final int MATHEMATICAL = 17; // +, -, *, /, %, etc.
	public static final int LOGICAL = 18; // &&, ||, ~NOR, etc.
	public static final int COMPARATIVE = 19; // <, ==, !=, >=, etc.
	public static final int SPECIAL_OP = 20;
	public static final int ASSIGNMENT = 21; // =, *=, +=, etc.
	
	public static final int MEMBERSHIP = 24;
	
	public static final int EOF = 22;
	
	public String value;
	
	public int type;
	public int subtype;
	
	public int char_loc;
	public int line_loc;
	
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
