package ast.statement;

import ast.ASTNode;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class AssignmentNode extends ASTNode {
	private ASTNode variable;
	private ASTNode value;

	public AssignmentNode(ASTNode l, Token t, ASTNode r) {
		setVariable(l);
		token = t;
		setValue(r);
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + getVariable().toString(depth + 1) + "\n" + getValue().toString(depth + 1);
	}

	public TypedObject visitNode() {
		Parser.environment.assign(getVariable().token, getValue().visitNode());
		return null;
	}

	public ASTNode getValue() {
		return value;
	}

	public void setValue(ASTNode value) {
		this.value = value;
	}

	public ASTNode getVariable() {
		return variable;
	}

	public void setVariable(ASTNode variable) {
		this.variable = variable;
	}
}
