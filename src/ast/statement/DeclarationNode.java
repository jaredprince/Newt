package ast.statement;

import ast.ASTNode;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class DeclarationNode extends ASTNode {

	private ASTNode type;
	private ASTNode name;
	
	/* optional initialization */
	private AssignmentNode assignment;
	
	public DeclarationNode(Token t){
		token = t;
	}
	
	public DeclarationNode(ASTNode l, ASTNode c, Token t){
		setType(l);
		setName(c);
		token = t;
	}
	
	public DeclarationNode(ASTNode l, ASTNode c, AssignmentNode r, Token t){
		setType(l);
		setName(c);
		setAssignment(r);
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + getType().toString(depth + 1) + "\n" + getName().toString(depth + 1) + (getAssignment() == null ? "" : "\n" + getAssignment().toString(depth + 1));
	}
	
	@Override
	public TypedObject visitNode(){
		
		Parser.environment.define(getType().token, getName().token);
		
		if(getAssignment() != null) {
			getAssignment().visitNode();
		}
		
		return null;
	}

	public ASTNode getType() {
		return type;
	}

	public void setType(ASTNode type) {
		this.type = type;
	}

	public ASTNode getName() {
		return name;
	}

	public void setName(ASTNode name) {
		this.name = name;
	}

	public AssignmentNode getAssignment() {
		return assignment;
	}

	public void setAssignment(AssignmentNode assignment) {
		this.assignment = assignment;
	}
	
}
