package parser;

public class IfElseNode extends ASTNode {
	
	ASTNode condition;
	NaryAST ifBody;
	NaryAST elseBody;
	
	public IfElseNode(Token t){
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + condition.toString(depth + 1) + "\n" + ifBody.toString(depth + 1) + "\n" + elseBody.toString(depth + 1);
	}
	
	@Override
	public TypedObject visitNode(){
		
		/* If statements will return token statements if their bodies do (break, continue, etc.).
		 * Otherwise, they return a boolean denoting if the condition was true.
		 * This is used by the switch to check for default case. */

		if((Boolean)condition.visitNode().object){
			TypedObject c = ifBody.visitNode();
			return c != null ? c : new TypedObject("boolean", new Boolean(true));
		} else if(elseBody.token.type != Token.BLANK){
			TypedObject r = elseBody.visitNode();
			return r != null ? r : new TypedObject("boolean", new Boolean(false));
		} else {
			return new TypedObject("boolean", new Boolean(false));
		}
	}
}
