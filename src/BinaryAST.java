import java.util.ArrayList;
import java.util.List;

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

	public Object visitNode() {
		
		// handles expressions and assignments
		if (token.type == Token.OPERATOR) {
			Object left = this.left.visitNode();
			Object right = this.right.visitNode();
			
			//TODO: Handle compound assignment (a += b)
			// handles assignments
			if (token.subtype == Token.ASSIGNMENT) {
				
				//a function variable gets the actual node
				if(this.left.token.value.equals("function")){
					Parser.environment.assign(this.left.token, new Function((BinaryAST)right));
				}
				
				Parser.environment.assign(this.left.token, right);
			}

			if (token.subtype == Token.LOGICAL) {
				try {
					switch (token.value) {
					case "&&":
						return new Boolean((Boolean) left && (Boolean) right);

					case "||":
						return new Boolean((Boolean) left || (Boolean) right);

					case "~NOR":
						return new Boolean(!((Boolean) left || (Boolean) right));

					case "~NAND":
						return new Boolean(!((Boolean) left && (Boolean) right));
					}
				} catch (ClassCastException e) {
					System.err.println("Only boolean values can be used with logical operators.");
					System.exit(0);
				}
			}

			//TODO: Handle objects using "=="
			if (token.subtype == Token.COMPARATIVE) {
				
				if(token.value.equals("==")){
					if(left instanceof Double && right instanceof Integer){
						return new Boolean(((Double)left).equals(new Double(((Integer)right).intValue())));
					}
					
					return new Boolean(left.equals(right));
				}

				if ((left instanceof Double || left instanceof Integer)
						&& (right instanceof Double || right instanceof Integer)) {

					switch (token.value) {

					case "<":
						return new Boolean(toDouble(left) < toDouble(right));

					case "<=":
						return new Boolean(toDouble(left) <= toDouble(right));
						
					case ">=":
						return new Boolean(toDouble(left) >= toDouble(right));
						
					case ">":
						return new Boolean(toDouble(left) > toDouble(right));
					}

				} else {
					System.err.println("Only numeric values can be used with comparative operators.");
					System.exit(0);
				}
			}

			if (token.subtype == Token.MATHEMATICAL) {
				
				//TODO: account for non-literal returns
				if(token.value.equals("+") && (left instanceof String || right instanceof String)){					
					return left.toString() + right.toString();
				}
				
				if ((left instanceof Double || left instanceof Integer)
						&& (right instanceof Double || right instanceof Integer)) {

					switch (token.value) {

					case "+":
						return new Double(toDouble(left) + toDouble(right));

					case "*":
						return new Double(toDouble(left) * toDouble(right));

					case "/":
						return new Double(toDouble(left) / toDouble(right));
						
					case "-":
						return new Double(toDouble(left) - toDouble(right));
						
					case "%":
						return new Double(toDouble(left) % toDouble(right));
						
					case "^":
						return new Double(Math.pow(toDouble(left), toDouble(right)));
					}

				} else {
					System.err.println("Only numeric values can be used with mathematical operators.");
					System.exit(0);
				}
			}

		}
		
		if(token.value.equals("call")){
			NaryAST args = (NaryAST) right;
			
			List<Object> arguments = new ArrayList<Object>();
			
			//visit each argument to get the object returned
			for(int i = 0; i < args.nodes.size(); i++){
				arguments.add(args.nodes.get(i).visitNode());
			}
			
			//return the result of calling the function with the given arguments
			return ((Callable)Parser.environment.get(left.token)).call(null, arguments);
		}

		if(token.value.equals("func")){
			if(Parser.environment.depth > 0){
				Parser.environment.define(token, left.token, new Function((BinaryAST) right, Parser.environment.getScopeFromInner(0)));
			}
			else {
				Parser.environment.define(token, left.token, new Function((BinaryAST)right));
			}
		}
		
		if (token.type == Token.STRUCTURE) {

			Parser.environment.enterScope();
			
			if(token.value.equals("do")){
				
				right.visitNode();
				
				while((Boolean) left.visitNode()){
					right.visitNode();
				}
			}
			
			else if(token.value.equals("while")){				
				while((Boolean) left.visitNode()){
					right.visitNode();
				}
			}

			else if(token.value.equals("switch")){
				int size = ((NaryAST)right).nodes.size() - 1;
				
				for(int i = 0; i < size; i++){
					if(left.visitNode() == ((BinaryAST)((NaryAST)right).nodes.get(i)).left){
						((BinaryAST)((NaryAST)right).nodes.get(i)).right.visitNode();
					}
				}
				
				if(((NaryAST)right).nodes.get(size).token.value.equals("case")){
					if(left.visitNode() == ((BinaryAST)((NaryAST)right).nodes.get(size)).left){
						((BinaryAST)((NaryAST)right).nodes.get(size)).right.visitNode();
					}
				} else {
					((BinaryAST)((NaryAST)right).nodes.get(size)).right.visitNode();
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
