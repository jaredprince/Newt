

public class ASTNode {
	
//	String type;
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
		if(token.type != Token.LITERAL){
			System.err.println("Expected literal, but no literal found.");
			System.exit(0);
		}
		
		String val = token.value;
		int stype = token.subtype;
		
		if(stype == Token.STRING){
			return val.substring(1, val.length() - 1);
		} else if(stype == Token.BOOLEAN) {
			return new Boolean(val);
		} else if(stype == Token.CHARACTER) {
			return new Character(val.charAt(0));
		} else if(stype == Token.DOUBLE){
			return new Double(val);
		} else if(stype == Token.INTEGER){
			return new Integer(val);
		}
		
		return null;
	}
}