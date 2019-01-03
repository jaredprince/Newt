package interpreter;

import interpreter.Expr.Assign;
import interpreter.Expr.Call;
import interpreter.Expr.Conditional;
import interpreter.Expr.Logical;
import interpreter.Expr.Variable;

/**
 * This method implements the Visitor interface to produce a String
 * representing the expression in parenthesized format, ie. ((* (- 123) (group 45.67)))
 * @author Jared
 */
public class ASTPrinter implements Expr.Visitor<String> {

	// test
	public static void main(String[] args) {
		
		Expr expression = new Expr.Binary(
				new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1, 0), new Expr.Literal(123)),
				new Token(TokenType.STAR, "*", null, 1, 0),
				new Expr.Grouping(new Token(TokenType.LEFT_PAREN, "(", null, 1, 0), new Expr.Literal(45.67)));

		System.out.println(new ASTPrinter().print(expression));
		System.out.println(expression.toString());
	}

	/**
	 * Produces a string representing the expression.
	 * @param expr the expression to print
	 * @return the string representation
	 */
	public String print(Expr expr) {
		return expr.accept(this);
	}

	/**
	 * Parenthesizes an expression.
	 * @param name the operator or keyword for the expression
	 * @param exprs the expression
	 * @return the parenthesized string
	 */
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.value == null)
			return "null";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return expr.name.lexeme;
	}

	@Override
	public String visitConditionalExpr(Conditional expr) {
		return parenthesize(expr.operator.lexeme, expr.condition, expr.first, expr.second);
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}

}
