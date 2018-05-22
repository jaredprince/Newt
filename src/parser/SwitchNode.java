package parser;

import java.util.ArrayList;

public class SwitchNode extends ASTNode {
	
	/* The value in the switch header. */
	ASTNode test;
	
	ArrayList<CaseNode> cases;
	StructureBodyNode defaultNode;
	
	public SwitchNode(){
		cases = new ArrayList<CaseNode>();
	}
	
	public SwitchNode(Token t){
		cases = new ArrayList<CaseNode>();
		token = t;
	}
	
	public SwitchNode(Token t, ArrayList<CaseNode> nodes, ASTNode test){
		for(int i = 0; i < nodes.size(); i++){
			cases.add(nodes.get(i));
		}
		
		token = t;
	}
	
	public void addNode(CaseNode node){
		cases.add(node);
	}
	
	public void setDefault(StructureBodyNode node) {
		defaultNode = node;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		
		str += "Token: " + token.value;
		
		for(int i = 0; i < cases.size(); i++){
			str += "\n" + cases.get(i).toString(depth + 1);
		}
		
		return str;
	}
	
	public TypedObject visitNode(){
		
		boolean caseExecuted = false;
		
		for(int i = 0; i < cases.size(); i++){
			CaseNode node = cases.get(i);
			
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
		
		return null;
	}

}
