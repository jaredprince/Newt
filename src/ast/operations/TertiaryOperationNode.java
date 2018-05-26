package ast.operations;

import ast.ASTNode;
import parser.Token;
import parser.TypedObject;

public class TertiaryOperationNode extends OperationNode {

	public ASTNode condition;
	
	public TertiaryOperationNode(ASTNode cond, ASTNode l, Token t, ASTNode r) {
		super(l, t, r);
		
		condition = cond;
	}

	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + condition.toString(depth + 1) + "\n" + left.toString(depth + 1) + "\n" + getRight().toString(depth + 1);
	}
	
	@Override
	public TypedObject visitNode(){
		return (Boolean)condition.visitNode().object ? left.visitNode() : getRight().visitNode();
	}
}
