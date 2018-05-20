package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Function implements Callable {
	 
	public BinaryAST node;
	public int arity;
	public Map<String, TypedObject> parentMap;
	
	public Function(BinaryAST node){
		this.node = node;
		
		//TODO: figure out how to give a range of arities
		//arity is the number of parameters
		arity = ((NaryAST)node.left).nodes.size();
	}
	
	public Function(BinaryAST node, Map<String, TypedObject> map){
		this.node = node;
		parentMap = map;
		
		arity = ((NaryAST)node.left).nodes.size();
	}

	@Override
	public TypedObject call(Parser parser, List<TypedObject> arguments) {
		
		Parser.environment.enterScope();
		
		ArrayList<ASTNode> parameters = ((NaryAST) node.left).nodes;
		
		int args = arguments.size();
		
		if(args != arity){
			throw new RuntimeError(arity, args, RuntimeError.MISMATCHED_ARGUMENT_NUMBER);
		}
		
		//initialize parameters with the arguments given
		for(int i = 0; i < args; i++){			
		
			Token paramType = ((BinaryAST) parameters.get(i)).left.token;
			Token paramName = ((BinaryAST) parameters.get(i)).right.token;
			
			String argType = arguments.get(i).type;
			
			//check the expected and received type
			if(paramType.value.equals(argType)) {
				Parser.environment.define(paramType, paramName, arguments.get(i));
			} else {
				throw new RuntimeError(i, paramType.value, argType, RuntimeError.MISMATCHED_ARGUMENT_TYPE);
			}
		}
		
		//execute the block
		((NaryAST) node.right).structureBody = true;
		TypedObject result = node.right.visitNode();
		
		Parser.environment.exitScope();
		
		return result;
	}

	@Override
	public int arity() {
		return arity;
	}

}
