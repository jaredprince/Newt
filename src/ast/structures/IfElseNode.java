package ast.structures;

import ast.ASTNode;
import parser.Token;
import parser.TypedObject;

public class IfElseNode extends ASTNode {
	
	private ASTNode condition;
	private StructureBodyNode ifBody;
	private StructureBodyNode elseBody;
	
	public IfElseNode(Token t){
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + getCondition().toString(depth + 1) + "\n" + getIfBody().toString(depth + 1) + "\n" + getElseBody().toString(depth + 1);
	}
	
	@Override
	public TypedObject visitNode(){
		
		/* If statements will return token statements if their bodies do (break, continue, etc.).
		 * Otherwise, they return a boolean denoting if the condition was true.
		 * This is used by the switch to check for default case. */

		if((Boolean)getCondition().visitNode().object){
			TypedObject c = getIfBody().visitNode();
			return c != null ? c : new TypedObject("boolean", new Boolean(true));
		} else if(getElseBody() != null){
			TypedObject r = getElseBody() == null ? null : getElseBody().visitNode();
			return r != null ? r : new TypedObject("boolean", new Boolean(false));
		} else {
			return new TypedObject("boolean", new Boolean(false));
		}
	}

	public ASTNode getCondition() {
		return condition;
	}

	public void setCondition(ASTNode condition) {
		this.condition = condition;
	}

	public StructureBodyNode getIfBody() {
		return ifBody;
	}

	public void setIfBody(StructureBodyNode ifBody) {
		this.ifBody = ifBody;
	}

	public StructureBodyNode getElseBody() {
		return elseBody;
	}

	public void setElseBody(StructureBodyNode elseBody) {
		this.elseBody = elseBody;
	}
}
