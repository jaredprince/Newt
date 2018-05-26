package ast.structures;

import ast.ASTNode;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class WhileNode extends ASTNode{
	
	private ASTNode condition;
	private StructureBodyNode body;

	public WhileNode(ASTNode l, Token t, StructureBodyNode r) {
		setCondition(l);
		token = t;
		setBody(r);
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + getCondition().toString(depth + 1) + "\n" + getBody().toString(depth + 1);
	}

	public TypedObject visitNode() {

		Parser.environment.enterScope();
		
		TypedObject returned_value = null;
		
		while((Boolean) getCondition().visitNode().object){
			returned_value = getBody().visitNode();
			
			//break if the return for that iteration was a break
			if(returned_value != null && returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
				break;
			}
		}
		
		Parser.environment.exitScope();

		return null;
	}

	public ASTNode getCondition() {
		return condition;
	}

	public void setCondition(ASTNode condition) {
		this.condition = condition;
	}

	public StructureBodyNode getBody() {
		return body;
	}

	public void setBody(StructureBodyNode body) {
		this.body = body;
	}

}
