package parser;

public class CaseNode extends ASTNode {
	
	OperationNode value;
	StructureBodyNode body;
	
	public CaseNode(Token t) {
		token = t;
	}
	
	public CaseNode(StructureBodyNode body) {
		this.body = body;
	}
	
	public static OperationNode createOperation(Token switchToken) {
		return null;
	}
	
	public TypedObject visitNode() {
		
		boolean valid = (Boolean) value.visitNode().object;
		
		if(valid) {
			TypedObject result = body.visitNode();
			return result == null ? new TypedObject("boolean", new Boolean(true)) : result;
		}
		
		return new TypedObject("boolean", new Boolean(false));
	}

}
