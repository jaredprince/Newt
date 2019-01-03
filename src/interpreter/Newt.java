package interpreter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import parser.Parser;

public class Newt {

	private static final Interpreter interpreter = new Interpreter();

	/**
	 * Alerts the interpreter if an error was found in the code.
	 */
	public static boolean hadError = false;

	/**
	 * Alerts the interpreter if an error was found during runtime.
	 */
	static boolean hadRuntimeError = false;

	/**
	 * This is the entry point for the Newt Interpreter.
	 * 
	 * @param args
	 *            the only argument accepted is a string with a source file
	 * @throws IOException
	 *             for problems reading the source file
	 */
	public static void main(String[] args) throws IOException {

		if (args.length > 1) {
			System.out.println("Usage: newt [script]");
			System.exit(1);
		} else if (args.length == 1) {
			/* runs the given source file */
			runFile(args[0]);

			if (hadError) {
				System.exit(10);
			}

			if (hadRuntimeError)
				System.exit(11);
		} else {
			/* opens a prompt to enter commands dynamically */
			runPrompt();
		}
	}

	/**
	 * Reads and executes a Newt program from a source file.
	 * 
	 * @param path
	 *            the path of the file to read
	 * @throws IOException
	 *             for problems reading the source file
	 */
	private static void runFile(String path) throws IOException {

		/* all Newt file have a .nwt file type */
		if (!path.endsWith(".nwt"))
			path += ".nwt";

		try {
			byte[] bytes = Files.readAllBytes(Paths.get(path));
			run(new String(bytes, Charset.defaultCharset()));
		} catch (FileNotFoundException exc) {
			System.err.print("No file named '" + path + "' was found.");
		}
	}

	/**
	 * Prompts the user to enter Newt commands in the console.
	 * 
	 * @throws IOException
	 *             for problems reading from the console
	 */
	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		/* loops indefinitely to receive and run user commands */
		while (true) {
			System.out.print("> ");
			run(reader.readLine());
			hadError = false;
		}
	}

	/**
	 * This method runs the given Newt commands.
	 * 
	 * @param source
	 *            the commands to be run
	 */
	private static void run(String source) {
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.lex();

		// For now, just print the tokens.
//		for (Token token : tokens) {
//			System.out.println(token);
//		}

		Parser parser = new Parser(tokens);

		while (!parser.isAtEnd()) {
			
			List<Stmt> statements = parser.parse();

			if (hadError) {
				return;
			}

			interpreter.interpret(statements);
		}
	}

	/**
	 * Generates an error.
	 * 
	 * @param line
	 *            the line on which the error occurred
	 * @param message
	 *            the error message to be shown to the user
	 */
	static void error(int line, String message) {
		report(line, "", message);
	}

	/**
	 * Generates an error.
	 * 
	 * @param line
	 *            the line on which the error occurred
	 * @param character
	 *            the character at which the token which produced the error began
	 * @param message
	 *            the error message to be shown to the user
	 */
	static void error(int line, int character, String message) {
		report(line, "", message);
	}

	/**
	 * Reports an error to the user.
	 * 
	 * @param line
	 *            the line on which the error occurred
	 * @param where
	 *            the token which produced the error
	 * @param message
	 *            the error message to be shown to the user
	 */
	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	/**
	 * Produces an error message.
	 * @param token the token at which the error occurred
	 * @param message the message to display
	 */
	public static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}

	/**
	 * Sends an error message to the user.
	 * @param error the error produced
	 */
	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}
}
