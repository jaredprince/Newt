package interpreter;

public class Token {

	/**
	 * specifies what type of token it is (string, while, bracket, etc.)
	 */
	public TokenType type;
	
	/**
	 * the string value of the token
	 */
	public String lexeme;
	
	/**
	 * the line in the source where the token was found
	 */
	public int line;
	
	/**
	 * the character on the line where the token starts
	 */
	public int character;
	
	/**
	 * Stores the value of tokens which are literals.
	 */
	public Object literal;
	
	public Token(TokenType type, String lexeme, Object literal, int line, int character) {
		super();
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
		this.character = character;
	}
	
	public String toString() {
		return "Type: " + type + " Value: " + (literal == null ? lexeme : literal) + " Location: " + line + "-" + character;
	}
}
