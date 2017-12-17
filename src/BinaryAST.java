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

		Object left = this.left.visitNode();
		Object right = this.right.visitNode();

		// handles expressions and assignments
		if (token.type == Token.OPERATOR) {

			// handles assignments
			if (token.subtype == Token.ASSIGNMENT) {
				// change the master value of this variable
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

				if ((left instanceof Double || left instanceof Integer)
						&& (right instanceof Double || right instanceof Integer)) {

					switch (token.value) {

					case "<":
						return new Boolean(toDouble(left) < toDouble(right));

					case "==":
						return new Boolean(toDouble(left) == toDouble(right));

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
				
				if(token.value.equals("+") && left instanceof String && right instanceof String){
					return (String)left + (String)right;
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

		//TODO: Handle while, do, switch, etc.
		if (token.type == Token.STRUCTURE) {

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
