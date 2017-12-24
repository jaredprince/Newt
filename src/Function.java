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

		//TODO: check types of arguments match parameters
		
		Parser.environment.enterScope();
		
		ArrayList<ASTNode> parameters = ((NaryAST) node.left).nodes;
		
		if(arguments.size() != arity){
			throw new RuntimeError(null, RuntimeError.MISMATCHED_ARGUMENTS);
		}
		
		for(int i = 0; i < arguments.size(); i++){
			//assign argument i to parameter i
			Parser.environment.define(((BinaryAST) parameters.get(i)).left.token, ((BinaryAST) parameters.get(i)).right.token, arguments.get(i));
		
			
			String expectedType = ((BinaryAST) parameters.get(i)).left.token.value;
//			if(arguments.get(i)){
//				
//			}
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
