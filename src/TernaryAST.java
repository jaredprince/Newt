public class TernaryAST extends ASTNode {

	ASTNode left;
	ASTNode center;
	ASTNode right;
	
	public TernaryAST(ASTNode l, ASTNode c, ASTNode r, Token t){
		left = l;
		center = c;
		right = r;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + left.toString(depth + 1) + "\n" + center.toString(depth + 1) + "\n" + right.toString(depth + 1);
	}
}