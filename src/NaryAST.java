import java.util.ArrayList;

public class NaryAST extends ASTNode {
	
	ArrayList<ASTNode> nodes;
	
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
		Parser.environment.enterScope();
		
		if(token.type == Token.GROUPING){
			for(int i = 0; i < nodes.size(); i++){
				nodes.get(i).visitNode();
			}
		}
		
		Parser.environment.exitScope();
		
		return null;
	}
}
