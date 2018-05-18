package parser;

import java.util.ArrayList;

import parser.ASTNode;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

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
	
	public TypedObject visitNode(){
		
		if(!structureBody){
			Parser.environment.enterScope();
		}
		
		if(token.type == Token.GROUPING){
			if(token.value.equals("switch")){
				boolean caseExecuted = false;
				for(int i = 0; i < nodes.size(); i++){
					ASTNode node = nodes.get(i);
					
					if(node.token.value.equals("default") && caseExecuted){
						return null;
					}
					
					TypedObject obj = node.visitNode();
					
					if(obj != null){
						if(obj.type.equals("token")){
							return obj;
						} else {
							caseExecuted = caseExecuted ? true : ((Boolean)obj.object).booleanValue();
						}
					}
											
					//returns exit the block immediately and return a value
					if(node.token.value.equals("return")){
						return obj;
					}
					
					//breaks and continues exit immediately, but do not return a value, so a token is returned as a flag
					if(node.token.value.equals("break") || node.token.value.equals("continue")){
						return new TypedObject("token", node.token);
					}
				}
			}
			else {
				for(int i = 0; i < nodes.size(); i++){
					ASTNode node = nodes.get(i);
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
			}
		}
		
		if(!structureBody){
			Parser.environment.exitScope();
		}
		
		return null;
	}
}
