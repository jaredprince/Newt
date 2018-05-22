package parser;

public class ForNode extends ASTNode {
	//TODO: make this assignment or declaration
	DeclarationNode declaration;
	ASTNode condition;
	AssignmentNode assignment;
	StructureBodyNode body;
	
	public ForNode(Token t){
		token = t;
	}
	
	public ForNode(DeclarationNode l, ASTNode lc, AssignmentNode rc, StructureBodyNode r, Token t){
		declaration = l;
		condition = lc;
		body = r;
		assignment = rc;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + declaration.toString(depth + 1) + "\n" + condition.toString(depth + 1) + "\n" + assignment.toString(depth + 1) + "\n" + body.toString(depth + 1);
	}
	
	public TypedObject visitNode(){
		
		Parser.environment.enterScope();
		
		//should declare a variable or do nothing
		declaration.visitNode();
		
		TypedObject returned_value;

		while((Boolean) condition.visitNode().object){
			//executes the body
			returned_value = body.visitNode();
			
			//break if the return for that iteration was a break
			if(returned_value != null && returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
				break;
			}
			
			//executes the incrementor
			assignment.visitNode();
		}
		
		Parser.environment.exitScope();
		
		return null;
	}
}
