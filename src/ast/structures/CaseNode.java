package ast.structures;

import ast.ASTNode;
import ast.operations.OperationNode;
import parser.Token;
import parser.TypedObject;

public class CaseNode extends ASTNode {
	
	private OperationNode value;
	private StructureBodyNode body;
	
	public CaseNode(Token t) {
		token = t;
	}
	
	public CaseNode(StructureBodyNode body) {
		this.setBody(body);
	}
	
	public static OperationNode createOperation(Token switchToken) {
		return null;
	}
	
	public TypedObject visitNode() {
		
		boolean valid = (Boolean) getValue().visitNode().object;
		
		if(valid) {
			TypedObject result = getBody().visitNode();
			return result == null ? new TypedObject("boolean", new Boolean(true)) : result;
		}
		
		return new TypedObject("boolean", new Boolean(false));
	}

	public OperationNode getValue() {
		return value;
	}

	public void setValue(OperationNode value) {
		this.value = value;
	}

	public StructureBodyNode getBody() {
		return body;
	}

	public void setBody(StructureBodyNode body) {
		this.body = body;
	}

}
