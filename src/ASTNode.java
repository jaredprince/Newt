

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
}