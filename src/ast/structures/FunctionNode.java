package ast.structures;

import java.util.ArrayList;

import ast.ASTNode;
import ast.statement.DeclarationNode;
import parser.Function;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class FunctionNode extends ASTNode {
	private ASTNode name;
	private ArrayList<DeclarationNode> params; //change to param list node
	private StructureBodyNode body;

	public FunctionNode(ASTNode name, ArrayList<DeclarationNode> l, Token t, StructureBodyNode r) {
		this.setName(name);
		setParams(l);
		token = t;
		setBody(r);
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + "Parameters" + "\n" + getBody().toString(depth + 1);
	}
	
	public void addParam(DeclarationNode p) {
		getParams().add(p);
	}

	public TypedObject visitNode() {
		
		Parser.environment.define(token, getName().token, new TypedObject("func", new Function(getParams(), getBody(), Parser.environment.getScopeFromInner(0))));
	
		return null;
	}

	public ArrayList<DeclarationNode> getParams() {
		return params;
	}

	public void setParams(ArrayList<DeclarationNode> params) {
		this.params = params;
	}

	public ASTNode getName() {
		return name;
	}

	public void setName(ASTNode name) {
		this.name = name;
	}

	public StructureBodyNode getBody() {
		return body;
	}

	public void setBody(StructureBodyNode body) {
		this.body = body;
	}
}
