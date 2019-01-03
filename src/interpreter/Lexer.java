package interpreter;

import static interpreter.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

	/**
	 * A list of all Newt keywords.
	 */
	private static final Map<String, TokenType> keywords;

	/**
	 * Fill the keyword list.
	 */
	static {
		keywords = new HashMap<>();
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("if", IF);
		keywords.put("print", PRINT);
		keywords.put("exprint", EXPRINT);
		keywords.put("construct", CONSTRUCT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR_TYPE);
		keywords.put("while", WHILE);
		keywords.put("try", TRY);
		keywords.put("continue", CONTINUE);
		keywords.put("break", BREAK);
		keywords.put("catch", CATCH);
		keywords.put("finally", FINALLY);
		keywords.put("switch", SWITCH);
		keywords.put("case", CASE);
		keywords.put("default", DEFAULT);
		keywords.put("do", DO);
		keywords.put("int", INT_TYPE);
		keywords.put("double", DOUBLE_TYPE);
		keywords.put("string", STRING_TYPE);
		keywords.put("char", CHAR_TYPE);
		keywords.put("bool", BOOL_TYPE);
		keywords.put("var", VAR_TYPE);
		keywords.put("func", FUNC);
		keywords.put("exit", EXIT);
		keywords.put("null", NULL);
	}

	/**
	 * The source from which to read.
	 */
	private final String source;

	/**
	 * The tokens lexed from the source.
	 */
	private final List<Token> tokens = new ArrayList<>();

	/**
	 * The current line being lexed.
	 */
	public int line = 0;

	/**
	 * The current character being lexed.
	 */
	public int character = 0;

	/**
	 * The current total offset of the lexer from the source start.
	 */
	public int offset = 0;

	/**
	 * The offset at which the token starts.
	 */
	public int start = 0;

	/**
	 * The offset at which the token ends.
	 */
	public int current = 0;

	public Lexer(String source) {
		this.source = source;
	}

	/**
	 * Lexes the source into a series of tokens.
	 * 
	 * @return the list of tokens lexed
	 */
	public List<Token> lex() {

		while (!sourceEmpty()) {
			start = current;
			lexToken();
		}

		/* EOF token is helpful to the parser */
		tokens.add(new Token(EOF, "", null, line, character));
		return tokens;
	}

	/**
	 * Lexes a single token from the source.
	 */
	private void lexToken() {
		char c = advance();
		switch (c) {
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			if (Character.isDigit(peek())) {
				lexNumber();
			} else {
				addToken(DOT);
			}
			break;
		case '|':
			addToken(match('|') ? OR : BAR);
			break;
		case '&':
			if (match('&')) {
				addToken(AND);
			} else {
				Newt.error(line, "Unexpected character.");
			}
			break;
		case '-':
			addToken(match('=') ? MINUS_EQUAL : match('-') ? MINUS_MINUS : match('>') ? ARROW : MINUS);
			break;
		case '+':
			addToken(match('=') ? PLUS_EQUAL : match('+') ? PLUS_PLUS : PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(match('=') ? STAR_EQUAL : STAR);
			break;
		case '%':
			addToken(match('=') ? PERCENT_EQUAL : PERCENT);
			break;
		case '^':
			addToken(match('=') ? CARAT_EQUAL : CARAT);break;
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case ':':
			addToken(COLON);
			break;
		case '?':
			//TODO: Possible conditional formats - ?(c1:x, c2:y, z) or ?: (c1, c2), (x, y, z) or ?: a:x, b:y, z			
			addToken(QUESTION); break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		case '/':
			if (match('/')) {
				// A comment goes until the end of the line.
				while (peek() != '\n' && !sourceEmpty())
					advance();
			} else if (match('*')) {
				/* a block comment */
				boolean starFound = false;

				/* continue until the comment ends or the source does */
				while (!sourceEmpty()) {
					/** if a star was found, close the comment with a / or reset starFound */
					if (starFound && match('/')) {
						break;
					} else {
						starFound = false;
					}

					/* if there is a star, set starTound true */
					if (match('*')) {
						starFound = true;
					} else {
						advance();
					}
				}
			} else {
				addToken(SLASH);
			}
			break;
		case ' ':
		case '\r':
		case '\t':
			// Ignore whitespace.
			break;

		case '\n':
			character = 0;
			line++;
			break;

		case '"':
			lexString();
			break;
		case '\'':
			lexCharacter();
			break;
		case '~':
			if (match('&')) {
				addToken(NAND);
			} else if (match('|')) {
				addToken(NOR);
			} else if (match('^')) {
				addToken(ROOT);
			} else {
				Newt.error(line, "Unexpected character.");
			}
			break;
		default:
			if (Character.isDigit(c)) {
				lexNumber();
			} else if (Character.isAlphabetic(c) || c == '_') {
				lexIdentifier();
			} else {
				Newt.error(line, "Unexpected character.");
			}
			break;
		}
	}

	/**
	 * Lexes an identifier token.
	 */
	public void lexIdentifier() {

		while ((peek() == '_' || Character.isAlphabetic(peek())) && !sourceEmpty()) {
			advance();
		}

		String value = source.substring(start, current);

		/*
		 * identifiers can't have successive underscores and can't start with an
		 * underscore followed by a digit
		 */
		if (value.contains("__") || (value.charAt(0) == '_' && Character.isDigit(value.charAt(1)))) {
			Newt.error(line, "Malformed Identifier.");
		}
		
		TokenType type = keywords.get(value);           
	    if (type == null) type = IDENTIFIER;

		addToken(type);
	}

	/**
	 * Lexes a character token.
	 */
	private void lexCharacter() {
		Character value = null;

		/* looks for escaped characters */
		if (match('\\')) {
			if (match('\'')) {
				value = '\'';
			} else if (match('\\')) {
				value = '\\';
			} else if (match('n')) {
				value = '\n';
			} else if (match('t')) {
				value = '\t';
			} else {
				Newt.error(line, "Invalid Escaped Character.");
			}
		}

		else {
			advance();
			value = source.charAt(current - 1);
		}

		/* causes an error if there is no immediate closing ' */
		if (!match('\'')) {
			Newt.error(line, "Unterminated Character.");
		}

		addToken(CHARACTER, value);
	}

	/**
	 * Lexes a number token (int or double).
	 */
	private void lexNumber() {

		boolean decimalFound = false;

		/* numbers can have a single decimal */
		while (((!decimalFound && peek() == '.') || Character.isDigit((peek()))) && !sourceEmpty()) {
			if (peek() == '.') {
				decimalFound = true;
			}

			advance();
		}

		String value = source.substring(start, current);

		/* numbers cannot have leading zeros */
		if (value.charAt(0) == 0 && value.length() > 1 && value.charAt(1) != '.') {
			Newt.error(line, "Malformed Number.");
			return;
		}

		if (decimalFound || value.charAt(0) == '.') {
			addToken(DOUBLE, Double.parseDouble(value));
		} else {
			addToken(INTEGER, Integer.parseInt(value));
		}
	}

	/**
	 * Lexes a string token.
	 */
	private void lexString() {
		boolean foundSlash = false;

		// continue while there is no escaped "
		while ((peek() != '"' || foundSlash) && !sourceEmpty()) {

			if (peek() == '\\') {
				// found a slash only if the last character wasn't a slash
				if (!foundSlash)
					foundSlash = true;
				else
					foundSlash = false;
			} else if (peek() == '\n') {
				line++;
				foundSlash = false;
			} else {
				foundSlash = false;
			}

			advance();
		}

		// Unterminated string.
		if (sourceEmpty()) {
			Newt.error(line, "Unterminated string.");
			return;
		}

		// The closing ".
		advance();

		// Trim the surrounding quotes.
		String value = source.substring(start + 1, current - 1);
		value = value.replace("\\n", "\n");
		value = value.replace("\\t", "\t");
		value = value.replace("\\\"", "\"");
		value = value.replace("\\\\", "\\");

		addToken(STRING, value);
	}

	/**
	 * Advances and consumes a character if it matches the given character.
	 * 
	 * @param expected
	 *            the character to be matched
	 * @return true if the characters matched, false otherwise
	 */
	private boolean match(char expected) {
		if (sourceEmpty())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	/**
	 * Peeks at the next character in line.
	 * 
	 * @return the character
	 */
	private char peek() {
		if (sourceEmpty())
			return '\0';
		return source.charAt(current);
	}

	/**
	 * Determines if the lexer is at the end of the source.
	 * 
	 * @return true if the lexer is at the end of source, false otherwise
	 */
	public boolean sourceEmpty() {
		return source.length() <= current;
	}

	/**
	 * Advances one character through the source and returns it.
	 * 
	 * @return the current character
	 */
	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	/**
	 * Adds a token to the list.
	 * 
	 * @param type
	 *            the type of the token to add
	 */
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	/**
	 * Adds a literal token to the list.
	 * 
	 * @param type
	 *            the type of the token
	 * @param literal
	 *            the literal value of the token
	 */
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line, character));
	}
}