import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {

	static String[] structures = { "do", "if", "else", "while", "for", "func", "switch", "case", "default", "try",
			"catch" // structures
	};

	static String[] statements = { "goto", "return", "break" };

	static String[] types = { "short", "long", "int", "string", "char", "double", "boolean", "var" };

	static String[] operators = { "++", "--", // arithmetic
			"==", "!=", "<", ">", "<=", ">=", "=", "+=", "-=", "*=", "/=", "%=", "^=", "!", "&&", "||", "~NAND", "~NOR",
			"->", "+", "-", "*", "/", "%", "^", "|",
			// compound
			".", // member
			"?.", // safe navigator
			"<=>", // spaceship - three way comparison (compareTo)
			"?", // tertiary
	};

	static String[] special_op = { ".", "?", "<=>" };

	static String[] math_op = { "+", "-", "*", "/", "%", "^", "|" };

	static String[] logical_op = { "!", "&&", "||", "~NAND", "~NOR", "->" };

	static String[] assignment_op = { "=", "+=", "-=", "*=", "/=", "%=", "^=", "++", "--" };

	static String[] comparative_op = { "==", "!=", "<", ">", "<=", ">=" };

	static char[] delineators = { ',', '{', '}', '[', ']', '(', ')', ';', ':' }; // <>
																					// ?

	ArrayList<Token> tokens = new ArrayList<Token>();
	Scanner in;

	int char_loc = 0;
	int line_loc = 0;

	String line = "";

	public Lexer(File source) throws FileNotFoundException {
		in = new Scanner(source);
		lex();
	}

	public Lexer(String source) {
		in = new Scanner(source);
		lex();
	}

	public void lex(){
		
		while(in.hasNextLine()){
			char_loc = 0;
			line = in.nextLine();
			
			while(line.length() > 0){
			
				Token t = null;
				
				char c = line.charAt(0);
				
				//throw whitespace
				if(Character.isWhitespace(c)){
					line = line.length() > 1 ? line.substring(1) : "";
					continue;
				}
				
				//lex a string
				else if(c == '"'){
					t = stringToken();
				}
				
				//lex a character
				else if(c == '\''){
					t = charToken();
				}
				
				//lex a number (double or int) if the char is a digit or a . followed by a digit
				else if(Character.isDigit(c) || (c == '.' && line.length() > 1 && Character.isDigit(line.charAt(1)))){
					t = numberToken();
				}
				
				else if(contains(delineators, c)){
					t = new Token(char_loc, line_loc, c + "", Token.DELINEATOR);
					line = line.substring(1);
					char_loc++;
				}
				
				else if(Character.isLetter(c) || c == '_'){				
					t = identifierToken();
					
					if(contains(structures, t.value)){
						t.type = Token.STRUCTURE;
					} else if(contains(statements, t.value)) {
						t.type = Token.STATEMENT;
					} else if(contains(types, t.value)) {
						t.type = Token.TYPE;
					} else if(t.value.equals("true") || t.value.equals("false")){
						t.type = Token.LITERAL;
						t.subtype = Token.BOOLEAN;
					}
				}
				
				//check for comments
				else if(c == '/' && (line.length() > 1 && (line.charAt(1) == '/' || line.charAt(1) == '*'))){
					if(line.charAt(1) == '/'){
						t = new Token(char_loc, line_loc, line, Token.COMMENT);
						line = "";
					} else {
						t = blockCommentToken();
					}
				}
				
				else {
					t = operatorToken();
				}
				
				tokens.add(t);
			}
			
			line_loc++;
		}
	}
	
	public Token blockCommentToken(){
		//start with the first char
		String val = "" + line.charAt(0) + line.charAt(1);
		line = line.length() > 2 ? line.substring(2) : "";
		
		//continue until the comment block closes
		while(val.charAt(val.length() - 1) != '/' || val.charAt(val.length() - 2) != '*'){
			if(line.length() == 0){
				if(in.hasNextLine()){
					line = in.nextLine();
					line_loc++;
				} else {
					break;
				}
			}
			
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			char_loc++;
		}
		
		Token t = new Token(char_loc, line_loc, val, Token.COMMENT);
		
		if(val.charAt(val.length() - 1) != '/' || val.charAt(val.length() - 2) != '*'){
			t.type = Token.ERROR;
		}
		
		return t;
	}
	
	public Token operatorToken(){
		//start with the first char
		String val = line.charAt(0) + "";
		line = line.substring(1);
		
		boolean found = contains(operators, val);
		boolean lost = false;
		
		//keep adding char until a match is found, then increase to find the largest match
		while(line.length() > 0 && (!found || !lost)){
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			if(contains(operators, val)){
				found = true;
			} else if(found) {
				lost = true;
			}
			
			char_loc++;
		}
		
		if(lost){
			//give a char back to line
			line = val.charAt(val.length() - 1) + line;
			val = val.substring(0, val.length() - 1);
			char_loc--;
		}
		
		Token t = new Token(char_loc, line_loc, val, Token.OPERATOR);
		
		//set the subtype
		if(contains(math_op, val)){
			t.subtype = Token.MATHEMATICAL;
		} else if(contains(assignment_op, val)){
			t.subtype = Token.ASSIGNMENT;
		} else if(contains(comparative_op, val)){
			t.subtype = Token.COMPARATIVE;
		} else if(contains(logical_op, val)){
			t.subtype = Token.LOGICAL;
		} else {
			t.subtype = Token.SPECIAL;
		}
		
		return t;
	}
	
	public Token identifierToken(){
		//start with the first char
		String val = line.charAt(0) + "";
		line = line.substring(1);
		
		//continue adding chars as long as there is a number or letter or "_"
		while(line.length() > 0 && ( Character.isLetter(line.charAt(0)) || line.charAt(0) == '_' || Character.isDigit(line.charAt(0)) )){
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			char_loc++;
		}
		
		return new Token(char_loc, line_loc, val, Token.IDENTIFIER);
	}
	
	public Token numberToken(){
		//start with the first char
		String val = line.charAt(0) + "";
		line = line.substring(1);
		
		//keep track of the decimal place
		boolean period = val.charAt(0) == '.' ? true : false;
		
		//add chars as long as there is a digit or a (single) period
		while(line.length() > 0 && ( Character.isDigit(line.charAt(0)) || (!period && line.charAt(0) == '.'))){
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			if(val.charAt(val.length() - 1) == '.'){
				period = true;
			}
			
			char_loc++;
		}
		
		Token t = new Token(char_loc, line_loc, val, Token.LITERAL);
		t.subtype = period ? Token.DOUBLE : Token.INTEGER;
		
		return t;
	}
	
	public Token charToken(){
		//start with the apostrophe
		String val = line.charAt(0) + "";
		line = line.substring(1);
		
		//add chars until there is a closing apostrophe or the line ends
		while(line.length() > 0 && !(val.length() > 1 && val.charAt(val.length() - 1) == '\'')){
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			char_loc++;
		}
		
		//return the char token or error token
		if(val.endsWith("'") && val.length() == 3){
			Token t = new Token(char_loc, line_loc, val, Token.LITERAL);
			t.subtype = Token.CHARACTER;
			return t;
		} else {
			return new Token(char_loc, line_loc, val, Token.ERROR);
		}
	}
	
	/**
	 * Lexes a string token.
	 * @return The token.
	 */
	public Token stringToken(){
		//TODO: handle escape chars (eg. "\"cat\"") == ""cat""
		
		//start with the quotation mark
		String val = line.charAt(0) + "";
		line = line.substring(1);
		
		//add chars until there is a closing quotation mark or the line ends
		while(line.length() > 0 && !(val.length() > 1 && val.charAt(val.length() - 1) == '"')){
			val += line.charAt(0);
			line = line.length() > 1 ? line.substring(1) : "";
			
			char_loc++;
		}
		
		//return a literal token or an error token
		if(val.endsWith("\"") && val.length() > 1){
			Token t = new Token(char_loc, line_loc, val, Token.LITERAL);
			t.subtype = Token.STRING;
			return t;
		} else {
			return new Token(char_loc, line_loc, val, Token.ERROR);
		}
	}

	public static boolean contains(String[] arr, String x) {
		for (int i = 0; i < arr.length; i++) {
			if (x.equals(arr[i])) {
				return true;
			}
		}

		return false;
	}

	public static boolean contains(char[] arr, char x) {
		for (int i = 0; i < arr.length; i++) {
			if (x == arr[i]) {
				return true;
			}
		}

		return false;
	}
	
	public boolean hasNextToken(){
		return !tokens.isEmpty();
	}
	
	public boolean nextTypeIs(int i){
		if(!hasNextToken()){
			return false;
		}
		
		return tokens.get(0).type == i;
	}
	
	public boolean nextValueIs(String... strings){
		if(!hasNextToken()){
			return false;
		}
		
		String val = tokens.get(0).value;
		
		for(String str : strings){
			if(str.equals(val)){
				return true;
			}
		}
		
		return false;
	}
	
	public void returnToken(Token t){
		tokens.add(0, t);
	}
	
	public Token consume(){
		Token s = tokens.get(0);
		tokens.remove(0);
		
		return s;
	}
	
	public Token read(){
		return hasNextToken() ? tokens.get(0) : null;
	}
}
