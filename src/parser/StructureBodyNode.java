package parser;

import java.util.ArrayList;

public class StructureBodyNode extends ASTNode {

	ArrayList<ASTNode> statements;
	
	public StructureBodyNode(){
		statements = new ArrayList<ASTNode>();
	}
	
	public StructureBodyNode(Token t){
		statements = new ArrayList<ASTNode>();
		token = t;
	}
	
	public StructureBodyNode(Token t, ArrayList<ASTNode> nodes){
		for(int i = 0; i < nodes.size(); i++){
			statements.add(nodes.get(i));
		}
		
		token = t;
	}
	
	public void addNode(ASTNode node){
		statements.add(node);
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		
		str += "Token: " + token.value;
		
		for(int i = 0; i < statements.size(); i++){
			str += "\n" + statements.get(i).toString(depth + 1);
		}
		
		return str;
	}
	
	public TypedObject visitNode(){
		
		for(int i = 0; i < statements.size(); i++){
			ASTNode node = statements.get(i);
			TypedObject obj = node.visitNode();
			
			if(obj != null && obj.type.equals("token"))
				return obj;
			
			//returns exit the block immediately and return a value
			if(node.token.value.equals("return")){
				return obj;
			}
			
			//breaks and continues exit immediately, but do not return a value, so a token is returned as a flag
			if(node.token.value.equals("break") || node.token.value.equals("continue")){
				return new TypedObject("token", node.token);
			}
		}
		
		return null;
	}
}
