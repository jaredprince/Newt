public class TernaryAST extends ASTNode {

	ASTNode left;
	ASTNode center;
	ASTNode right;
	
	public TernaryAST(Token t){
		token = t;
	}
	
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
	
	@Override
	public Object visitNode(){
		
		//TODO: handle if statements
		if(token.value.equals("if")){
			if((Boolean)left.visitNode()){
				center.visitNode();
			} else if(right.token.type != Token.BLANK){
				right.visitNode();
			}
		}
		
		if(token.value.equals("declaration")){
			Parser.environment.define(left.token, center.token, (right.token.type == Token.BLANK ? null : right.visitNode()));
			return null;
		}
		
		if(token.type == Token.OPERATOR){
			Object left = this.left.visitNode();
			
			if(token.value.equals("?")){
				
				//handle conditional operator using conditional operator
				return (Boolean)left ? center.visitNode() : right.visitNode();
			}
		}
		
		return null;
	}
}