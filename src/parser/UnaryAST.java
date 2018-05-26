package parser;

import ast.ASTNode;

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
	public TypedObject visitNode(){
		TypedObject child = this.child.visitNode();
		
		String val = token.value;
		
		if(token.value.equals("return")){
			return child;
		}
		
		if(token.type == Token.DATA_TYPE){
			if(val.equals("int")){
				if(child.type.equals("int")){
					return child;
				} else if(child.type.equals("double")) {
					return new TypedObject("int", new Integer((int)((Double)child.object).doubleValue()));
				} else {
					throw new RuntimeError(token, RuntimeError.CANNOT_CAST);
				}
			}
			
			else if(val.equals("double")){
				if(child.type.equals("double")){
					return child;
				} else if(child.type.equals("int")) {
					return new TypedObject("double", new Double(((Integer)child.object).intValue()));
				} else {
					throw new RuntimeError(token, RuntimeError.CANNOT_CAST);
				}
			}
			
			else if(val.equals("string")){
				return new TypedObject("string", child.toString());
			}
		}
		
		if(val.equals("print")){
			System.out.println(child.object);
			return null;
		}

		if(val.equals("!")){
			if(child.type.equals("boolean")){
				return new TypedObject("boolean", new Boolean(!((Boolean)child.object).booleanValue()));
			}
			
			else {
				
				throw new RuntimeError(token, RuntimeError.BOOLEAN_INPUT_EXPECTED);
			}
		}
		
		if(val.equals("|")){
			if(child.type.equals("double")){
				return new TypedObject("double", new Double(Math.abs(( (Double)child.object).doubleValue() )));
			}
			
			if(child.type.equals("int")){
				return new TypedObject("int", new Integer(Math.abs(( (Integer)child.object).intValue() )));
			}
			
			throw new RuntimeError(token, RuntimeError.NUMERIC_INPUT_EXPECTED);
		}
		
		if(val.equals("-")) {
			if(child.type.equals("double")){
				return new TypedObject("double", new Double( -((Double)child.object).doubleValue() ));
			}
			
			if(child.type.equals("int")){
				return new TypedObject("int", new Integer( -((Integer)child.object).intValue() ));
			}
			
			throw new RuntimeError(token, RuntimeError.NUMERIC_INPUT_EXPECTED);
		}
		
		return null;
	}
}
