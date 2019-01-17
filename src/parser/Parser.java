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
import static interpreter.TokenType.FALSE;
import static interpreter.TokenType.FOR;
import static interpreter.TokenType.FORGE;
import static interpreter.TokenType.FUNC;
import static interpreter.TokenType.GREATER;
import static interpreter.TokenType.GREATER_EQUAL;
import static interpreter.TokenType.IDENTIFIER;
import static interpreter.TokenType.IF;
import static interpreter.TokenType.IMPORT;
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
import static interpreter.TokenType.NAND;
import static interpreter.TokenType.NOR;
import static interpreter.TokenType.NULL;
import static interpreter.TokenType.OR;
import static interpreter.TokenType.PERCENT;
import static interpreter.TokenType.PERCENT_EQUAL;
import static interpreter.TokenType.PLUS;
import static interpreter.TokenType.PLUS_EQUAL;
import static interpreter.TokenType.PLUS_PLUS;
import static interpreter.TokenType.QUESTION;
import static interpreter.TokenType.RETURN;
import static interpreter.TokenType.RIGHT_BRACE;
import static interpreter.TokenType.RIGHT_BRACKET;
import static interpreter.TokenType.RIGHT_PAREN;
import static interpreter.TokenType.ROOT;
import static interpreter.TokenType.ROOT_EQUAL;
import static interpreter.TokenType.SCULPT;
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
import static interpreter.TokenType.TRUE;
import static interpreter.TokenType.UNDEC;
import static interpreter.TokenType.VAR_TYPE;
import static interpreter.TokenType.WHILE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import interpreter.Expr;
import interpreter.Expr.Assign;
import interpreter.Expr.Binary;
import interpreter.Expr.Call;
import interpreter.Expr.Conditional;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Logical;
import interpreter.Expr.Sharp;
import interpreter.Expr.Unary;
import interpreter.Expr.UnaryAssign;
import interpreter.Expr.Variable;
import interpreter.Lexer;
import interpreter.Newt;
import interpreter.Placeholder;
import interpreter.Stmt;
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
import interpreter.Stmt.Return;
import interpreter.Stmt.Sculpture;
import interpreter.Stmt.Struct;
import interpreter.Stmt.Switch;
import interpreter.Stmt.Undec;
import interpreter.Stmt.While;
import interpreter.Token;
import interpreter.TokenType;

public class Parser {
	private List<Token> tokens;
	private int current = 0;
	
	/**
	 * Necessary to to distinguish proper and improper uses of the #[] expression;
	 */
	private boolean inMould = false;
	
	/**
	 * The moulds that have been parsed.
	 */
	private List<Struct> moulds = new ArrayList<Struct>();
	
	private List<Stmt> statements = new ArrayList<>();

	/**
	 * The Parser constructor.
	 * @param tokens the list of tokens to be parsed
	 */
	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Parses the token list into a series of statements.
	 * @return the statement list
	 */
	public List<Stmt> parse() {
		
		while (!isAtEnd()) {
			Stmt statement = statement();
			
			if(statement != null)
				statements.add(statement);
		}
	
		return statements;
	}

	/**
	 * Parses a single statement.
	 * @return the parsed statement
	 */
	public Stmt statement() {

		//attempt to parse a structure
		try {

			if (match(BREAK, CONTINUE, EXIT)) {
				Keyword word = new Keyword(previous());
				consume(SEMICOLON, "Expect ';' after keyword '" + word.word.lexeme + "'.");
				return word;
			}
			
			if(match(RETURN)) {
				
				if(!match(SEMICOLON)) {
					//TODO: return the return value
					Expr expression = expression();
					consume(SEMICOLON, "Expect ';' after return value.");
					
					return new Return(expression);
				}
				
				return new Return(null);
			}
			
			if (match(IMPORT))
				return importStatement();
			
			if (match(UNDEC))
				return undecStatement();
			
			if (match(VAR_TYPE, INT_TYPE, STRING_TYPE, DOUBLE_TYPE, CHAR_TYPE, BOOL_TYPE))
				return declaration();
			
			//if a statement is expected, parse the sharp expression and bundle it as a statement
			if (match(SHARP)) {
				if(!inMould) {
					error(previous(), "# is only valid in a mould statement.");
				}
				
				consume(LEFT_BRACKET, "Expect '[' after '#'.");
				Expr expr = expression();
				consume(RIGHT_BRACKET, "Expect ']' after expression.");
				return new Expression(new Sharp(expr));
			}
			
			if (match(IF))
				return ifStatement();
			
			if (match(WHILE))
				return whileStatement();
			
			if (match(DO))
				return doStatement();
			
			if (match(FOR))
				return forStatement();
			
			if (match(FUNC))
				return functionStatement("function");

			if (match(SWITCH))
				return switchStatement();
			
			if (match(STRUCT)) {
				Struct stmt = structStatement();
				moulds.add(stmt);
				return stmt;
			}
			
			//if an identifier, check for user defined structures
			if(peek().type == IDENTIFIER) {
				Token next = peek();

				for(Struct stmt : moulds) {
					//check if the identifier matches the struct
					if(((Token)((Sculpture)stmt.sculpture).sculpture.get(0)).equals(next)) {
						return userStructStatement(stmt);
					}
				}
			}
			
//			if (match(PRINT))
//				return printStatement();
//
//			if (match(EXPRINT))
//				return exPrintStatement();
			
			if (match(COMMA, COLON, SEMICOLON, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET, ARROW, GREATER, LESS))
				error(previous(), "Expect statement.");

		} catch (ParseError error) {
			synchronize();
			return null;
		}

		return expressionStatement();
	}
	
	private Stmt importStatement() {
		String source = consume(IDENTIFIER, "Expect import source.").lexeme;
		String fileData = null;
		
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(source + ".nwt"));
			fileData = new String(bytes, Charset.defaultCharset());
		} catch (FileNotFoundException exc) {
			System.err.print("No file named '" + source + ".nwt' was found.");
			throw new ParseError();
		} catch (IOException e) {
			System.err.print("Cannot read file named '" + source + ".nwt'.");
			throw new ParseError();
		}
		
		Lexer lexer = new Lexer(fileData);
		
		//substitute the old tokens with those of the imported file
		List<Token> tokens = this.tokens;
		this.tokens = lexer.lex();
		
		int current = this.current;
		this.current = 0;
	
		//parse the new tokens
		parse();
		
		//reset the tokens
		this.tokens = tokens;
		this.current = current;
		
		consume(SEMICOLON, "Expect ';' after import source.");
		
		//import statements are not interpreted; they only give a new source to parse
		return null;
	}
	
	/**
	 * Parses an undec statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt undecStatement() {
	
		consume(LEFT_BRACE, "Expect '{' after undec.");
	
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		
		do {
			variables.add(new Variable(consume(IDENTIFIER, "Expect variable name.")));
		} while (match(COMMA));
		
		Stmt statement = new Undec(variables);
	
		consume(RIGHT_BRACE, "Expect '}' variables in undec.");
	
		return statement;
	}

	/**
	 * Parses a declaration statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Declare declaration() {
		Token type = previous();
		Token name = advance();
		Expr value = null;
	
		if (match(EQUAL)) {
			value = expression();
		}
	
		consume(SEMICOLON, "Expect ';' after value.");
	
		return new Declare(type, name, value);
	}

	/**
	 * Parses a block statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Block block() {
		ArrayList<Stmt> statements = new ArrayList<Stmt>();
	
		if (match(LEFT_BRACE)) {
			while (!match(RIGHT_BRACE)) {
				statements.add(statement());
			}
		} else {
			statements.add(statement());
		}
	
		return new Block(statements);
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
	
		Block ifBody = block();
	
		if (match(ELSE)) {
			return new If(condition, ifBody, block());
		}
	
		return new If(condition, ifBody, null);
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
	
		return new While(condition, block());
	}

	/**
	 * Parses a do statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt doStatement() {
		Block block = block();
		
		consume(WHILE, "Expect 'while' after do body.");
		consume(LEFT_PAREN, "Expect '(' after while.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
	
		return new Do(condition, block);
	}

	/**
	 * Parses a for statement.
	 * Precedence Level:
	 * Examples: 
	 * @return the parsed statement
	 */
	private Stmt forStatement() {
		consume(LEFT_PAREN, "Expect '(' after for.");
		
		Declare declaration = null;
		
		//declaration can be skipped
		if(!match(SEMICOLON)) {
			if(!match(VAR_TYPE, INT_TYPE, STRING_TYPE, DOUBLE_TYPE, CHAR_TYPE, BOOL_TYPE)) {
				error(advance(), "Expect data type. The first statement of a for header must be a declaration.");
			}
			
			declaration = declaration();
		}
		
		Expr condition = expression();
		consume(SEMICOLON, "Expect ';' after condition.");
		
		Expr incrementation = null;
		
		//incrementation can be skipped
		if(!match(RIGHT_PAREN)) {
			incrementation = assignment();
			consume(RIGHT_PAREN, "Expect ')' after incrementation.");
		}
	
		return new For(declaration, condition, incrementation, block());
	}

	/**
	 * Parses a function statement.
	 * 
	 * Example:
	 * [visibility] [static] function functionName(Type name, Type name, ...) { statements }
	 * 
	 * @param kind the type of function to parse
	 * @return the parsed Function
	 */
	private Function functionStatement(String kind) {
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
		
		return new Function(name, types, parameters, block());
	}

	// TODO: What to do about {}? in cases
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
	
		ArrayList<Case> cases = new ArrayList<Case>();
	
		while (match(CASE)) {
			cases.add(caseStatement());
		}
	
		Block block = null;
		
		if (match(DEFAULT)) {
			block = block();
		}
	
		consume(RIGHT_BRACE, "Expect '}' after last case.");
	
		return new Switch(list, cases, block);
	}

	/**
		 * Parses a case statement.
		 * Precedence Level:
		 * Examples: 
		 * @return the parsed statement
		 */
		private Case caseStatement() {
	
			ArrayList<Expr> list;
	
			if (match(LEFT_PAREN)) {
				list = expressionList();
				consume(RIGHT_PAREN, "Expect ')' after expression list.");
			} else {
				list = new ArrayList<Expr>();
				list.add(expression());
			}
	
	//		consume(COLON, "Expect ':' after case header.");
	
			return new Case(list, block());
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

	private Struct structStatement(){
		
		consume(LEFT_BRACE, "Expect '{' after 'struct'.");
		
		Sculpture sculpture = sculptureStatement();
		Mould mould = forgeStatement();
		
		consume(RIGHT_BRACE, "Expect '}' after 'mould'.");
		
		return new Struct(sculpture, mould);
	}

	private Sculpture sculptureStatement(){
		consume(SCULPT, "Expect 'sculpt' inside struct.");
		consume(LEFT_BRACE, "Expect '{' after 'sculpt'.");
		
		//sculpture consists of tokens and Placeholders (which are <name, type> pairs of Strings)
		ArrayList<Object> sculpture = new ArrayList<Object>();
		
		//TODO: For now, assume all structures start with a keyword and have no internal identifiers
		sculpture.add(consume(IDENTIFIER, "Expect identifier as first element of sculpt."));
	
		//loop until the sculpture is closed
		int internalBracesOpen = 0;
		while(!check(RIGHT_BRACE) || internalBracesOpen > 0) {
			
			//token is '<', so we need a <name : type> pair
			if(match(LESS)) {
				Token name = consume(IDENTIFIER, "Expect identifier after '<'.");
				consume(COLON, "Expect ':' after component name.");
				Token type = consume(IDENTIFIER, "Expect identifier after ':'.");
				
				//add the placeholder to the sculpture
				sculpture.add(new Placeholder(name.lexeme, type.lexeme));
				
				consume(GREATER, "Expect '>' after component type.");
			}
			
			//take the delimiter token as given
			else {
				if(match(COMMA, COLON, SEMICOLON, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET, ARROW)) {
					Token token = previous();
					sculpture.add(token);
					
					if(token.type == LEFT_BRACE) {
						internalBracesOpen++;
					} else if(token.type == RIGHT_BRACE) {
						internalBracesOpen--;
					}
				} else {
					consume(COMMA, "Expect ':' ';' '(' ')' '[' ']' '{' '}' ',' '->' or '<'. These are the only valid non-component symbols in a sculpture statement.");
				}
			}
		}
		
		consume(RIGHT_BRACE, "Expect '}' after sculpt body.");
		
		return new Sculpture(sculpture);
	}

	/**
	 * Parses a forge statement
	 * @return the Mould parsed from the forge statement
	 */
	private Mould forgeStatement(){
		inMould = true;
		
		consume(FORGE, "Expect 'forge' after sculpt.");
		
		ArrayList<Stmt> statements = new ArrayList<Stmt>();
		
		consume(LEFT_BRACE, "Expect '{' after 'forge'.");
		
		while (!match(RIGHT_BRACE)) {
			statements.add(statement());
		}
		
		Block mould = new Block(statements);
	
		inMould = false;
		return new Mould(null, mould);
	}

	/**
	 * Parses a structure from a user-defined template.
	 * 
	 * @param struct the structure to be parsed
	 * @return a Mould statement
	 */
	private Mould userStructStatement(Struct struct){
		
		//fill the sculpture with the user given components
		ArrayList<Placeholder> placeholders = fillSculpture((Sculpture) struct.sculpture);

		//create a copy of the mould and add the placeholders from the user given components
		Mould mouldClone = ((Mould) struct.mould).mouldClone();
		mouldClone = new Mould(placeholders, mouldClone.body);
		
		return mouldClone;
	}
	
	/**
	 * Parses a structure into a list of components using the sculpture as a model.
	 * 
	 * @param sculpture the sculpture belonging to the structure
	 * @return the list of Placeholder components
	 */
	private ArrayList<Placeholder> fillSculpture(Sculpture sculpture){
		String previous = "";
		ArrayList<Placeholder> placeholders = new ArrayList<Placeholder>();
		
		//fill the sculpture
		for(Object obj : sculpture.sculpture) {
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

	/**
	 * Parses a print statement.
	 * Precedence Level:
	 * Examples: print 1 + 3
	 * @return the parsed statement
	 */
	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Print(value);
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
		return new ExPrint(value);
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
		return new Expression(expr);
	}

	/**
	 * Parses an expression.
	 * @return the parsed expression
	 */
	private Expr expression() {
		return assignment();
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

			if (expr instanceof Variable) {
				Token name = ((Variable) expr).name;
				return new Assign(name, equals, value);
			}

			error(equals, "Invalid assignment target.");
		}
		
		if (match(PLUS_PLUS, MINUS_MINUS)) {
			Token equals = previous();
			
			Token name = ((Variable) expr).name;
			return new UnaryAssign(name, equals);
		}

		return expr;
	}

	/**
	 * Parses a conditional expression.
	 * Precedence Level:
	 * Example: true ? 1 : 0
	 * @return the parsed expression
	 */
	private Expr conditional() {
		Expr expr = logicalOR();

		if (match(QUESTION)) {
			Token operator = previous();
			Expr first = expression();
			consume(COLON, "Expect ':' after expression.");

			expr = new Conditional(expr, operator, first, expression());
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
			expr = new Logical(expr, operator, right);
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
			expr = new Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a nor expression.
	 * Precedence Level:
	 * Example: true nor false
	 * @return the parsed expression
	 */
	private Expr logicalNOR() {
		Expr expr = logicalAND();

		while (match(NOR)) {
			Token operator = previous();
			Expr right = logicalAND();
			expr = new Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an and expression.
	 * Precedence Level:
	 * Example: true && false
	 * @return the parsed expression
	 */
	private Expr logicalAND() {
		Expr expr = logicalImplies();

		while (match(AND)) {
			Token operator = previous();
			Expr right = logicalImplies();
			expr = new Logical(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an implication expression.
	 * Precedence Level:
	 * Example: true -> false
	 * @return the parsed expression
	 */
	private Expr logicalImplies() {
		Expr expr = equality();

		while (match(ARROW)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Logical(expr, operator, right);
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
			expr = new Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a comparison expression.
	 * Precedence Level:
	 * Examples: >, <, <=, >=
	 * @return the parsed expression
	 */
	private Expr comparison() {
		Expr expr = addition();

		while (match(GREATER, LESS, GREATER_EQUAL, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an addition expression.
	 * Precedence Level:
	 * Examples: +, -
	 * @return the parsed expression
	 */
	private Expr addition() {
		Expr expr = multiplication();

		while (match(PLUS, MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a multiplication expression.
	 * Precedence Level:
	 * Examples: *, /, %
	 * @return the parsed expression
	 */
	private Expr multiplication() {
		Expr expr = exponentiation();

		while (match(STAR, SLASH, PERCENT)) {
			Token operator = previous();
			Expr right = exponentiation();
			expr = new Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses an exponential expression.
	 * Precedence Level:
	 * Examples: ^, root
	 * @return the parsed expression
	 */
	private Expr exponentiation() {
		Expr expr = unary();

		while (match(CARAT, ROOT)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * Parses a unary expression.
	 * Precedence Level:
	 * Examples: !, -
	 * @return the parsed expression
	 */
	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Unary(operator, right);
		}

		return call();
	}

	private Expr call() {
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

		return new Call(callee, paren, arguments);
	}

	/**
	 * Parses a primary expression.
	 * 
	 * @return the parsed expression
	 */
	private Expr primary() {
		if (match(IDENTIFIER))
			return new Variable(previous());
		if (match(FALSE))
			return new Literal(false);
		if (match(TRUE))
			return new Literal(true);
		if (match(NULL))
			return new Literal(null);
		if (match(INTEGER, DOUBLE, STRING, CHARACTER))
			return new Literal(previous().literal);

		/* a '(' must be paired with ')' and enclose an expression */
		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Grouping(previous(), expr);
		}

		/* a '|' must be paired with another and enclose an expression */
		if (match(BAR)) {
			Expr expr = expression();
			consume(BAR, "Expect '|' after expression.");
			return new Grouping(previous(), expr);
		}
		
		if (match(SHARP)) {
			if(!inMould) {
				error(previous(), "# is only valid in a mould statement.");
			}
			
			consume(LEFT_BRACKET, "Expect '[' after '#'.");
			Expr expr = expression();
			consume(RIGHT_BRACKET, "Expect ']' after expression.");
			return new Sharp(expr);
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