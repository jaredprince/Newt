package parser;

public class DeclarationNode extends ASTNode {

	ASTNode type;
	ASTNode name;
	
	/* optional initialization */
	AssignmentNode assignment;
	
	public DeclarationNode(Token t){
		token = t;
	}
	
	public DeclarationNode(ASTNode l, ASTNode c, Token t){
		type = l;
		name = c;
		token = t;
	}
	
	public DeclarationNode(ASTNode l, ASTNode c, AssignmentNode r, Token t){
		type = l;
		name = c;
		assignment = r;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + type.toString(depth + 1) + "\n" + name.toString(depth + 1) + (assignment == null ? "" : "\n" + assignment.toString(depth + 1));
	}
	
	@Override
	public TypedObject visitNode(){
		
		Parser.environment.define(type.token, name.token, (assignment == null ? null : assignment.visitNode()));
		
		return null;
	}
	
}
