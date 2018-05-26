package ast.operations;

import ast.ASTNode;
import parser.Token;
import parser.TypedObject;

public class OperationNode extends ASTNode {
	
	ASTNode left;
	private ASTNode right;

	public OperationNode(ASTNode l, Token t, ASTNode r) {
		left = l;
		token = t;
		setRight(r);
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + left.toString(depth + 1) + "\n" + getRight().toString(depth + 1);
	}

	public TypedObject visitNode() {
		
		//evaluate left and right branches
		TypedObject left = this.left.visitNode();
		TypedObject right = this.getRight().visitNode();

		if (token.subtype == Token.LOGICAL) {
			try {
				switch (token.value) {
				case "&&":
					return new TypedObject("boolean", new Boolean((Boolean) left.object && (Boolean) right.object));

				case "||":
					return new TypedObject("boolean", new Boolean((Boolean) left.object || (Boolean) right.object));

				case "~NOR":
					return new TypedObject("boolean", new Boolean(!((Boolean) left.object || (Boolean) right.object)));

				case "~NAND":
					return new TypedObject("boolean", new Boolean(!((Boolean) left.object && (Boolean) right.object)));
				}
			} catch (ClassCastException e) {
				System.err.println("Only boolean values can be used with logical operators.");
				System.exit(0);
			}
		}

		//TODO: Handle objects using "=="
		if (token.subtype == Token.COMPARATIVE) {
			
			//handles any
			if(left.type.equals("token")){
				Token t = (Token) left.object;
				
				if(t.value.equals("any")){
					return new TypedObject("boolean", new Boolean(true));
				}
			}
			
			if(right.type.equals("token")){
				Token t = (Token) right.object;
				
				if(t.value.equals("any")){
					return new TypedObject("boolean", new Boolean(true));
				}
			}
			
			if(token.value.equals("==")){
				if(left.type.equals("double") && right.type.equals("int")){
					return new TypedObject("boolean", new Boolean(((Double)left.object).equals(new Double(((Integer)right.object).intValue()))));
				}
				
				return new TypedObject("boolean", new Boolean(left.object.equals(right.object)));
			}

			if ((left.type.equals("double") || left.type.equals("int"))
					&& (right.type.equals("double") || right.type.equals("int"))) {

				switch (token.value) {

				case "<":
					return new TypedObject("boolean", new Boolean(toDouble(left.object) < toDouble(right.object)));

				case "<=":
					return new TypedObject("boolean", new Boolean(toDouble(left.object) <= toDouble(right.object)));
					
				case ">=":
					return new TypedObject("boolean", new Boolean(toDouble(left.object) >= toDouble(right.object)));
					
				case ">":
					return new TypedObject("boolean", new Boolean(toDouble(left.object) > toDouble(right.object)));
				}

			} else {
				System.err.println("Only numeric values can be used with comparative operators.");
				System.exit(0);
			}
		}

		if (token.subtype == Token.MATHEMATICAL) {
			
			//TODO: account for non-literal returns
			if(token.value.equals("+") && (left.type.equals("string") || right.type.equals("string"))){					
				return new TypedObject("string", left.object.toString() + right.object.toString());
			}
			
			if ((left.type.equals("double") || left.type.equals("int"))
					&& (right.type.equals("double") || right.type.equals("int"))) {

				switch (token.value) {

				case "+":
					return new TypedObject("double", new Double(toDouble(left.object) + toDouble(right.object)));

				case "*":
					return new TypedObject("double", new Double(toDouble(left.object) * toDouble(right.object)));

				case "/":
					return new TypedObject("double", new Double(toDouble(left.object) / toDouble(right.object)));
					
				case "-":
					return new TypedObject("double", new Double(toDouble(left.object) - toDouble(right.object)));
					
				case "%":
					return new TypedObject("double", new Double(toDouble(left.object) % toDouble(right.object)));
					
				case "^":
					return new TypedObject("double", new Double(Math.pow(toDouble(left.object), toDouble(right.object))));
				}

			} else {
				System.err.println("Only numeric values can be used with mathematical operators.");
				System.exit(0);
			}
		}
		
		if(token.subtype == Token.MEMBERSHIP) {
			//TODO: handle membership
		}

		return null;
	}

	public static Double toDouble(Object o){
		if(o instanceof Double){
			return (Double)o;
		}
		
		if(o instanceof Integer){
			return new Double(((Integer)o).intValue());
		}
		
		return null;
	}

	public ASTNode getRight() {
		return right;
	}

	public void setRight(ASTNode right) {
		this.right = right;
	}
}
