package parser;

import java.util.ArrayList;

public class FunctionNode extends ASTNode {
	ASTNode name;
	ArrayList<DeclarationNode> params; //change to param list node
	StructureBodyNode body;

	public FunctionNode(ASTNode name, ArrayList<DeclarationNode> l, Token t, StructureBodyNode r) {
		this.name = name;
		params = l;
		token = t;
		body = r;
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + "Parameters" + "\n" + body.toString(depth + 1);
	}

	public TypedObject visitNode() {
		
//		//TODO: update 'Function' to match new params
//		if(Parser.environment.depth > 0){
//			Parser.environment.define(token, name.token, new TypedObject("func", new Function(params, body, Parser.environment.getScopeFromInner(0))));
//		}
//		else {
//			Parser.environment.define(token, name.token, new TypedObject("func", new Function(params, body)));
//		}
		return null;
	}
}
