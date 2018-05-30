package ast;

import java.util.ArrayList;

import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class NaryAST extends ASTNode {
	
	private ArrayList<ASTNode> nodes;
	
	//used to ensure variables declared in for loops
	//and functions are in the same scope as teh body
	boolean structureBody;
	
	public NaryAST(){
		setNodes(new ArrayList<ASTNode>());
	}
	
	public NaryAST(Token t){
		setNodes(new ArrayList<ASTNode>());
		token = t;
	}
	
	public NaryAST(Token t, ArrayList<ASTNode> nodes){
		for(int i = 0; i < nodes.size(); i++){
			this.getNodes().add(nodes.get(i));
		}
		
		token = t;
	}
	
	public void addNode(ASTNode node){
		getNodes().add(node);
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		
		str += "Token: " + token.value;
		
		for(int i = 0; i < getNodes().size(); i++){
			str += "\n" + getNodes().get(i).toString(depth + 1);
		}
		
		return str;
	}
	
	public TypedObject visitNode(){
		
		if(!structureBody){
			Parser.environment.enterScope();
		}
		
		for(int i = 0; i < getNodes().size(); i++){
			ASTNode node = getNodes().get(i);
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
		
		if(!structureBody){
			Parser.environment.exitScope();
		}
		
		return null;
	}

	public ArrayList<ASTNode> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<ASTNode> nodes) {
		this.nodes = nodes;
	}
}
