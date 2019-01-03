package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ast.statement.DeclarationNode;
import ast.structures.StructureBodyNode;

public class Function implements Callable {
	
	public ArrayList<DeclarationNode> params;
	public StructureBodyNode body;
	
	public int arity;
	public Scope parentMap;
	
	public Function(ArrayList<DeclarationNode> params, StructureBodyNode body, Scope map){
		this.params = params;
		this.body = body;
		parentMap = map;
		
		arity = params.size();
	}

	@Override
	public TypedObject call(Environment environment, List<TypedObject> arguments) {
		
		environment.enterScope();
		
		int args = arguments.size();
		
		if(args != arity){
			throw new RuntimeError(arity, args, RuntimeError.MISMATCHED_ARGUMENT_NUMBER);
		}
		
		//initialize parameters with the arguments given
		for(int i = 0; i < args; i++){			
		
			Token paramType = params.get(i).getType().token;
			Token paramName = params.get(i).getName().token;
			
			String argType = arguments.get(i).type;
			
			//check the expected and received type
			if(paramType.value.equals("var") || paramType.value.equals(argType)) {
				environment.define(paramType, paramName, arguments.get(i));
			} else {
				throw new RuntimeError(i, paramType.value, argType, RuntimeError.MISMATCHED_ARGUMENT_TYPE);
			}
		}
		
		//execute the block
		TypedObject result = body.visitNode();
		
		environment.exitScope();
		
		return result;
	}

	@Override
	public int arity() {
		return arity;
	}

}
