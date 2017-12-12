public class BinaryAST extends ASTNode {
	ASTNode left;
	ASTNode right;
	
	public BinaryAST(ASTNode l, Token t, ASTNode r){
		left = l;
		token = t;
		right = r;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + left.toString(depth + 1) + "\n" + right.toString(depth + 1);
	}
}
