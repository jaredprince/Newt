package parser;

public class DoWhileNode extends ASTNode {
	
	//TODO: Consider removing final semi-colon after while
	
	ASTNode condition;
	StructureBodyNode body;

	public DoWhileNode(ASTNode l, Token t, StructureBodyNode r) {
		condition = l;
		token = t;
		body = r;
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + condition.toString(depth + 1) + "\n" + body.toString(depth + 1);
	}

	public TypedObject visitNode() {
		
		Parser.environment.enterScope();
				
		TypedObject returned_value = body.visitNode();
		
		while(((Boolean) condition.visitNode().object)){
			
			//break if the return for that iteration was a break
			if(returned_value != null && returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
				break;
			}
			
			returned_value = body.visitNode();
		}
		
		Parser.environment.exitScope();

		return null;
	}

}
