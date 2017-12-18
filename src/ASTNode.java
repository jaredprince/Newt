

public class ASTNode {
	
	int type;
	Token token;

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
	
	public Object visitNode(){
		
		if(token.type != Token.IDENTIFIER && token.type != Token.LITERAL && token.type != Token.STATEMENT && token.type != Token.TYPE){
			System.out.println("Expected: identifier, literal, statement, or type");
			System.out.println("  Recieved: " + Token.names[token.type]);
			System.out.println("  Value:    " + token.value);
		}
		
		//types get passed up to the declaration node
		if(token.type == Token.TYPE){
			return token;
		}
		
		if(token.type == Token.IDENTIFIER){
			return Parser.environment.get(token);
		}
		
		String val = token.value;
		int stype = token.subtype;
		
		if(stype == Token.STRING){
			return val.substring(1, val.length() - 1);
		} else if(stype == Token.BOOLEAN) {
			return new Boolean(val);
		} else if(stype == Token.CHARACTER) {
			return new Character(val.charAt(1));
		} else if(stype == Token.DOUBLE){
			return new Double(val);
		} else if(stype == Token.INTEGER){
			return new Integer(val);
		}
		
		return null;
	}
}