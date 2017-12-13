import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {
	
	int char_loc = 0;
	int line_loc = 0;
	
	static String[] keywords = {
		"do", "if", "else", "while", "for", "func", "switch", "case", "default", //structures
		"void", //modifiers
		"goto", "return", "break" //actions	
	};
	
	static String[] types = {
		"int", "string", "char", "float", "boolean"
	};
	
	static String[] operators = {
		"+", "=", "-", "*", "/", "%", "++", "--", "^",	//arithmetic
		"==", "!=", "<", ">", "<=", ">=",  //comparison
		"!", "&&", "||", "~NAND", "~NOR", "->",  //logical
		"+=", "-=", "*=", "/=", "%=", "^=",  //compound
		".",  //member
		"?.", //safe navigator
		"<=>", //spaceship - three way comparison (compareTo)
		"?", //tertiary
		"|", //absolute value
	};
	
	static char[] delineators = {',', '{', '}', '[', ']', '(', ')', ';', ':'}; // <> ?

	ArrayList<Token> tokens = new ArrayList<Token>();
	Scanner in;
	
	public Lexer(File source) throws FileNotFoundException{
		in = new Scanner(source);
		lex();
	}

	public Lexer(String source){
		in = new Scanner(source);
		lex();
	}
	
	public void lex(){
		
		//go through each line
		while(in.hasNextLine()){
			String line = in.nextLine();
			
			//go through each character
			while(!line.equals("")){
				String value = "";
				Token t = new Token(char_loc, line_loc);
				
				//get the char from line and remove
				char c = line.charAt(0);
				line = line.substring(1);
				
				char_loc++;
				value += c;
				
				//handles whitespace
				if(Character.isWhitespace(c)){
					continue;
				}
				
				//handle strings
				else if(c == '"'){
					t.type = 8;
					value = stringToken(c, line);
					
					line = line.substring(value.length() - 1);
				}
				
				//handle keywords and identifiers
				else if(Character.isLetter(c)) {
					
					value = identifierToken(c, line);
					line = line.substring(value.length() - 1);
					
					if(contains(keywords, value)){
						t.type = 6;	
					} else if (contains(types, value)){
						t.type = 1;
					} else if (value.equals("true") || value.equals("false")){
						t.type = 0;
					} else {
						t.type = 7;
					}
				}
				
				else if(contains(delineators, c)) {
					t.type = 3;
				}
				
				else if (c == '/'){
					
					//handles //comments
					if(line.charAt(0) == '/'){
						t.type = 4;
						value += line;
						line = "";
						
						char_loc++;
					} 
					
					//handles /*comments*/
					else if (line.charAt(0) == '*'){
						value += line.charAt(0);
						line = line.substring(1);
						
						char_loc++;
						
						t.type = 4;
						//continue until the comment closes
						while(!value.substring(value.length() - 2).equals("*/")){
							value += line.charAt(0);
							line = line.substring(1);
							
							char_loc++;
						}
					}
					
					//handles division op
					else {
						t.type = 5;
					}
				}

				//handles number literals
				else if (Character.isDigit(c)){
					t.type = 0;
					boolean period = false;
					
					while(Character.isDigit(line.charAt(0)) || (!period && line.charAt(0) == '.')){
						if(line.charAt(0) == '.'){
							period = true;
						}
						
						value += line.charAt(0);
						line = line.substring(1);
						
						char_loc++;
					}
				}
				
				else if (c == '\''){
					value = c + line.substring(0, 2);
					line = line.substring(2);
					
					char_loc += 2;
					
					t.type = 0;
				}
				
				//handles operators (including ones like + and ++)
				else {
					
					value = operatorToken(c, line);
					line = line.substring(value.length() - 1);
					
					t.type = 5;
				}
				
				t.value = value;
				
				//weed out comments for now
				if(t.type != Token.COMMENT){
					//add the token
					tokens.add(t);
				}
			}
		}
	}
	
	public String operatorToken(char c, String line){
		boolean found = contains(operators, c + "");
		boolean lost = false;
		String value = "" + c;
		
		//expand the operator until it matches something (ie. || or &&)
		//this will crash if it is not an operator
		while(!found || !lost){
			value += line.charAt(0);
			line = line.substring(1);
			
			char_loc++;
			
			if(contains(operators, value)){
				found = true;
				lost = false;
			} else {
				lost = true;
			}
		}
		
		//give a char back to the line
		value = value.substring(0, value.length() - 1);
		char_loc--;
		
		return value;
	}
	
	public String stringToken(char c, String line){
		String value = "" + c + line.charAt(0);
		line = line.substring(1);
		
		char_loc++;
		
		//!!remember escape chars!!
		while(value.charAt(value.length() - 1) != '"'){
			value += line.charAt(0);
			line = line.substring(1);
			char_loc++;
		}
		
		return value;
	}
	
	public String identifierToken(char c, String line){
		String value = "" + c;
		while(line.length() > 0 && (Character.isLetter(line.charAt(0)) || Character.isDigit(line.charAt(0)) || line.charAt(0) == '_')){
			value += line.charAt(0);
			line = line.substring(1);
			
			char_loc++;
		}
		
		return value;
		
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
	
	public Token consume(){
		Token s = tokens.get(0);
		tokens.remove(0);
		
		return s;
	}
	
	public Token read(){
		return hasNextToken() ? tokens.get(0) : null;
	}
		
	public static boolean contains(char[] arr, char x){
		for(int i = 0; i < arr.length; i++){
			if(x == arr[i]){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean contains(String[] arr, String x){
		for(int i = 0; i < arr.length; i++){
			if(x.equals(arr[i])){
				return true;
			}
		}
		
		return false;
	}

}
