


public class UnaryAST extends ASTNode {

	ASTNode child;
	
	public UnaryAST(Token t){
		token = t;
	}
	
	public UnaryAST(ASTNode c, Token t){
		child = c;
		token = t;
	}
	
	public String toString(int depth){
		String str = "";
		for(int i = 0; i < depth; i++){
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + child.toString(depth + 1);
	}
	
	@Override
	public Object visitNode(){
		Object child = this.child.visitNode();
		
		String val = token.value;
		
		if(token.value.equals("return")){
			return child;
		}
		
		if(token.type == Token.TYPE){
			if(val.equals("int")){
				if(child instanceof Integer){
					return child;
				} else if(child instanceof Double) {
					return new Integer((int)((Double)child).doubleValue());
				} else {
					throw new RuntimeError(token, RuntimeError.CANNOT_CAST);
				}
			}
		}
		
		if(val.equals("print")){
			System.out.println(child);
			return null;
		}

		if(val.equals("!")){
			if(child instanceof Boolean){
				return new Boolean(!((Boolean)child).booleanValue());
			}
			
			else {
				System.err.println("Expect a boolean.");
				System.exit(0);
			}
		}
		
		if(val.equals("|")){
			if(child instanceof Double){
				return new Double(Math.abs(( (Double)child).doubleValue() ));
			}
			
			if(child instanceof Integer){
				return new Integer(Math.abs(( (Integer)child).intValue() ));
			}
			
			System.err.println("Expect a numeric value.");
			System.exit(0);
		}
		
		if(val.equals("-")) {
			if(child instanceof Double){
				return new Double( -((Double)child).doubleValue() );
			}
			
			if(child instanceof Integer){
				return new Integer( -((Integer)child).intValue() );
			}
			
			System.err.println("Expect a numeric value.");
			System.exit(0);
		}
		
		return null;
	}
}
