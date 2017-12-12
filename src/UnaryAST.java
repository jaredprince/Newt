


public class UnaryAST extends ASTNode {

	ASTNode child;
	
	public UnaryAST(ASTNode c, Token t){
		child = c;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + child.toString(depth + 1);
	}
}
