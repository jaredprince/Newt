package parser;



import java.util.ArrayList;
import java.util.List;

import ast.ASTNode;
import ast.NaryAST;
import parser.Callable;
import parser.Function;
import parser.Parser;
import parser.Token;
import parser.TypedObject;

public class BinaryAST extends ASTNode {
	ASTNode left;
	ASTNode right;

	public BinaryAST(ASTNode l, Token t, ASTNode r) {
		left = l;
		token = t;
		right = r;
	}

	public String toString(int depth) {
		String str = "";
		for (int i = 0; i < depth; i++) {
			str = str + "  ";
		}
		return str + "Token: " + token.value + "\n" + left.toString(depth + 1) + "\n" + right.toString(depth + 1);
	}

	public TypedObject visitNode() {
		
		// handles expressions and assignments
		if (token.type == Token.OPERATOR) {
			TypedObject left = this.left.visitNode();
			TypedObject right = this.right.visitNode();

			// handles assignments
			if (token.subtype == Token.ASSIGNMENT) {				
				Parser.environment.assign(this.left.token, right);
			}

			if (token.subtype == Token.LOGICAL) {
				try {
					switch (token.value) {
					case "&&":
						return new TypedObject("int", new Boolean((Boolean) left.object && (Boolean) right.object));

					case "||":
						return new TypedObject("int", new Boolean((Boolean) left.object || (Boolean) right.object));

					case "~NOR":
						return new TypedObject("int", new Boolean(!((Boolean) left.object || (Boolean) right.object)));

					case "~NAND":
						return new TypedObject("int", new Boolean(!((Boolean) left.object && (Boolean) right.object)));
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

		}
		
		if(token.value.equals("call")){
			NaryAST args = (NaryAST) right;
			
			List<TypedObject> arguments = new ArrayList<TypedObject>();
			
			//visit each argument to get the object returned
			for(int i = 0; i < args.getNodes().size(); i++){
				arguments.add(args.getNodes().get(i).visitNode());
			}
			
			//return the result of calling the function with the given arguments
			return ((Callable)Parser.environment.get(left.token).object).call(null, arguments);
		}
		
		if (token.type == Token.STRUCTURE) {

			Parser.environment.enterScope();
			
			TypedObject returned_value = null;
			
			if(token.value.equals("do")){
				
				returned_value = right.visitNode();
				
				while(((Boolean) left.visitNode().object)){
					
					//break if the return for that iteration was a break
					if(returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
						break;
					}
					
					returned_value = right.visitNode();
				}
			}
			
			else if(token.value.equals("while")){			
				while((Boolean) left.visitNode().object){
					returned_value = right.visitNode();
					
					//break if the return for that iteration was a break
					if(returned_value.type.equals("token") && ((Token) returned_value.object).value.equals("break")){
						break;
					}
				}
			}
			
			Parser.environment.exitScope();
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
}
