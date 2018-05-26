package ast.structures;

import ast.ASTNode;
import ast.statement.AssignmentNode;
import ast.statement.DeclarationNode;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class ForNode extends ASTNode {
	//TODO: make this assignment or declaration
	private DeclarationNode declaration;
	private ASTNode condition;
	private AssignmentNode assignment;
	private StructureBodyNode body;
	
	public ForNode(Token t){
		token = t;
	}
	
	public ForNode(DeclarationNode l, ASTNode lc, AssignmentNode rc, StructureBodyNode r, Token t){
		setDeclaration(l);
		setCondition(lc);
		setBody(r);
		setAssignment(rc);
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + getDeclaration().toString(depth + 1) + "\n" + getCondition().toString(depth + 1) + "\n" + getAssignment().toString(depth + 1) + "\n" + getBody().toString(depth + 1);
	}
	
	public TypedObject visitNode(){
		
		Parser.environment.enterScope();
		
		//should declare a variable or do nothing
		getDeclaration().visitNode();
		
		TypedObject returned_value;

		while((Boolean) getCondition().visitNode().object){
			//executes the body
			returned_value = getBody().visitNode();
			
			//break if the return for that iteration was a break
			if(returned_value != null && returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
				break;
			}
			
			//executes the incrementor
			getAssignment().visitNode();
		}
		
		Parser.environment.exitScope();
		
		return null;
	}

	public DeclarationNode getDeclaration() {
		return declaration;
	}

	public void setDeclaration(DeclarationNode declaration) {
		this.declaration = declaration;
	}

	public ASTNode getCondition() {
		return condition;
	}

	public void setCondition(ASTNode condition) {
		this.condition = condition;
	}

	public AssignmentNode getAssignment() {
		return assignment;
	}

	public void setAssignment(AssignmentNode assignment) {
		this.assignment = assignment;
	}

	public StructureBodyNode getBody() {
		return body;
	}

	public void setBody(StructureBodyNode body) {
		this.body = body;
	}
}
