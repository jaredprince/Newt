package interpreter;

import java.util.ArrayList;

public abstract class Expr implements Cloneable {
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
		T visitSharpExpr(Sharp expr);
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

		public Conditional mouldClone() {
			return new Conditional(condition.mouldClone(), operator, first.mouldClone(), second.mouldClone());
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

		public Binary mouldClone() {
			return new Binary(left.mouldClone(), operator, right.mouldClone());
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

		public Logical mouldClone() {
			return new Logical(left.mouldClone(), operator, right.mouldClone());
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

		public Grouping mouldClone() {
			return new Grouping(grouping, expression.mouldClone());
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

		public Literal mouldClone() {
			return new Literal(value);
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

		public Unary mouldClone() {
			return new Unary(operator, right.mouldClone());
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

		public Variable mouldClone() {
			return new Variable(name);
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

		public Assign mouldClone() {
			return new Assign(name, operator, value.mouldClone());
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

		public UnaryAssign mouldClone() {
			return new UnaryAssign(name, operator);
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

		public Call mouldClone() {
			return new Call(callee.mouldClone(), parenthesis, arrayListClone(arguments));
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitCallExpr(this);
		}

		public final Expr callee;
		public final Token parenthesis;
		public final ArrayList<Expr> arguments;
	}

	public static class Sharp extends Expr {
		public Sharp(Expr name) {
			this.name = name;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + name.toString(depth + 1);
		}

		public Sharp mouldClone() {
			return new Sharp(name.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitSharpExpr(this);
		}

		public final Expr name;
	}


	abstract <T> T accept(Visitor<T> visitor);
	abstract String toString(int i);
	abstract Expr mouldClone();

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

	public static <T> ArrayList<T> arrayListClone(ArrayList<T> list) {
		if(list == null) { 
			return null;
		}

		ArrayList<T> newList = new ArrayList<T>();

		for(int i = 0; i < list.size(); i++) {
			T obj = list.get(i);

			if(obj instanceof Expr) {
				newList.add((T) ((Expr) obj).mouldClone());
			} else if (obj instanceof Stmt){
				newList.add((T) ((Stmt) obj).mouldClone());
			} else {
				newList.add(obj);
			}
		}

		return newList;
	}
}
