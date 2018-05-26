package ast.statement;

import java.util.ArrayList;
import java.util.List;

import ast.ASTNode;
import ast.NaryAST;
import parser.Callable;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class CallNode extends ASTNode {
	
	ASTNode function;
	NaryAST args;

	public CallNode(ASTNode l, Token t, NaryAST r) {
		function = l;
		token = t;
		args = r;
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + function.toString(depth + 1) + "\n" + args.toString(depth + 1);
	}

	public TypedObject visitNode() {
		
		List<TypedObject> arguments = new ArrayList<TypedObject>();
		
		//visit each argument to get the object returned
		for(int i = 0; i < args.getNodes().size(); i++){
			arguments.add(args.getNodes().get(i).visitNode());
		}
		
		//return the result of calling the function with the given arguments
		Callable function = (Callable) Parser.environment.get(this.function.token).object;
		return function.call(null, arguments);
	}

}