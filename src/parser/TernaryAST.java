package parser;

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
	public TypedObject visitNode(){
		
		/* If statements will return token statements if their bodies do (break, continue, etc.).
		 * Otherwise, they return a boolean denoting if the condition was true.
		 * This is used by the switch to check for default case. */
		if(token.value.equals("if")){
			if((Boolean)left.visitNode().object){
				TypedObject c = center.visitNode();
				return c != null ? c : new TypedObject("boolean", new Boolean(true));
			} else if(right.token.type != Token.BLANK){
				TypedObject r = right.visitNode();
				return r != null ? r : new TypedObject("boolean", new Boolean(false));
			} else {
				return new TypedObject("boolean", new Boolean(false));
			}
		}
		
		if(token.value.equals("declaration")){
			Parser.environment.define(left.token, center.token, (right.token.type == Token.BLANK ? null : right.visitNode()));
			return null;
		}
		
		if(token.type == Token.OPERATOR){
			TypedObject left = this.left.visitNode();
			
			if(token.value.equals("?")){
				
				//handle conditional operator using conditional operator
				return (Boolean)left.object ? center.visitNode() : right.visitNode();
			}
		}
		
		return null;
	}
}