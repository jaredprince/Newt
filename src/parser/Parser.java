package parser;

import static interpreter.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import interpreter.Expr;
import interpreter.Newt;
import interpreter.Stmt;
import interpreter.Token;
import interpreter.TokenType;

public class Parser {
	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(statement());
		}

		return statements;
	}

	public Stmt statement() {

		try {

			if (match(FUNC)) {
				return function("function");
			}

			if (match(VAR_TYPE, INT_TYPE, STRING_TYPE, DOUBLE_TYPE, CHAR_TYPE, BOOL_TYPE))
				return declaration();

			if (match(PRINT))
				return printStatement();

			if (match(EXPRINT))
				return exPrintStatement();

			if (match(WHILE))
				return whileStatement();
			
			if (match(DO))
				return doStatement();

			if (match(SWITCH))
				return switchStatement();

			if (match(FOR))
				return forStatement();

			if (match(IF)) {
				return ifStatement();
			}
			
			if (match(UNDEC)) {
				return undecStatement();
			}

			if (match(BREAK, CONTINUE, EXIT, RETURN)) {
				Stmt.Keyword word = new Stmt.Keyword(previous());
				consume(SEMICOLON, "Expect ';' after keyword '" + word.word.lexeme + "'.");
				return word;
			}

		} catch (ParseError error) {
			synchronize();
			return null;
		}

		return expressionStatement();
	}

	private Stmt.Function function(String kind) {
		Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
		consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		ArrayList<Token> types = new ArrayList<>();
		ArrayList<Token> parameters = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size() >= 32) {
					error(peek(), "Cannot have more than 32 parameters.");
				}

				if (match(INT_TYPE, DOUBLE_TYPE, STRING_TYPE, CHAR_TYPE, BOOL_TYPE, VAR_TYPE, IDENTIFIER)) {
					types.add(previous());
				}

				parameters.add(consume(IDENTIFIER, "Expect parameter name."));
			} while (match(COMMA));
		}

		consume(RIGHT_PAREN, "Expect ')' after parameters.");
		
		return new Stmt.Function(name, types, parameters, block());
	}

	// TODO: Switches should have automatic breaks between blocks
	// Idea: special keyword for nonbreaking case - ?
	// What to do about {}? in cases
	/**
	 * Parses a switch statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt switchStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'switch'.");
		ArrayList<Expr> list = expressionList();
		consume(RIGHT_PAREN, "Expect ')' after expression list.");

		consume(LEFT_BRACE, "Expect '{' after switch header.");

		ArrayList<Stmt.Case> cases = new ArrayList<Stmt.Case>();

		while (match(CASE)) {
			cases.add(caseStatement());
		}

		Stmt.Block block = null;
		
		if (match(DEFAULT)) {
//			consume(COLON, "Expect ':' after 'default'.");
			block = block();
		}

		consume(RIGHT_BRACE, "Expect '}' after last case.");

		return new Stmt.Switch(list, cases, block);
	}

	/**
	 * Parses a case statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt.Case caseStatement() {

		ArrayList<Expr> list;

		if (match(LEFT_PAREN)) {
			list = expressionList();
			consume(RIGHT_PAREN, "Expect ')' after expression list.");
		} else {
			list = new ArrayList<Expr>();
			list.add(expression());
		}

//		consume(COLON, "Expect ':' after case header.");

		return new Stmt.Case(list, block());
	}

	/**
	 * Parses a list of expressions.
	 * Precedence Level:
	 * Examples: 
	 * @return an ArrayList containing the expressions
	 */
	private ArrayList<Expr> expressionList() {
		ArrayList<Expr> list = new ArrayList<Expr>();

		list.add(expression());

		while (match(COMMA)) {
			list.add(expression());
		}

		return list;
	}

	/**
	 * Parses an assignment expression.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Expr assignment() {
		Expr expr = conditional();

		if (match(EQUAL, MINUS_EQUAL, PLUS_EQUAL, SLASH_EQUAL, STAR_EQUAL, PERCENT_EQUAL, CARAT_EQUAL, ROOT_EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable) expr).name;
				return new Expr.Assign(name, equals, value);
			}

			error(equals, "Invalid assignment target.");
		}
		
		if (match(PLUS_PLUS, MINUS_MINUS)) {
			Token equals = previous();
			
			Token name = ((Expr.Variable) expr).name;
			return new Expr.UnaryAssign(name, equals);
		}

		return expr;
	}

	/**
	 * Parses a declaration statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt declaration() {
		Token type = previous();
		Token name = advance();
		Expr value = null;

		if (match(EQUAL)) {
			value = expression();
		}

		consume(SEMICOLON, "Expect ';' after value.");

		return new Stmt.Declare(type, name, value);
	}

	/**
	 * Parses a for statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt forStatement() {
		//TODO: allow missing declarations or incrementors
		consume(LEFT_PAREN, "Expect '(' after for.");
		advance(); //necessary because declaration assumes the type has already been read
		Stmt declaration = declaration();
		Expr condition = expression();
		consume(SEMICOLON, "Expect ';' after condition.");
		Expr incrementation = assignment();
		consume(RIGHT_PAREN, "Expect ')' after incrementation.");

		return new Stmt.For(declaration, condition, incrementation, block());
	}

	/**
	 * Parses an if statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt ifStatement() {

		consume(LEFT_PAREN, "Expect '(' after if.");

		Expr condition = expression();

		consume(RIGHT_PAREN, "Expect ')' after condition.");

		Stmt ifBlock = block();

		if (match(ELSE)) {
			return new Stmt.If(condition, ifBlock, block());
		}

		return new Stmt.If(condition, ifBlock, null);
	}
	
	/**
	 * Parses an undec statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt undecStatement() {

		consume(LEFT_BRACE, "Expect '{' after undec.");

		ArrayList<Expr> variables = new ArrayList<Expr>();
		
		
		do {
			variables.add(new Expr.Variable(consume(IDENTIFIER, "Expect variable name.")));
		} while (match(COMMA));
		
		Stmt statement = new Stmt.Undec(variables);

		consume(RIGHT_BRACE, "Expect '}' variables in undec.");

		return statement;
	}

	/**
	 * Parses a block statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt.Block block() {
		ArrayList<Stmt> statements = new ArrayList<Stmt>();

		if (match(LEFT_BRACE)) {
			while (!match(RIGHT_BRACE)) {
				statements.add(statement());
			}
		} else {
			statements.add(statement());
		}

		return new Stmt.Block(statements);
	}

	/**
	 * Parses a while statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt whileStatement() {
		consume(LEFT_PAREN, "Expect '(' after while.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");

		return new Stmt.While(condition, block());
	}
	
	/**
	 * Parses a do statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt doStatement() {
		Stmt.Block block = block();
		
		consume(WHILE, "Expect 'while' after do body.");
		consume(LEFT_PAREN, "Expect '(' after while.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");

		return new Stmt.Do(condition, block());
	}

	/**
	 * Parses a print statement.
	 * Precedence Level:
	 * Examples: print 1 + 3
	 * @return the parsed statement
	 */
	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	/**
	 * Parses an exprint statement.
	 * Precedence Level:
	 * Examples: exprint 1 + 3
	 * @return the parsed statement
	 */
	private Stmt exPrintStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.ExPrint(value);
	}

	/**
	 * Parses an expression statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}

	/**
	 * Parses and expression.
	 * @return the parsed expression
	 */
	public Expr expression() {
		return assignment();
	}

	/**
	 * Parses a conditional expression.
	 * Precedence Level:
	 * Examples: ? :
	 * @return the parsed expression
	 */
	private Expr conditional() {
		Expr expr = logicalOR();

		if (match(QUESTION)) {
			Token operator = previous();
			Expr first = expression();
			consume(COLON, "Expect ':' after expression.");

			expr = new Expr.Conditional(expr, operator, first, expression());
		}

		return expr;
	}

	/**
	 * Parses a or expression.
	 * Precedence Level:
	 * Examples: ||
	 * @return the parsed expression
	 */
	private Expr logicalOR() {
		Expr expr = logicalNAND();

		while (match(OR)) {
			Token operator = previous();
			Expr right = logicalNAND();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a nand expression.
	 * Precedence Level:
	 * Examples: nand
	 * @return the parsed expression
	 */
	private Expr logicalNAND() {
		Expr expr = logicalNOR();

		while (match(NAND)) {
			Token operator = previous();
			Expr right = logicalNOR();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a nor expression.
	 * Precedence Level:
	 * Examples: nor
	 * @return the parsed expression
	 */
	private Expr logicalNOR() {
		Expr expr = logicalAND();

		while (match(NOR)) {
			Token operator = previous();
			Expr right = logicalAND();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an and expression.
	 * Precedence Level:
	 * Examples: &&
	 * @return the parsed expression
	 */
	private Expr logicalAND() {
		Expr expr = logicalImplies();

		while (match(AND)) {
			Token operator = previous();
			Expr right = logicalImplies();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an implication expression.
	 * Precedence Level:
	 * Examples: ->
	 * @return the parsed expression
	 */
	private Expr logicalImplies() {
		Expr expr = equality();

		while (match(ARROW)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an equality expression.
	 * Precedence Level:
	 * Examples: !=, ==
	 * @return the parsed expression
	 */
	private Expr equality() {
		Expr expr = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a comparison expression.
	 * Precedence Level:
	 * Examples: >, <, <=, >=
	 * @return the parsed expression
	 */
	public Expr comparison() {
		Expr expr = addition();

		while (match(GREATER, LESS, GREATER_EQUAL, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an addition expression.
	 * Precedence Level:
	 * Examples: +, -
	 * @return the parsed expression
	 */
	public Expr addition() {
		Expr expr = multiplication();

		while (match(PLUS, MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a multiplication expression.
	 * Precedence Level:
	 * Examples: *, /, %
	 * @return the parsed expression
	 */
	public Expr multiplication() {
		Expr expr = exponentiation();

		while (match(STAR, SLASH, PERCENT)) {
			Token operator = previous();
			Expr right = exponentiation();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an exponential expression.
	 * Precedence Level:
	 * Examples: ^, root
	 * @return the parsed expression
	 */
	public Expr exponentiation() {
		Expr expr = unary();

		while (match(CARAT, ROOT)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a unary expression.
	 * Precedence Level:
	 * Examples: !, -
	 * @return the parsed expression
	 */
	public Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return call();
	}

	public Expr call() {
		Expr expr = primary();

		while (true) {
			if (match(LEFT_PAREN)) {
				expr = finishCall(expr);
			} else {
				break;
			}
		}

		return expr;
	}

	private Expr finishCall(Expr callee) {
		ArrayList<Expr> arguments = new ArrayList<>();

		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size() >= 32) {
					error(peek(), "Cannot have more than 32 arguments.");
				}

				arguments.add(expression());
			} while (match(COMMA));
		}

		Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

		return new Expr.Call(callee, paren, arguments);
	}

	/**
	 * Parses a primary expression.
	 * 
	 * @return the parsed expression
	 */
	public Expr primary() {
		if (match(IDENTIFIER))
			return new Expr.Variable(previous());
		if (match(FALSE))
			return new Expr.Literal(false);
		if (match(TRUE))
			return new Expr.Literal(true);
		if (match(NULL))
			return new Expr.Literal(null);
		if (match(INTEGER, DOUBLE, STRING, CHARACTER))
			return new Expr.Literal(previous().literal);

		/* a '(' must be paired with ')' and enclose an expression */
		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(previous(), expr);
		}

		/* a '|' must be paired with another and enclose an expression */
		if (match(BAR)) {
			Expr expr = expression();
			consume(BAR, "Expect '|' after expression.");
			return new Expr.Grouping(previous(), expr);
		}

		/* throw an error if no primary expression was found */
		throw error(peek(), "Expect expression.");
	}

	/**
	 * Checks if the next token is of the given types and advances if so.
	 * 
	 * @param types
	 *            the types to match
	 * @return true if the token was matched, false otherwise
	 */
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks of the next token is of the given type.
	 * 
	 * @param type
	 *            the type of token for which to check
	 * @return true of the next token matches, false otherwise
	 */
	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().type == type;
	}

	/**
	 * Advances to the next token.
	 * 
	 * @return the current token
	 */
	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	/**
	 * @return true if the next token is an EOF, false otherwise
	 */
	public boolean isAtEnd() {
		return peek().type == EOF;
	}

	/**
	 * @return the next token
	 */
	private Token peek() {
		return tokens.get(current);
	}

	/**
	 * @return the previous token
	 */
	private Token previous() {
		return tokens.get(current - 1);
	}

	/**
	 * Consumes and returns the next token, if it is the appropriate type. Throws an
	 * error otherwise.
	 * 
	 * @param type
	 *            the type of token to consume
	 * @param message
	 *            the message to be displayed in the error
	 * @return the consumed token
	 */
	private Token consume(TokenType type, String message) {
		if (check(type))
			return advance();

		throw error(peek(), message);
	}

	/**
	 * Shows an error.
	 * 
	 * @param token
	 *            the token at which the error occurred
	 * @param message
	 *            the message to display
	 * @return a ParseError
	 */
	private ParseError error(Token token, String message) {
		Newt.error(token, message);
		return new ParseError();
	}

	/**
	 * Advances through the code to a point that begins a new statement. This is
	 * used to exit an unknown state when an error is found.
	 */
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			/* semicolons end statements */
			if (previous().type == SEMICOLON)
				return;

			switch (peek().type) {
			case CLASS:
			case FUNC:
			case FOR:
			case IF:
			case WHILE:
			case PRINT:
			case EXPRINT:
			case BREAK:
			case EXIT:
			case RETURN:
				return;
			default:
			}

			advance();
		}
	}

	/**
	 * Resets the current token counter to 0, allowing the parser to parse from the
	 * beginning again.
	 */
	public void reset() {
		current = 0;
	}
}