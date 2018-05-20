package parser;

public class AssignmentNode extends ASTNode {
	ASTNode variable;
	ASTNode value;

	public AssignmentNode(ASTNode l, Token t, ASTNode r) {
		variable = l;
		token = t;
		value = r;
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + variable.toString(depth + 1) + "\n" + value.toString(depth + 1);
	}

	public TypedObject visitNode() {
		
		//TODO: Handle unary and complex assignments
		
		switch(token.value) {
		
		case "++":
			break;
		case "--":
			break;
		case "+=":
			break;
		case "-=":
			break;
		case "/=":
			break;
		case "*=":
			break;
		case "%=":
			break;
			
		default:
			Parser.environment.assign(variable.token, value.visitNode());
		}

		return null;
	}
}
