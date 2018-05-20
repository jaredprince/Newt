package lexer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import parser.Token;

public class Lexer {

	static String[] structures = { "do", "class", "if", "else", "while", "for", "func", "switch", "case", "default", "try",
			"catch", "class" // structures
	};

	static String[] statements = { "goto", "return", "break", "continue", "redo", "restart", "exit"};
	
	static String[] specials = {"any", "inf", "neginf"};

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

	int char_loc = 0; /* Location of the character on the line */
	int line_loc = 1; /* Location of the line in the file */

	String line = "";

	/**
	 * Lexer constructor.
	 * 
	 * @param source The file to be lexed.
	 * @throws FileNotFoundException if the given source file does not exist.
	 */
	public Lexer(File source) throws FileNotFoundException {
		in = new Scanner(source);
		lex();
	}

	/**
	 * Lexer constructor.
	 * @param source The String to be lexed.
	 */
	public Lexer(String source) {
		in = new Scanner(source);
		lex();
	}

	/**
	 * Lexes the file or String associated with this Lexer into tokens.
	 */
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
				
				//lex a delineator
				else if(contains(delineators, c)){
					t = new Token(char_loc, line_loc, c + "", Token.DELINEATOR);
					line = line.substring(1);
					char_loc++;
				}
				
				//lex an identifier
				else if(Character.isLetter(c) || c == '_'){				
					t = identifierToken();
					
					if(contains(structures, t.value)){
						t.type = Token.STRUCTURE;
					} else if(contains(statements, t.value)) {
						t.type = Token.STATEMENT;
					} else if(contains(types, t.value)) {
						t.type = Token.DATA_TYPE;
					} else if(contains(specials, t.value)) {
						t.type = Token.LITERAL;
						t.subtype = Token.SPECIAL_VALUE;
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
				
				//Finally, lex an operator
				else {
					t = operatorToken();
				}
				
				//remove comments for now
				if(t.type != Token.COMMENT)
					tokens.add(t);
			}
			
			line_loc++;
		}
		
		tokens.add(new Token("EOF", Token.EOF));
	}
	
	/**
	 * Lexes a block comment token.
	 * 
	 * ex. /* This is a comment. *\/
	 * 
	 * @return The token.
	 */
	public Token blockCommentToken(){
		//start with the first char
		String val = "" + line.charAt(0) + line.charAt(1);
		line = line.length() > 2 ? line.substring(2) : "";
		
		//continue until the comment block closes
		while( val.charAt(val.length() - 1) != '/' || val.charAt(val.length() - 2) != '*' ){
			
			//ends the line
			if(line.length() == 0){
				
				if(in.hasNextLine()){
					line = in.nextLine();
					line_loc++;
					char_loc = 0;
				}
				
			} else {
				val += line.charAt(0);
				line = line.length() > 1 ? line.substring(1) : "";
				
				char_loc++;
			}
		}
		
		Token t = new Token(char_loc, line_loc, val, Token.COMMENT);
		
		if(val.charAt(val.length() - 1) != '/' || val.charAt(val.length() - 2) != '*'){
			t.type = Token.ERROR;
		}
		
		return t;
	}
	
	/**
	 * Lexes an operator token.
	 * 
	 * ex. +, -, >, ->, ?
	 * 
	 * @return The token.
	 */
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
			t.subtype = Token.SPECIAL_OP;
		}
		
		return t;
	}
	
	/**
	 * Lexes an identifier token.
	 * 
	 * ex. var, _var, var0
	 * 
	 * @return The token.
	 */
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
	
	/**
	 * Lexes an integer or double token.
	 * 
	 * ex. 123, 123.5. .004
	 * 
	 * @return The token.
	 */
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
	
	/**
	 * Lexes a character token.
	 * 
	 * ex. 'a'
	 * 
	 * @return The character token.
	 */
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
	 * 
	 * ex. "cat", "\"dog\""
	 * 
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

	/**
	 * Checks if a String is present in a String array.
	 * @param arr The array of Strings.
	 * @param x The String for which to search.
	 * @return True if the String is present, false otherwise.
	 */
	public static boolean contains(String[] arr, String x) {
		for (int i = 0; i < arr.length; i++) {
			if (x.equals(arr[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a character is present in a character array.
	 * @param arr The array of characters.
	 * @param x The character for which to search.
	 * @return True if the character is present, false otherwise.
	 */
	public static boolean contains(char[] arr, char x) {
		for (int i = 0; i < arr.length; i++) {
			if (x == arr[i]) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Checks if there is a next token.
	 * @return False if the token stack is empty, false otherwise.
	 */
	public boolean hasNextToken(){
		return !tokens.isEmpty();
	}
	
	/**
	 * CHecks if the next token is of the given type.
	 * @param i An integer representing the type.
	 * @return True if the token is of type i, false otherwise.
	 */
	public boolean nextTypeIs(int i){
		if(!hasNextToken()){
			return false;
		}
		
		return tokens.get(0).type == i ? true : tokens.get(0).subtype == i;
	}
	
	/**
	 * Checks if the next token is any of the given strings.
	 * @param strings The strings to be checked.
	 * @return True if the next token is in 'strings', false otherwise.
	 */
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
	
	/**
	 * Pushes the given token back onto the stack.
	 * In a LL(1) grammar, this is not necessary.
	 * @param t The token to add.
	 */
	public void returnToken(Token t){
		tokens.add(0, t);
	}
	
	/**
	 * Reads and destroys the next token.
	 * @return The next token.
	 */
	public Token consume(){
		Token s = tokens.get(0);
		tokens.remove(0);
		
		return s;
	}
	
	/**
	 * Reads the next token, without consuming.
	 * @return The next token or null.
	 */
	public Token read(){
		return hasNextToken() ? tokens.get(0) : null;
	}
}
