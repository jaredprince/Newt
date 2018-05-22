package parser;

public class CaseNode extends ASTNode {
	
	OperationNode value;
	StructureBodyNode body;
	
	public CaseNode() {
		
	}
	
	public CaseNode(StructureBodyNode body) {
		this.body = body;
	}
	
//	public boolean createOperation(Token switchToken) {
//
//	}
	
	public TypedObject visitNode() {
		
		boolean valid = (Boolean) value.visitNode().object;
		
		if(valid) {
			TypedObject result = body.visitNode();
			return result == null ? new TypedObject("boolean", new Boolean(true)) : result;
		}
		
		return new TypedObject("boolean", new Boolean(false));
	}

}
