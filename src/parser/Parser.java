package parser;

import static interpreter.TokenType.AND;
import static interpreter.TokenType.ARROW;
import static interpreter.TokenType.BANG;
import static interpreter.TokenType.BANG_EQUAL;
import static interpreter.TokenType.BAR;
import static interpreter.TokenType.BOOL_TYPE;
import static interpreter.TokenType.BREAK;
import static interpreter.TokenType.CARAT;
import static interpreter.TokenType.CARAT_EQUAL;
import static interpreter.TokenType.CASE;
import static interpreter.TokenType.CHARACTER;
import static interpreter.TokenType.CHAR_TYPE;
import static interpreter.TokenType.COLON;
import static interpreter.TokenType.COMMA;
import static interpreter.TokenType.CONTINUE;
import static interpreter.TokenType.DEFAULT;
import static interpreter.TokenType.DO;
import static interpreter.TokenType.DOUBLE;
import static interpreter.TokenType.DOUBLE_TYPE;
import static interpreter.TokenType.ELSE;
import static interpreter.TokenType.EOF;
import static interpreter.TokenType.EQUAL;
import static interpreter.TokenType.EQUAL_EQUAL;
import static interpreter.TokenType.EXIT;
import static interpreter.TokenType.EXPRINT;
import static interpreter.TokenType.FALSE;
import static interpreter.TokenType.FOR;
import static interpreter.TokenType.FUNC;
import static interpreter.TokenType.GREATER;
import static interpreter.TokenType.GREATER_EQUAL;
import static interpreter.TokenType.IDENTIFIER;
import static interpreter.TokenType.IF;
import static interpreter.TokenType.INTEGER;
import static interpreter.TokenType.INT_TYPE;
import static interpreter.TokenType.LEFT_BRACE;
import static interpreter.TokenType.LEFT_BRACKET;
import static interpreter.TokenType.LEFT_PAREN;
import static interpreter.TokenType.LESS;
import static interpreter.TokenType.LESS_EQUAL;
import static interpreter.TokenType.MINUS;
import static interpreter.TokenType.MINUS_EQUAL;
import static interpreter.TokenType.MINUS_MINUS;
import static interpreter.TokenType.MOLD;
import static interpreter.TokenType.NAND;
import static interpreter.TokenType.NOR;
import static interpreter.TokenType.NULL;
import static interpreter.TokenType.OR;
import static interpreter.TokenType.PERCENT;
import static interpreter.TokenType.PERCENT_EQUAL;
import static interpreter.TokenType.PLUS;
import static interpreter.TokenType.PLUS_EQUAL;
import static interpreter.TokenType.PLUS_PLUS;
import static interpreter.TokenType.PRINT;
import static interpreter.TokenType.QUESTION;
import static interpreter.TokenType.RETURN;
import static interpreter.TokenType.RIGHT_BRACE;
import static interpreter.TokenType.RIGHT_BRACKET;
import static interpreter.TokenType.RIGHT_PAREN;
import static interpreter.TokenType.ROOT;
import static interpreter.TokenType.ROOT_EQUAL;
import static interpreter.TokenType.SEMICOLON;
import static interpreter.TokenType.SHARP;
import static interpreter.TokenType.SLASH;
import static interpreter.TokenType.SLASH_EQUAL;
import static interpreter.TokenType.STAR;
import static interpreter.TokenType.STAR_EQUAL;
import static interpreter.TokenType.STRING;
import static interpreter.TokenType.STRING_TYPE;
import static interpreter.TokenType.STRUCT;
import static interpreter.TokenType.SWITCH;
import static interpreter.TokenType.TEMPLATE;
import static interpreter.TokenType.TRUE;
import static interpreter.TokenType.UNDEC;
import static interpreter.TokenType.VAR_TYPE;
import static interpreter.TokenType.WHILE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import interpreter.Expr;
import interpreter.Newt;
import interpreter.Placeholder;
import interpreter.Stmt;
import interpreter.Token;
import interpreter.TokenType;

public class Parser {
	private final List<Token> tokens;
	private int current = 0;
	
	/**
	 * Nessessary to to distinguish proper and improper uses of the #[] expression;
	 */
	private boolean inMold = false;
	
	/**
	 * The molds that have been parsed.
	 */
	private List<Stmt.Struct> molds = new ArrayList<Stmt.Struct>();

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
			
			if (match(STRUCT)) {
				Stmt.Struct stmt = structStatement();
				molds.add(stmt);
				return stmt;
			}
			
			if (match(UNDEC)) {
				return undecStatement();
			}
			
			if (match(SEMICOLON)) {
				error(previous(), "Extra semicolon found.");
			}
			
			//if a statement is expected, parse the sharp expression and bundle it as a statement
			//TODO: Note - expression is used inside the #[]. This means that the argument can either be a string name (ex. #["name"])
			//or an expression which would evaluate to a name (ex. str = "name"; #[str] or #["na" + "me"])
			// This should allow some interesting examples. The name of the # to replace will be evaluated at runtime.
			//This is fun, but it might need to be changed later.
			if (match(SHARP)) {
				if(!inMold) {
					error(previous(), "# is only valid in a mold statement.");
				}
				
				consume(LEFT_BRACKET, "Expect '[' after '#'.");
				Expr expr = expression();
				consume(RIGHT_BRACKET, "Expect ']' after expression.");
				return new Stmt.Expression(new Expr.Sharp(expr));
			}

			if (match(BREAK, CONTINUE, EXIT)) {
				Stmt.Keyword word = new Stmt.Keyword(previous());
				consume(SEMICOLON, "Expect ';' after keyword '" + word.word.lexeme + "'.");
				return word;
			}
			
			if(match(RETURN)) {
				Stmt.Keyword word = new Stmt.Keyword(previous());
				
				if(!match(SEMICOLON)) {
					Expr expression = expression();
					consume(SEMICOLON, "Expect ';' after keyword '" + word.word.lexeme + "'.");
					
					return word;
				}
				
				return word;
			}
			
			//if an identifier, check for user defined structures
			if(peek().type == IDENTIFIER) {
				Token next = peek();
				
				for(Stmt.Struct stmt : molds) {
					//check if the identifier matches the struct
					if(((Token)((Stmt.Template)stmt.template).template.get(0)).equals(next)) {
						return parseStruct(stmt);
					}
				}
			}

		} catch (ParseError error) {
			synchronize();
			return null;
		}

		return expressionStatement();
	}
	
	public Stmt.Mold parseStruct(Stmt.Struct struct){
		
		//fill the template with the user given components
		ArrayList<Placeholder> placeholders = fillTemplate((Stmt.Template) struct.template);

		//create a copy of the mold and add the placeholders from the user given components
		Stmt.Mold moldClone = ((Stmt.Mold) struct.mold).moldClone();
		moldClone = new Stmt.Mold(placeholders, moldClone.mold);
		
		return moldClone;
	}
	
	public ArrayList<Placeholder> fillTemplate(Stmt.Template template){
		String previous = "";
		ArrayList<Placeholder> placeholders = new ArrayList<Placeholder>();
		
		//fill the template
		for(Object obj : template.template) {
			//match token exactly
			if(obj instanceof Token) {
				Token token = (Token)obj;
				consume(token.type, "Expect '" + token.lexeme + "' after " + previous + ".");
				previous = "'" + token.lexeme + "'";
			}
			
			//fill placeholders
			else {
				Placeholder p = (Placeholder)obj;
				String name = p.name;
				String type = (String)p.value;
				
				if(type.equals("expression")) {
					placeholders.add(new Placeholder(name, expression()));
				} else if (type.equals("statement")) {
					placeholders.add(new Placeholder(name, statement()));
				}
			}
		}
		
		return placeholders;
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
			block = block();
		}

		consume(RIGHT_BRACE, "Expect '}' after last case.");

		return new Stmt.Switch(list, cases, block);
	}
	
	private Stmt.Struct structStatement(){
		
		consume(LEFT_BRACE, "Expect '{' after 'struct'.");
		
		Stmt.Template template = templateStatement();
		Stmt.Mold mold = moldStatement();
		
		consume(RIGHT_BRACE, "Expect '}' after 'mold'.");
		
		return new Stmt.Struct(template, mold);
	}
	
	private Stmt.Template templateStatement(){
		consume(TEMPLATE, "Expect 'template' inside struct.");
		consume(LEFT_BRACE, "Expect '{' after 'template'.");
		
		//template consists of tokens and Placeholders (which are <name, type> pairs of Strings)
		ArrayList<Object> template = new ArrayList<Object>();
		
		//TODO: For now, assume all structures start with a keyword and have no internal identifiers
		template.add(consume(IDENTIFIER, "Expect identifier as first argument of template."));

		//loop until the template is closed
		int internalBracesOpen = 0;
		while(!check(RIGHT_BRACE) || internalBracesOpen > 0) {
			
			//token is '<', so we need a <name : type> pair
			if(match(LESS)) {
				Token name = consume(IDENTIFIER, "Expect identifier after '<'.");
				consume(COLON, "Expect ':' after name.");
				Token type = consume(IDENTIFIER, "Expect identifier after ':'.");
				
				//add the placeholder to the template
				template.add(new Placeholder(name.lexeme, type.lexeme));
				
				consume(GREATER, "Expect '>' after type in placeholder.");
			}
			
			//take the delimiter token as given
			else {
				if(match(COMMA, COLON, SEMICOLON, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET)) {
					Token token = previous();
					template.add(token);
					
					if(token.type == LEFT_BRACE) {
						internalBracesOpen++;
					} else if(token.type == RIGHT_BRACE) {
						internalBracesOpen--;
					}
				} else {
					//TODO: better expect
					consume(COMMA, "Expect ':' ';' '(' ')' '[' ']' '{' '}' ',' or '<'");
				}
			}
		}
		
		consume(RIGHT_BRACE, "Expect '}' after template body.");
		
		return new Stmt.Template(template);
	}
	
	private Stmt.Mold moldStatement(){
		inMold = true;
		
		consume(MOLD, "Expect 'mold' after template.");

		//TODO: Right now, the mold can consist of a single statement with no braces. I might want to change this in future.
		
		Stmt.Block mold = block();

		inMold = false;
		return new Stmt.Mold(null, mold);
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

		return new Stmt.Do(condition, block);
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
		
		if (match(SHARP)) {
			if(!inMold) {
				error(previous(), "# is only valid in a mold statement.");
			}
			
			consume(LEFT_BRACKET, "Expect '[' after '#'.");
			Expr expr = expression();
			consume(RIGHT_BRACKET, "Expect ']' after expression.");
			return new Expr.Sharp(expr);
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
			case DO:
			case SWITCH:
			case CASE:
			case UNDEC:
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