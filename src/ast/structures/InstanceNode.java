package ast.structures;

import java.util.ArrayList;
import java.util.List;

import ast.ASTNode;
import ast.NaryAST;
import parser.ClassObject;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class InstanceNode extends ASTNode {
	
	ASTNode className;
	NaryAST args;
	
	public InstanceNode(ASTNode name, Token t, NaryAST args) {
		this.args = args;
		className = name;
		token = t;
	}
	
	public TypedObject visitNode() {
		//the list of arguments to pass to the constructor
		List<TypedObject> arguments = new ArrayList<TypedObject>();
		
		//visit each argument to get the object returned
		for(int i = 0; i < args.getNodes().size(); i++){
			arguments.add(args.getNodes().get(i).visitNode());
		}
		
		//retrieve the class object
		ClassObject obj = (ClassObject) Parser.environment.get(className.token).object;
		
		//return the instance generated from the class
		return obj.generateInstance(arguments);
	}

}
