import java.util.ArrayList;

public class NaryAST extends ASTNode {
	
	ArrayList<ASTNode> nodes;
	
	//used to ensure variables declared in for loops
	//and functions are in the same scope as teh body
	boolean structureBody;
	
	public NaryAST(){
		nodes = new ArrayList<ASTNode>();
	}
	
	public NaryAST(Token t){
		nodes = new ArrayList<ASTNode>();
		token = t;
	}
	
	public NaryAST(Token t, ArrayList<ASTNode> nodes){
		for(int i = 0; i < nodes.size(); i++){
			this.nodes.add(nodes.get(i));
		}
		
		token = t;
	}
	
	public void addNode(ASTNode node){
		nodes.add(node);
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		
		str += "Token: " + token.value;
		
		for(int i = 0; i < nodes.size(); i++){
			str += "\n" + nodes.get(i).toString(depth + 1);
		}
		
		return str;
	}
	
	public Object visitNode(){
		
		if(!structureBody){
			Parser.environment.enterScope();
		}
		
		if(token.type == Token.GROUPING){
			for(int i = 0; i < nodes.size(); i++){
				ASTNode node = nodes.get(i);
				Object obj = node.visitNode();
				
				if(obj != null)
					return obj;
				
				//returns exit the block immediately and return a value
				if(node.token.value.equals("return")){
					return obj;
				}
				
				//breaks and continues exit immediately, but do not return a value, so a token is returned as a flag
				if(node.token.value.equals("break") || node.token.value.equals("continue")){
					return node.token;
				}
			}
		}
		
		if(!structureBody){
			Parser.environment.exitScope();
		}
		
		return null;
	}
}
