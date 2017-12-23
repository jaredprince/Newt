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
		
		if(token.value.equals("for")){
		
			Parser.environment.enterScope();
			
			//should declare a variable or do nothing
			left.visitNode();
			
			Object returned_value;
	
			while((Boolean) left_center.visitNode()){
				//executes the body
				((NaryAST)right).structureBody = true;
				returned_value = right.visitNode();
				
				//break if the return for that iteration was a break
				if(returned_value instanceof Token && ((Token) returned_value).value.equals("break")){
					break;
				}
				
				//executes the incrementor
				right_center.visitNode();
			}
			
			Parser.environment.exitScope();
		
		}
		
		return null;
	}
}