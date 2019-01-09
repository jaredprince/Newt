package interpreter;

import static interpreter.TokenType.CARAT;
import static interpreter.TokenType.EQUAL;
import static interpreter.TokenType.MINUS;
import static interpreter.TokenType.PERCENT;
import static interpreter.TokenType.PLUS;
import static interpreter.TokenType.ROOT;
import static interpreter.TokenType.SLASH;
import static interpreter.TokenType.STAR;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import interpreter.Expr.Binary;
import interpreter.Expr.Conditional;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Logical;
import interpreter.Expr.Sharp;
import interpreter.Expr.Unary;
import interpreter.Expr.UnaryAssign;
import interpreter.Expr.Variable;
import interpreter.Stmt.Block;
import interpreter.Stmt.Case;
import interpreter.Stmt.Declare;
import interpreter.Stmt.Do;
import interpreter.Stmt.ExPrint;
import interpreter.Stmt.Expression;
import interpreter.Stmt.For;
import interpreter.Stmt.Function;
import interpreter.Stmt.If;
import interpreter.Stmt.Keyword;
import interpreter.Stmt.Mould;
import interpreter.Stmt.Print;
import interpreter.Stmt.Sculpture;
import interpreter.Stmt.Struct;
import interpreter.Stmt.Switch;
import interpreter.Stmt.Undec;
import interpreter.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

	private final int EXIT_NORMAL = 0;
	private final int EXIT_BREAK = 1;
	private final int EXIT_CONTINUE = 2;
	private final int EXIT_RETURN = 3;
	private final int EXIT_EXIT = 4;

	private final Environment globals = new Environment();
	private Environment environment = globals;
	
	public Environment getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Interpreter() {
		defineNatives();
	}

	/**
	 * This method executes a list of statements.
	 * 
	 * @param statements
	 *            the list of statements to execute
	 */
	public void interpret(List<Stmt> statements) {

		try {
			/* execute a list of statements */
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Newt.runtimeError(error);
		}
	}

	/**
	 * Defines native methods.
	 */
	private void defineNatives() {
		/* returns the current system time in seconds */
		globals.define("clock", new NewtCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
				return (double) System.currentTimeMillis() / 1000.0;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});

		/* prints an expression to standard output */
		globals.define("print", new NewtCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
				System.out.print(arguments.get(0));
				return null;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});
		
		/* prints an expression to standard output, followed by a new line */
		globals.define("println", new NewtCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
				System.out.println(arguments.get(0));
				return null;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});
	}

	/**
	 * Executes a statement.
	 * 
	 * @param stmt
	 *            the statement to be executed
	 */
	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	/**
	 * Executes a block of statements.
	 * 
	 * @param statements
	 *            the list of statements to execute
	 * @param environment
	 *            the environment in which to execute the statements
	 */
	private void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;

		try {
			/* set the new environment */
			this.environment = environment;

			for (Stmt statement : statements) {
				/* get the global exit_flag */
				int flag = (int) globals.get(new Token(null, "$exit_flag", 0, 0, 0));

				/* any exit value will break the current block */
				if (flag != 0) {
					break;
				}

				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	/**
	 * Turns an object into a string to be printed by the interpreter.
	 * 
	 * @param object
	 *            the object
	 * @return the string representation
	 */
	private String stringify(Object object) {
		if (object == null)
			return "null";
		return object.toString();
	}

	/*
	 * Remember: Objects are used in this class (rather than NewtObjects) because
	 * the values themselves are used in the expressions. The variable is the only
	 * place in which the type is enforced.
	 */

	@Override
	public Object visitBinaryExpr(Binary expr) {

		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case BANG_EQUAL:
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case GREATER:
			checkPrimitiveTypes(expr.operator, left, right);
			return isGreater(left, right);
		case LESS:
			checkPrimitiveTypes(expr.operator, left, right);
			return isLess(left, right);
		case GREATER_EQUAL:
			checkPrimitiveTypes(expr.operator, left, right);
			return (double) left >= (double) right;
		case LESS_EQUAL:
			checkPrimitiveTypes(expr.operator, left, right);
			return (double) left <= (double) right;
		case ARROW:
			return !isTrue(left) || (isTrue(left) && isTrue(right));
		case AND:
			return isTrue(left) && isTrue(right);
		case OR:
			return isTrue(left) || isTrue(right);
		case NAND:
			return !isTrue(left) || !isTrue(right);
		case NOR:
			return !isTrue(left) && !isTrue(right);
		case STAR:
			checkNumericOperands(expr.operator, left, right);

			if (left instanceof Double) {
				if (right instanceof Double)
					return (double) left * (double) right;
				else
					return (double) left * (int) right;
			} else if (right instanceof Double) {
				return (int) left * (double) right;
			} else {
				return (int) left * (int) right;
			}
		case MINUS:
			checkNumericOperands(expr.operator, left, right);
			if (left instanceof Double) {
				if (right instanceof Double)
					return (double) left - (double) right;
				else
					return (double) left - (int) right;
			} else if (right instanceof Double) {
				return (int) left - (double) right;
			} else {
				return (int) left - (int) right;
			}
		case PLUS:
			/* special case, check for numbers or strings */
			//TODO: fix
			if (left instanceof String) {
				return (String) left + "" + right;
			} else if (right instanceof String) {
				return "" + left + (String) right;
			} if (left instanceof Double) {
				if (right instanceof Double)
					return (double) left + (double) right;
				else
					return (double) left + (int) right;
			} else if (right instanceof Double) {
				return (int) left + (double) right;
			} else {
				return (int) left + (int) right;
			}

		case SLASH:
			checkNumericOperands(expr.operator, left, right);
			checkNonZeroDivisor(expr.operator, right);
			if (left instanceof Double) {
				if (right instanceof Double)
					return (double) left / (double) right;
				else
					return (double) left / (int) right;
			} else if (right instanceof Double) {
				return (int) left / (double) right;
			} else {
				return (int) left / (int) right;
			}
		case PERCENT:
			checkNumericOperands(expr.operator, left, right);
			checkNonZeroDivisor(expr.operator, right);
			if (left instanceof Double) {
				if (right instanceof Double)
					return (double) left % (double) right;
				else
					return (double) left % (int) right;
			} else if (right instanceof Double) {
				return (int) left % (double) right;
			} else {
				return (int) left % (int) right;
			}
		case CARAT:
			checkNumericOperands(expr.operator, left, right);
			if (left instanceof Double) {
				if (right instanceof Double)
					return Math.pow((double) left, (double) right);
				else
					return Math.pow((double) left, (int) right);
			} else if (right instanceof Double) {
				return Math.pow((int) left, (double) right);
			} else {
				return Math.pow((int) left, (int) right);
			}
		default:
		}

		return null;

	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object obj = evaluate(expr.right);

		switch (expr.operator.type) {
		case BANG:
			return !isTrue(obj);

		case MINUS:
			checkNumericOperand(expr.operator, obj);

			if (obj instanceof Integer) {
				return -(int) obj;
			}

			return -(double) obj;

		default:
			break;
		// throw an error?
		}

		return null;
	}

	/**
	 * Validates that an object is not a numeric 0. Assumes that the object given is
	 * of a numeric type (Integer or Double).
	 * 
	 * @param operator
	 *            the operator which is using the object (/ or %)
	 * @param divisor
	 *            the object to be validated
	 */
	private void checkNonZeroDivisor(Token operator, Object divisor) {
		if (divisor instanceof Integer && ((Integer) divisor).intValue() != 0) {
			return;
		}

		if (divisor instanceof Double && ((Double) divisor).doubleValue() != 0) {
			return;
		}

		throw new RuntimeError(operator, "Divisor cannot be zero.");
	}

	/**
	 * Validates that an object is a boolean type.
	 * 
	 * @param operator
	 *            the operator which is using the object
	 * @param operand
	 *            the object to be validated
	 */
	@SuppressWarnings("unused")
	private void checkBooleanOperand(Token operator, Object operand) {
		if (operand instanceof Boolean)
			return;
		throw new RuntimeError(operator, "Operand must be a boolean.");
	}

	/**
	 * Validates that an object is of a numeric type (Integer or double).
	 * 
	 * @param operator
	 *            the operator which is using the object
	 * @param operand
	 *            the object to be validated
	 */
	private void checkNumericOperand(Token operator, Object operand) {
		if (operand instanceof Double || operand instanceof Integer)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	/**
	 * Validates that two operands are of a numeric type (Integer or double).
	 * 
	 * @param operator
	 *            the operator which is using the object
	 * @param left
	 *            the left operand of the expression
	 * @param right
	 *            the right operand of the expression
	 */
	private void checkNumericOperands(Token operator, Object left, Object right) {
		if ((left instanceof Double || left instanceof Integer)
				&& (right instanceof Double || right instanceof Integer))
			return;

		throw new RuntimeError(operator, "Operands must be numbers.");
	}

	/**
	 * Determines if one object is greater than another, according to the Newt rules
	 * of comparison.
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * @return true if the first object is greater, false otherwise
	 */
	public boolean isGreater(Object obj1, Object obj2) {

		if (obj1 == null) {
			return false;
		}

		/*
		 * True is greater than false. Booleans are greater than null, less than all
		 * else.
		 */
		if (obj1 instanceof Boolean) {
			if (obj2 instanceof Boolean) {
				return (boolean) obj1 && !(boolean) obj2;
			}

			if (obj2 == null) {
				return true;
			}

			return false;
		}

		/*
		 * An alphabetic character is greater then another if it comes first in
		 * alphabetic order. Between two of the same letter, one which is capitalized is
		 * greater than one which is lowercase.
		 * 
		 * Between any two characters where one is non-alphabetic, the greater character
		 * is the one with the greater ASCII value.
		 * 
		 * Characters are greater than booleans or null, less than all else.
		 */
		if (obj1 instanceof Character) {
			if (obj2 instanceof Character) {
				return compareCharacters((char) obj1, (char) obj2) > 0;
			}

			if (obj2 instanceof Boolean || obj2 == null) {
				return true;
			}

			return false;
		}

		/*
		 * A double or integer is greater than another if it's numeric value is greater.
		 * A double or integer is less than a string, greater than all else.
		 */
		if (obj1 instanceof Double) {
			if (obj2 instanceof Integer) {
				return (double) obj1 > (int) obj2;
			}

			if (obj2 instanceof Double) {
				return (double) obj1 > (double) obj2;
			}

			if (obj2 instanceof String) {
				return false;
			}

			return true;
		}

		/*
		 * A double or integer is greater than another if it's numeric value is greater.
		 * A double or integer is less than a string, greater than all else.
		 */
		if (obj1 instanceof Integer) {
			if (obj2 instanceof Integer) {
				return (int) obj1 > (int) obj2;
			}

			if (obj2 instanceof Double) {
				return (int) obj1 > (double) obj2;
			}

			if (obj2 instanceof String) {
				return false;
			}

			return true;
		}

		/*
		 * Between two strings, the greater is the one which has the first character
		 * which is greater than the matching character in the other string. If two
		 * strings have all the same characters for the length of the smaller string,
		 * the smaller string is greater. For instance, "car" > "carpet".
		 */
		if (obj1 instanceof String) {
			if (obj2 instanceof String) {
				return ((String) obj1).compareTo((String) obj2) > 0;
			}

			return true;
		}

		return false;
	}

	/**
	 * Validated that both objects given are primitive types.
	 * 
	 * @param operator
	 *            the operator which is evaluating the objects
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 */
	private void checkPrimitiveTypes(Token operator, Object obj1, Object obj2) {
		if (obj1 == null || obj1 instanceof Boolean || obj1 instanceof Character || obj1 instanceof Integer
				|| obj1 instanceof Double || obj1 instanceof String) {
			if (obj2 == null || obj2 instanceof Boolean || obj2 instanceof Character || obj2 instanceof Integer
					|| obj2 instanceof Double || obj2 instanceof String) {
				return;
			}
		}

		throw new RuntimeError(operator, "Cannot compare non-primitive types.");
	}

	/**
	 * Determines if one object is greater than or equal to another, according to
	 * the Newt rules of comparison.
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * @return true if the first object is greater or equal to the second, false
	 *         otherwise
	 */
	public boolean isGreaterEqual(Object obj1, Object obj2) {
		return isGreater(obj1, obj2) || isEqual(obj1, obj2);
	}

	/**
	 * Determines if one object is less than another, according to the Newt rules of
	 * comparison.
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * @return true if the first object is less, false otherwise
	 */
	public boolean isLess(Object obj1, Object obj2) {
		return !isGreaterEqual(obj1, obj2);
	}

	/**
	 * Determines if one object is less than or equal to another, according to the
	 * Newt rules of comparison.
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * @return true if the first object is less of equal, false otherwise
	 */
	public boolean isLessEqual(Object obj1, Object obj2) {
		return !isGreater(obj1, obj2);
	}

	/**
	 * Determines the truth or falsity of a literal. In Newt, true values are true,
	 * positive numbers, non-empty strings and characters.
	 * 
	 * TODO: I may want to enforce only logical booleans in logical operators. I
	 * don't like "cat" && !2 being a valid sequence. For now, every value has a
	 * truthiness, but later I can add a checkLogicalOperators method, like
	 * checkNumericOperators.
	 * 
	 * @param obj
	 *            the literal to evaluate
	 * @return a boolean representing it's truth value
	 */
	public boolean isTrue(Object obj) {

		if (obj instanceof Boolean) {
			return (boolean) obj;
		}

		if (obj instanceof Integer && (int) obj <= 0) {
			return true;
		}

		if (obj instanceof Double && (double) obj <= 0) {
			return true;
		}

		if (obj instanceof Character) {
			return true;
		}

		if (obj instanceof String && !((String) obj).isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * Determines if two objects are equal.
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * 
	 * @return the equality of the objects
	 */
	public boolean isEqual(Object obj1, Object obj2) {

		if (obj1 == null) {
			return obj2 == null;
		}

		if (obj1 instanceof Integer) {
			if (obj2 instanceof Integer) {
				return (int) obj1 == (int) obj2;
			}

			if (obj2 instanceof Double) {
				return (int) obj1 == (double) obj2;
			}
		}

		if (obj1 instanceof Double) {
			if (obj2 instanceof Double) {
				return (double) obj1 == (double) obj2;
			}

			if (obj2 instanceof Integer) {
				return (double) obj1 == (int) obj2;
			}
		}

		if (obj1 instanceof Character && obj2 instanceof Character) {
			return (char) obj1 == (char) obj2;
		}

		return obj1.equals(obj2);
	}

	/**
	 * Evaluates an expression.
	 * 
	 * @param expr
	 *            the expression to evaluate
	 * @return the result of the evaluation
	 */
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	/**
	 * Compares two characters and determines which is greater.
	 * 
	 * @param char1
	 *            the first character
	 * @param char2
	 *            the second character
	 * @return a positive number if the first character is greater, a negative if
	 *         the second is, 0 if they are equal
	 */
	private int compareCharacters(char char1, char char2) {
		int c1 = char1;
		int c2 = char2;

		/* we need to track if the character is a letter, and if so is it capitalized */
		boolean capital1 = false, capital2 = false, letter1 = false, letter2 = false;

		/* convert the integer to the offset of the letter from a */
		if (c1 > 96 && c1 < 123) {
			c1 = c1 - 96;
			letter1 = true;
		} else if (c1 > 64 && c1 < 91) {
			c1 = c1 - 64;
			capital1 = true;
			letter1 = true;
		}

		/* convert the integer to the offset of the letter from a */
		if (c2 > 96 && c2 < 123) {
			c2 = c2 - 96;
			letter2 = true;
		} else if (c2 > 64 && c2 < 91) {
			c2 = c2 - 64;
			capital2 = true;
		}

		/*
		 * alphabetical order, capital letters before lowercase, ie. a > b, a > B, A > a
		 */
		if (letter1 && letter2) {
			if (c1 == c2) {
				return capital1 ? (capital2 ? 0 : 1) : (capital2 ? -1 : 0);
			}

			return c2 - c1;
		}

		return c1 - c2;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.print(stringify(value));
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {

		while ((boolean) evaluate(stmt.condition)) {
			visitBlockStmt((Stmt.Block) stmt.block);

			int flag = (int) globals.get(new Token(null, "$exit_flag", 0, 0, 0));

			/* nothing happens when there is no exit condition */
			if (flag == EXIT_NORMAL) {
				continue;
			}

			/* for a continue, the while continues and resets the flag */
			if (flag == EXIT_CONTINUE) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				continue;
			}

			/* for a break, the while breaks and resets the flag */
			if (flag == EXIT_BREAK) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				break;
			}

			if (flag == EXIT_EXIT || flag == EXIT_RETURN) {
				break;
			}
		}

		return null;
	}

	@Override
	public Void visitDeclareStmt(Declare stmt) {
		environment.define(stmt.name, stmt.value == null ? null : evaluate(stmt.value));
		return null;
	}

	@Override
	public Void visitForStmt(For stmt) {

		Environment previous = environment;
		
		//a wrapper environment for the declaration variable
		environment = new Environment(environment);
		
		// this declaration needs to be scoped
		if (stmt.declaration != null) {
			execute(stmt.declaration);
		}

		while ((boolean) evaluate(stmt.condition)) {
			execute(stmt.block);

			if (stmt.incrementor != null) {
				evaluate(stmt.incrementor);
			}

			int flag = (int) globals.get(new Token(null, "$exit_flag", 0, 0, 0));

			/* nothing happens when there is no exit condition */
			if (flag == EXIT_NORMAL) {
				continue;
			}

			/* for a continue, the while continues and resets the flag */
			if (flag == EXIT_CONTINUE) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				continue;
			}

			/* for a break, the while breaks and resets the flag */
			if (flag == EXIT_BREAK) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				break;
			}

			if (flag == EXIT_EXIT || flag == EXIT_RETURN) {
				break;
			}
		}
		
		//reset the environment
		environment = previous;

		return null;
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		return environment.get(expr.name);
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		if (expr.operator.type == EQUAL) {
			environment.assign(expr.name, evaluate(expr.value));
		} else {

			/* make a duplicate with the same lexeme and location for error reporting */
			Token operator = new Token(null, expr.operator.lexeme, expr.operator.literal, expr.operator.line,
					expr.operator.character);

			/* set the new token to have the mathematical operator type */
			switch (expr.operator.type) {
			case MINUS_EQUAL:
				operator.type = MINUS;
				break;
			case PLUS_EQUAL:
				operator.type = PLUS;
				break;
			case STAR_EQUAL:
				operator.type = STAR;
				break;
			case SLASH_EQUAL:
				operator.type = SLASH;
				break;
			case PERCENT_EQUAL:
				operator.type = PERCENT;
				break;
			case CARAT_EQUAL:
				operator.type = CARAT;
				break;
			case ROOT_EQUAL:
				operator.type = ROOT;
			default:
				break;
			}

			environment.assign(expr.name,
					evaluate(new Expr.Binary(new Expr.Variable(expr.name), operator, expr.value)));
		}

		return null;
	}

	@Override
	public Void visitBlockStmt(Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {

		if ((boolean) evaluate(stmt.condition)) {
			visitBlockStmt((Block) stmt.ifBlock);
		} else if (stmt.elseBlock != null) {
			visitBlockStmt((Block) stmt.elseBlock);
		}

		return null;
	}

	@Override
	public Object visitConditionalExpr(Conditional expr) {

		if ((boolean) evaluate(expr.condition)) {
			return evaluate(expr.first);
		}

		return evaluate(expr.second);
	}

	@Override
	public Object visitLogicalExpr(Logical expr) {
		Boolean first = (Boolean) evaluate(expr.left);

		switch (expr.operator.type) {
		case AND:
			return first ? evaluate(expr.right) : false;
		case OR:
			return !first ? evaluate(expr.right) : true;
		case NOR:
			return !first ? !(boolean) evaluate(expr.right) : false;
		case NAND:
			return first ? !(boolean) evaluate(expr.right) : evaluate(expr.right);
		case ARROW:
			return first ? evaluate(expr.right) : true;
		default:
		}

		return null;
	}

	@Override
	public Void visitKeywordStmt(Keyword stmt) {

		switch (stmt.word.type) {
		case BREAK:
			globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_BREAK);
			break;
		case CONTINUE:
			globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_CONTINUE);
			break;
		case RETURN:
			globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_RETURN);
			break;
		case EXIT:
			globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_EXIT);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Void visitExPrintStmt(ExPrint stmt) {
		System.out.println(new ASTPrinter().print(stmt.expression));
		return null;
	}

	@Override
	public Object visitCallExpr(Expr.Call expr) {

		Object callee = evaluate(expr.callee);

		ArrayList<Object> arguments = new ArrayList<>();
		for (Expr argument : expr.arguments) {
			arguments.add(evaluate(argument));
		}

		if (!(callee instanceof NewtCallable)) {
			throw new RuntimeError(expr.parenthesis, "Can only call functions and classes.");
		}

		NewtCallable function = (NewtCallable) callee;

		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.parenthesis,
					"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
		}

		return function.call(this, arguments);
	}

	@Override
	public Void visitFunctionStmt(Function stmt) {
		
		environment.define(stmt.name, new NewtFunction(stmt));
		
		return null;
	}

	@Override
	public Void visitSwitchStmt(Switch stmt) {

		boolean caseFound = false;
		
		//for each case
		for (int i = 0; i < stmt.cases.size(); i++) {
			//get the case
			Stmt.Case caseStmt = stmt.cases.get(i);
			
			boolean validCase = true;
			
			//for each value
			for(int j = 0; j < stmt.controls.size(); j++) {
				//create expression comparing control and test values
				Expr.Binary expr = new Expr.Binary(stmt.controls.get(j), new Token(TokenType.EQUAL_EQUAL, "==", null, 0, 0), caseStmt.tests.get(j));
				
				Boolean bool = (Boolean) evaluate(expr);
				
				if(!bool) {
					validCase = false;
					break;
				}
			}
			
			//execute the case
			if(validCase) {
				caseFound = true;
				visitCaseStmt(caseStmt);

				int flag = (int) globals.get(new Token(null, "$exit_flag", 0, 0, 0));

				/* nothing happens when there is no exit condition */
				if (flag == EXIT_NORMAL) {
					continue;
				}

				/* for a break, the switch breaks and resets the flag */
				if (flag == EXIT_BREAK) {
					globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
					break;
				}

				/* and exit, return, or continue flag is not resolved by the switch */
				if (flag == EXIT_EXIT || flag == EXIT_RETURN || flag == EXIT_CONTINUE) {
					break;
				}
			}
		}
		
		//execute default
		if(!caseFound && stmt.defaultCase != null) {
			visitBlockStmt((Stmt.Block) stmt.defaultCase);
		}

		return null;
	}

	@Override
	public Void visitCaseStmt(Case stmt) {
		//for now, all the work is done in the switch statement
		//In later versions, I may change the parsing such that each case gets a copy of the control value.
		//At that point the work will need to shift to the case.
		visitBlockStmt((Stmt.Block) stmt.block);
		return null;
	}

	@Override
	public Void visitDoStmt(Do stmt) {
		
		do {
			visitBlockStmt((Stmt.Block) stmt.block);

			int flag = (int) globals.get(new Token(null, "$exit_flag", 0, 0, 0));

			/* nothing happens when there is no exit condition */
			if (flag == EXIT_NORMAL) {
				continue;
			}

			/* for a continue, the while continues and resets the flag */
			if (flag == EXIT_CONTINUE) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				continue;
			}

			/* for a break, the while breaks and resets the flag */
			if (flag == EXIT_BREAK) {
				globals.assign(new Token(null, "$exit_flag", 0, 0, 0), EXIT_NORMAL);
				break;
			}

			if (flag == EXIT_EXIT || flag == EXIT_RETURN) {
				break;
			}
		} while ((boolean) evaluate(stmt.condition));
		
		return null;
	}

	@Override
	public Void visitUndecStmt(Undec stmt) {

		for(Expr expr : stmt.variables) {
			environment.undefine(((Expr.Variable)expr).name);
		}
		
		return null;
	}

	@Override
	public Object visitUnaryAssignExpr(UnaryAssign expr) {
		/* make a duplicate with the same lexeme and location for error reporting */
		Token operator = new Token(null, expr.operator.lexeme, expr.operator.literal, expr.operator.line,
				expr.operator.character);

		/* set the new token to have the mathematical operator type */
		switch (expr.operator.type) {
		case MINUS_MINUS:
			operator.type = MINUS;
			break;
		case PLUS_PLUS:
			operator.type = PLUS;
			break;
		default:
			break;
		}

		environment.assign(expr.name, evaluate(new Expr.Binary(new Expr.Variable(expr.name), operator, new Expr.Literal(new Integer(1)))));

		return null;
	}

	@Override
	public Void visitStructStmt(Struct stmt) {
		return null;
	}

	@Override
	public Void visitSculptureStmt(Sculpture stmt) {
		return null;
	}
	
	@Override
	public Object visitSharpExpr(Sharp expr) {
		//sharp expressions should never be visited - they are only placeholders
		return null;
	}

	@Override
	public Void visitMouldStmt(Mould stmt) {
		fillMould(stmt.mould, stmt.placeholders);
		execute(stmt.mould);
		return null;
	}
	
	public void fillMould(Object mouldElement, ArrayList<Placeholder> placeholders){
		//for each of the object's fields (without knowing the class)
		for (Field field : mouldElement.getClass().getDeclaredFields()) {
		    field.setAccessible(true); // You might want to set modifier to public first.
		    
		    try {
				Object value = field.get(mouldElement);
				
				//replace a sharp with the appropriate component
				if(value instanceof Expr.Sharp) {
					
					//evaluate the expression of the sharp to get the name of the placeholder
					String name = (String) evaluate(((Expr.Sharp) value).name);
					
					//find the placeholder and with the given name and replace the field with it's value (an Expr or Stmt)
					int index = placeholders.indexOf(new Placeholder(name, null));
					field.set(mouldElement, placeholders.get(index).value);
				}
				
				else if (value instanceof Stmt.Expression && ((Stmt.Expression) value).expression instanceof Expr.Sharp) {
					Expr.Sharp expression = (Sharp) ((Stmt.Expression) value).expression;
					String name = (String) evaluate(expression.name);
					
					//find the placeholder and with the given name and replace the field with it's value (an Expr or Stmt)
					int index = placeholders.indexOf(new Placeholder(name, null));
					field.set(mouldElement, placeholders.get(index).value);
				}
				
				//only expressions and statements can hold a sharp
				else if (value instanceof Expr || value instanceof Stmt) {
					fillMould(value, placeholders);
				}
				
				else if (value instanceof ArrayList) {
					for(int i = 0; i < ((ArrayList) value).size(); i++) {
						Object element = ((ArrayList) value).get(i);
						
						if(element instanceof Expr.Sharp) {
							//evaluate the expression of the sharp to get the name of the placeholder
							String name = (String) evaluate(((Expr.Sharp) element).name);
							
							//find the placeholder and with the given name and replace the field with it's value (an Expr or Stmt)
							int index = placeholders.indexOf(new Placeholder(name, null));
							
							((ArrayList) value).remove(i);
							((ArrayList) value).add(i, placeholders.get(index).value);
						}
						
						else if (element instanceof Stmt.Expression && ((Stmt.Expression) element).expression instanceof Expr.Sharp) {
							Expr.Sharp expression = (Sharp) ((Stmt.Expression) element).expression;
							String name = (String) evaluate(expression.name);
							
							//find the placeholder and with the given name and replace the field with it's value (an Expr or Stmt)
							int index = placeholders.indexOf(new Placeholder(name, null));
							
							((ArrayList) value).remove(i);
							((ArrayList) value).add(i, placeholders.get(index).value);
						}
						
						else if(element instanceof ArrayList || element instanceof Expr || element instanceof Stmt) {
							fillMould(element, placeholders);
						}
					}
				}
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} 
		}
	}
	
}
