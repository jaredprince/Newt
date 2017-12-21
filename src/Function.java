import java.util.ArrayList;
import java.util.List;

public class Function implements Callable {
	
	public BinaryAST node;
	public int arity;
	
	public Function(BinaryAST node){
		this.node = node;
		
		//TODO: figure out how to give a range of arities
		//arity is the number of parameters
		arity = ((NaryAST)node.left).nodes.size();
	}

	@Override
	public Object call(Parser parser, List<Object> arguments) {

		//TODO: check types of arguments match parameters
		
		Parser.environment.enterScope();
		
		ArrayList<ASTNode> parameters = ( (NaryAST) node.left).nodes;
		
		for(int i = 0; i < arguments.size(); i++){
			//assign argument i to parameter i
			Parser.environment.define(((BinaryAST) parameters.get(i)).left.token, ((BinaryAST) parameters.get(i)).right.token, arguments.get(i));
		}
		
		//TODO: make the block and the argument declarations the same scope
		//execute the block
		Object result = node.right.visitNode();
		
		Parser.environment.exitScope();
		
		return result;
	}

	@Override
	public int arity() {
		return arity;
	}

}
