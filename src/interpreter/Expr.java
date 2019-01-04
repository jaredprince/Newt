package interpreter;

import java.util.ArrayList;

public abstract class Expr {
	interface Visitor<T> {
		T visitConditionalExpr(Conditional expr);
		T visitBinaryExpr(Binary expr);
		T visitLogicalExpr(Logical expr);
		T visitGroupingExpr(Grouping expr);
		T visitLiteralExpr(Literal expr);
		T visitUnaryExpr(Unary expr);
		T visitVariableExpr(Variable expr);
		T visitAssignExpr(Assign expr);
		T visitUnaryAssignExpr(UnaryAssign expr);
		T visitCallExpr(Call expr);
	}

	public static class Conditional extends Expr {
		public Conditional(Expr condition, Token operator, Expr first, Expr second) {
			this.condition = condition;
			this.operator = operator;
			this.first = first;
			this.second = second;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + condition.toString(depth + 1) + "\n" + first.toString(depth + 1) + "\n" + second.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitConditionalExpr(this);
		}

		public final Expr condition;
		public final Token operator;
		public final Expr first;
		public final Expr second;
	}

	public static class Binary extends Expr {
		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + left.toString(depth + 1) + "\n" + right.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public final Expr left;
		public final Token operator;
		public final Expr right;
	}

	public static class Logical extends Expr {
		public Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + left.toString(depth + 1) + "\n" + right.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitLogicalExpr(this);
		}

		public final Expr left;
		public final Token operator;
		public final Expr right;
	}

	public static class Grouping extends Expr {
		public Grouping(Token grouping, Expr expression) {
			this.grouping = grouping;
			this.expression = expression;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + grouping.lexeme + "\n" + expression.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		public final Token grouping;
		public final Expr expression;
	}

	public static class Literal extends Expr {
		public Literal(Object value) {
			this.value = value;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + value;
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		public final Object value;
	}

	public static class Unary extends Expr {
		public Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + right.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public final Token operator;
		public final Expr right;
	}

	public static class Variable extends Expr {
		public Variable(Token name) {
			this.name = name;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + name.lexeme;
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitVariableExpr(this);
		}

		public final Token name;
	}

	public static class Assign extends Expr {
		public Assign(Token name, Token operator, Expr value) {
			this.name = name;
			this.operator = operator;
			this.value = value;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + name.lexeme + "\n" + value.toString(depth + 1);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitAssignExpr(this);
		}

		public final Token name;
		public final Token operator;
		public final Expr value;
	}

	public static class UnaryAssign extends Expr {
		public UnaryAssign(Token name, Token operator) {
			this.name = name;
			this.operator = operator;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + operator.lexeme + "\n" + name.lexeme;
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitUnaryAssignExpr(this);
		}

		public final Token name;
		public final Token operator;
	}

	public static class Call extends Expr {
		public Call(Expr callee, Token parenthesis, ArrayList<Expr> arguments) {
			this.callee = callee;
			this.parenthesis = parenthesis;
			this.arguments = arguments;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + parenthesis.lexeme + "\n" + callee.toString(depth + 1) + "\n" + arrayListToString(arguments);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitCallExpr(this);
		}

		public final Expr callee;
		public final Token parenthesis;
		public final ArrayList<Expr> arguments;
	}


	abstract <T> T accept(Visitor<T> visitor);
	abstract String toString(int i);

	public String toString() {
		return this.toString(0);
	}

	public static String arrayListToString(ArrayList<?> list) {
		String str = "";
		for(int i = 0; i < list.size(); i++) {
			str = str + "\n" + list.get(i);
		}

		return str;
	}
}
