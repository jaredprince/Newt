package interpreter;

public enum TokenType {

	// Single-character tokens.
	LEFT_BRACKET, RIGHT_BRACKET, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON, 
	MINUS, PLUS, SLASH, STAR, CARAT, PERCENT, BAR, ROOT, SHARP,
	
	MINUS_EQUAL, MINUS_MINUS, PLUS_EQUAL, PLUS_PLUS, SLASH_EQUAL, STAR_EQUAL, PERCENT_EQUAL, CARAT_EQUAL, ROOT_EQUAL,

	// One or two character tokens.
	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, OR, AND, ARROW, NOR, NAND,
	QUESTION, COLON,

	// Literals.
	IDENTIFIER, STRING, INTEGER, DOUBLE, CHARACTER, NULL,

	// Keywords.
	CLASS, ELSE, FALSE, FOR, IF, SUPER, THIS, TRUE, WHILE, DO, TRY, CATCH, CASE, SWITCH, FINALLY,
	DEFAULT, CONSTRUCT, FUNC, UNDEC, STATIC, STRUCT, SCULPT, FORGE,
	
	EXIT, BREAK, PRINT, RETURN, EXPRINT, CONTINUE, IMPORT,
	
	//primitive types
	INT_TYPE, DOUBLE_TYPE, STRING_TYPE, CHAR_TYPE, BOOL_TYPE, VAR_TYPE,

	EOF
}
