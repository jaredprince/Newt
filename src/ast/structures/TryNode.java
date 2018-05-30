package ast.structures;

import java.util.ArrayList;

import ast.ASTNode;
import parser.Token;
import parser.TypedObject;

public class TryNode extends ASTNode {

	
	StructureBodyNode tryBody;
	ArrayList<CatchNode> catches;
	StructureBodyNode finallyBody;
	
	public TryNode(Token t) {
		token = t;
	}
	
	public TypedObject visitNode() {
		
		
		
		return null;
	}
}