package ast.structures;

import java.util.ArrayList;

import ast.ASTNode;
import parser.Token;
import parser.TypedObject;

public class SwitchNode extends ASTNode {
	
	/* The value in the switch header. */
	ASTNode test;
	
	ArrayList<CaseNode> cases;
	private StructureBodyNode defaultNode;
	
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
		setDefaultNode(node);
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
			TypedObject obj = node.visitNode();
			
			if(obj != null){
				if(obj.type.equals("token")){
					
					//returns are passed up through the switch
					if(((Token)(obj.object)).value.equals("return")){
						return obj;
					}
					
					//breaks and continues cause the switch to exit instantly and are destroyed
					if(((Token)(obj.object)).value.equals("break") || ((Token)(obj.object)).value.equals("continue")){
						return null;
					}
					
					return obj;
				} else {
					caseExecuted = caseExecuted ? true : ((Boolean)obj.object).booleanValue();
				}
			}
		}
		
		if(!caseExecuted && getDefaultNode() != null) {
			return getDefaultNode().visitNode();
		}
		
		return null;
	}

	public StructureBodyNode getDefaultNode() {
		return defaultNode;
	}

	public void setDefaultNode(StructureBodyNode defaultNode) {
		this.defaultNode = defaultNode;
	}

}
