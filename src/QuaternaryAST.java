public class QuaternaryAST extends ASTNode {

	ASTNode left;
	ASTNode left_center;
	ASTNode right_center;
	ASTNode right;
	
	public QuaternaryAST(Token t){
		token = t;
	}
	
	public QuaternaryAST(ASTNode l, ASTNode lc, ASTNode rc, ASTNode r, Token t){
		left = l;
		left_center = lc;
		right = r;
		right_center = rc;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + left.toString(depth + 1) + "\n" + left_center.toString(depth + 1) + "\n" + right_center.toString(depth + 1) + "\n" + right.toString(depth + 1);
	}
	
	public Object visitNode(){
		
		//should declare a variable or do nothing
		left.visitNode();

		while((Boolean) left_center.visitNode()){
			//executes the body
			((NaryAST)right).structureBody = true;
			right.visitNode();
			
			//executes the incrementor
			right_center.visitNode();
		}
		
		return null;
	}
}