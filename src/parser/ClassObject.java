package parser;

import java.util.ArrayList;
import java.util.Map;

import ast.statement.DeclarationNode;
import ast.structures.FunctionNode;

/**
 * This object will be held in a TypedObject of type 'class' as the obj.
 * @author Jared
 *
 */
public class ClassObject {
	
	ArrayList<FunctionNode> methods;
	ArrayList<DeclarationNode> variables;
	Function constructor;

	public ClassObject(ArrayList<FunctionNode> functions, ArrayList<DeclarationNode> variables) {
		methods = functions;		
		this.variables = variables;
	}
	
	public void setConstructor(Function f) {
		constructor = f;
	}
	
	public TypedObject generateInstance(String name) {
		
		//create a new scope
		Parser.environment.enterScope();
		
		//define functions
		for(int i = 0; i < methods.size(); i++) {
			methods.get(i).visitNode();
		}
		
		//define variables
		for(int i = 0; i < variables.size(); i++) {
			variables.get(i).visitNode();
		}
		
		//get the populated scope
		Map<String, TypedObject> scope = Parser.environment.getScopeFromInner(0);
		
		//exit the scope
		Parser.environment.exitScope();

		return new TypedObject(name, scope);
	}
}
