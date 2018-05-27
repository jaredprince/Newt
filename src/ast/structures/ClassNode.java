package ast.structures;

import java.util.ArrayList;

import ast.ASTNode;
import ast.statement.DeclarationNode;
import parser.ClassObject;
import parser.Function;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class ClassNode extends ASTNode {
	
	ASTNode name;
	ArrayList<FunctionNode> methods;
	ArrayList<DeclarationNode> data;
	private FunctionNode construct;
	
	public ClassNode(Token t) {
		token = t;
		methods = new ArrayList<FunctionNode>();
		data = new ArrayList<DeclarationNode>();
	}
	
	public void addFunction(FunctionNode n) {
		methods.add(n);
	}
	
	public void addData(DeclarationNode n) {
		data.add(n);
	}
	
	public TypedObject visitNode() {
		ClassObject obj = new ClassObject(methods, data);
		obj.setConstructor(new Function(getConstruct().getParams(), getConstruct().getBody(), Parser.environment.getScopeFromInner(0)));
		
		Parser.environment.define(token, getName().token, new TypedObject("class", obj));
		
		return null;
	}

	public ASTNode getName() {
		return name;
	}

	public void setName(ASTNode name) {
		this.name = name;
	}

	public FunctionNode getConstruct() {
		return construct;
	}

	public void setConstruct(FunctionNode construct) {
		this.construct = construct;
	}
}
