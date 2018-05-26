package ast;

import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class ASTNode {
	
	public int type;
	public Token token;

	public ASTNode(){
		
	}
	
	public ASTNode(Token t){
		token = t;
	}
	
	public void setToken(Token t){
		this.token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value;
	}
	
	public TypedObject visitNode(){
		if(token.type == Token.BLANK){
			return null;
		}
		
		if(token.type == Token.STATEMENT){
			if(token.value.equals("break") || token.value.equals("continue")){
				//tokens will be passed up the tree until they are used
				return new TypedObject("token", token);
			}
		}
		
		if(token.type != Token.IDENTIFIER && token.type != Token.LITERAL && token.type != Token.STATEMENT && token.type != Token.DATA_TYPE){
			System.out.println("Expected: identifier, literal, statement, or type");
			System.out.println("  Recieved: " + Token.names[token.type]);
			System.out.println("  Value:    " + token.value);
		}
		
		//types get passed up to the declaration node
		if(token.type == Token.DATA_TYPE){
			return new TypedObject("token", token);
		}
		
		if(token.type == Token.IDENTIFIER){
			return Parser.environment.get(token);
		}
		
		String val = token.value;
		int stype = token.subtype;
		
		if(stype == Token.STRING){
			return new TypedObject("string", val.substring(1, val.length() - 1));
		} else if(stype == Token.BOOLEAN) {
			return new TypedObject("boolean", new Boolean(val));
		} else if(stype == Token.CHARACTER) {
			return new TypedObject("char", new Character(val.charAt(1)));
		} else if(stype == Token.DOUBLE){
			return new TypedObject("double", new Double(val));
		} else if(stype == Token.INTEGER){
			return new TypedObject("int", new Integer(val));
		} else if (stype == Token.SPECIAL_VALUE){
			return new TypedObject("token", token);
		}
		
		return null;
	}
}