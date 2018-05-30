package ast.structures;

import java.util.ArrayList;

import ast.ASTNode;
import ast.statement.DeclarationNode;
import parser.Token;
import parser.TypedObject;

public class CatchNode extends ASTNode {

	ArrayList<DeclarationNode> exceptions;
	StructureBodyNode body;
	
	public CatchNode(Token t) {
		token = t;
		exceptions = new ArrayList<DeclarationNode>();
	}
	
	public void addException(DeclarationNode e) {
		exceptions.add(e);
	}
	
	public TypedObject visitNode() {
		
		return null;
	}
	
}
