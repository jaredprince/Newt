import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Parser {
	
	//TODO: Add arrays
	
	//TODO: Add sets
	
	//TODO: compare references of objects with "==="
	
	//TODO: add classes
	
	//TODO: try/catch structures
	
	static Lexer lex;
	static ASTNode root;

	static LinkedList<String[]> operators;
	
	static Environment environment = new Environment();

	public static void main(String[] args) throws IOException {
		lex = new Lexer(new File("Parser Test.txt"));
		
		//a native method
		environment.define(new Token("native", Token.TYPE), new Token("test", Token.STRUCTURE), new TypedObject("native", new Callable(){
			
			@Override
			public TypedObject call(Parser parser, List<TypedObject> arguments) {
				System.out.println("this is test one");
				return new TypedObject("string", "this is test two");
			}

			@Override
			public int arity() {
				return 0;
			}
			
		}));
		
//		while(lex.hasNextToken()){
//			 System.out.println(lex.consume());
//		}
		
		parseProgram().visitNode();
		 
//		System.out.println(r.toString(0));
	}

	//TODO: replace multiple operator methods with a list of string[]
	public static void operatorsPrecedence() {
		String[][] ops = { { "==", "!=" }, { ">=", "<=", "<", ">" }, { "+", "-" }, { "*", "/", "%" }, { "^" },
				{ "-", "!" } };

		operators = new LinkedList<String[]>();
		for (int i = 0; i < ops.length; i++) {
			operators.add(ops[i]);
		}
	}
	
	/**
	 * Parses the entire program.
	 * @return An ASTNode node representing the program.
	 */
	public static NaryAST parseProgram() {
		NaryAST node = new NaryAST(new Token("program", Token.GROUPING));
		
		//parse statements until there is no next token
		while (lex.hasNextToken() && !lex.nextTypeIs(Token.EOF)) {
			ASTNode subnode = parseStatement();
			
			//functions get declared immediately so they can be used
			if(subnode.token.value.equals("func")){
				subnode.visitNode();
			} else {
				node.addNode(subnode);
			}
		}

		return node;
	}

	/**
	 * Parses a single statement (declaration, assignment, structure, or keyword statement).
	 * @return An ASTNode representing the statement.
	 */
	public static ASTNode parseStatement() {

		//TODO: Allow declaring functions without initializing (func c;) and allow (func c = otherFuncName;)
		
		//parse keyword statements
		if (lex.nextTypeIs(Token.STATEMENT)) {
			if(lex.nextValueIs("return")){
				UnaryAST node = new UnaryAST(lex.consume());
				
				if(!lex.nextValueIs(";")){
					node.child = parseExpression();
				} else {
					node.child = new ASTNode(new Token("void", Token.BLANK));
				}
				
				expect(";");
				
				return node;
			}
			
			else {
				ASTNode node = new ASTNode(lex.consume());	
				expect(";");
				
				return node;
			}
		}

		//parse structures
		else if (lex.nextTypeIs(Token.STRUCTURE)) {
			return parseStructure();
		}

		//parse declarations
		else if (lex.nextTypeIs(Token.TYPE)) {
			ASTNode node = parseDeclaration();
			expect(";");
			return node;
		}

		//parse assignments
		else if (lex.nextTypeIs(Token.IDENTIFIER)) {
			ASTNode node = parseFunctionCallOrAssignment();
			expect(";");
			return node;
		}
		
		else if (lex.nextValueIs("{")){
			return parseBlock();
		}

		//give an error if no statement was found
		error("keyword or identifier");

		return null;
	}

	/**
	 * Parses the appropriate structure.
	 * @return An ASTNode representing the structure.
	 */
	public static ASTNode parseStructure() {

		//parse a function
		if(lex.nextValueIs("func")) {
			return parseFunction();
		}
		
		//parse a while loop
		if (lex.nextValueIs("while")) {
			return parseWhile();
		}

		//parse a for loop
		if (lex.nextValueIs("for")) {
			return parseFor();
		}

		//parse an if statement
		if (lex.nextValueIs("if")) {
			return parseIf();
		}

		//parse a do-while loop
		if (lex.nextValueIs("do")) {
			return parseDo();
		}

		//parse a switch statement
		if (lex.nextValueIs("switch")) {
			return parseSwitch();
		}
		
		if(lex.nextValueIs("print")){
			return parsePrint();
		}

		//give an error if no structure is found
		error("structure");

		return null;
	}
	
	//TODO: Fix to make this LL(1)
	public static BinaryAST parseFunctionCallOrAssignment(){
		
		Token identifier = lex.consume();
		
		// a function call has two parts: the name (AST) and the arguments (Nary)
		
		if(lex.nextValueIs("(")){
			BinaryAST node = new BinaryAST(new ASTNode(identifier), new Token("call", Token.STRUCTURE), parseArguments());
			return node;
		} else {
			lex.returnToken(identifier);
			return parseAssignment();
		}
		
	}
	
	/**
	 * Parses a set of arguments.
	 * @return A NaryAST representing the set.
	 */
	public static NaryAST parseArguments(){
		
		NaryAST node = new NaryAST(new Token("arguments", Token.GROUPING));
		
		expect("(");
		
		//add multiple arguments
		while(!lex.nextValueIs(")")){
			node.addNode(parseExpression());
			
			//commas between arguments
			if(!lex.nextValueIs(")")){
				expect(",");
			}
		}
		
		expect(")");
		
		return node;
	}
	
	public static BinaryAST parseAnonymousFunction(){
		lex.consume();
		return new BinaryAST(parseParameters(), new Token("function", Token.STRUCTURE), parseBlock());
	}
	
	public static UnaryAST parsePrint(){
		UnaryAST node = new UnaryAST(lex.consume());
		
		//compound expressions must be in parentheses
		if(lex.nextValueIs("(")){
			expect("(");
			node.child = parseExpression();
			expect(")");
		}
		
		//single elements can be alone
		else {
			node.child = parsePrimary();
		}
		
		expect(";");
		
		return node;
	}

	/**
	 * Parses a switch statement.
	 * @return A BinaryAST node representing the statement
	 */
	public static BinaryAST parseSwitch() {
		BinaryAST node = new BinaryAST(null, lex.consume(), null);

		//get the rest of the head
		expect("(");
		node.left = parseExpression();
		expect(")");
		expect("{");

		//hold the case statements
		NaryAST block = new NaryAST();
		block.token = new Token("switch-body", Token.GROUPING);

		//add cases to the block
		while (lex.read().value.equals("case")) {
			block.addNode(parseCase());
		}

		//optional default statement
		if (lex.read().value.equals("default")) {
			block.addNode(parseDefault());
		}

		expect("}");

		node.right = block;

		return node;
	}

	/**
	 * Parses a case statement.
	 * @return A BinaryAST node representing the case.
	 */
	public static BinaryAST parseCase() {
		BinaryAST node = new BinaryAST(null, lex.consume(), null);

		//get the expression
		expect("(");
		node.left = parseExpression();
		expect(")");
		
		//get the following statements
		node.right = parseBlock();

		return node;
	}

	/**
	 * Parses a default statement.
	 * @return A UnaryAST node representing the default.
	 */
	public static UnaryAST parseDefault() {
		UnaryAST node = new UnaryAST(null, lex.consume());

		node.child = parseBlock();

		return node;
	}
	
	/**
	 * Parses function declarations. Called when the keyword "func" is the start of a statement.
	 * @return A Binary node representing the function.
	 */
	public static BinaryAST parseFunction(){
		//a function consists of a name (left), parameters (center), and a body (right)
		
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		node.left = new ASTNode(expect(Token.IDENTIFIER));
		node.right = new BinaryAST(parseParameters(), new Token("function", Token.STRUCTURE), parseBlock());
		
		return node;
	}
	
	/**
	 * Parses a set of parameters (type, name).
	 * @return A NaryASt representing the set of parameters.
	 */
	public static NaryAST parseParameters(){
		NaryAST node = new NaryAST(new Token("parameters", Token.GROUPING));
		
		expect("(");
		
		//collect the parameters
		while(!lex.nextValueIs(")")){
			Token type = expect(Token.TYPE);
			Token identifier = expect(Token.IDENTIFIER);
			
			//a parameter consists of a type (left) and a name (right)
			BinaryAST param = new BinaryAST(new ASTNode(type), new Token("parameter", Token.BLANK), new ASTNode(identifier));
			
			node.addNode(param);
			
			//need a comma if there is a next parameter
			if(!lex.nextValueIs(")")){
				expect(",");
			}
		}
		
		expect(")");
		
		return node;
	}
	
	//TODO: Make for loops LL(1)
	public static QuaternaryAST parseFor() {
		QuaternaryAST node = new QuaternaryAST(lex.consume());

		// read the condition
		expect("(");

		// get a declaration
		if (lex.nextTypeIs(Token.TYPE)) {
			node.left = parseDeclaration();
			expect(";");
		} else if (lex.nextTypeIs(Token.IDENTIFIER)) {
			Token t = lex.consume();

			// check for assignment
			if (lex.nextValueIs("=")) {
				node.left = new BinaryAST(new ASTNode(t), lex.consume(), parseExpression());
				expect(";");
			} else {
				lex.returnToken(t);
				node.left = new ASTNode(new Token(Token.BLANK));
			}
		}

		// get the condition
		node.left_center = parseExpression();

		// get the optional final statement
		if (!lex.nextValueIs(")")) {
			expect(";");

			if (lex.nextTypeIs(Token.IDENTIFIER)) {
				node.right_center = parseAssignment();
			} else if (lex.nextTypeIs(Token.STRUCTURE)) {
				node.right_center = parseStructure();
			} else if (lex.nextTypeIs(Token.STATEMENT)) {
				node.right_center = new ASTNode(lex.consume());
			} else {
				error("statement");
			}
		} else {
			node.right_center = new ASTNode(new Token(Token.BLANK));
		}

		expect(")");

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}

		return node;
	}

	/**
	 * Parses a do-while loop.
	 * @return A BinaryAST node representing the loop.
	 */
	public static BinaryAST parseDo() {
		BinaryAST node = new BinaryAST(null, lex.consume(), null);

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}

		// read the condition
		expect("while");
		expect("(");
		node.left = parseExpression();
		expect(")");
		expect(";");

		return node;
	}

	/**
	 * Parses a while loop.
	 * @return A BinaryAST node representing the while.
	 */
	public static BinaryAST parseWhile() {
		BinaryAST node = new BinaryAST(null, lex.consume(), null);

		// read the condition
		expect("(");
		node.left = parseExpression();
		expect(")");

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}

		return node;
	}

	/**
	 * Parses an if, else, or else if statement.
	 * @return An ASTNode representing the if statement.
	 */
	public static ASTNode parseIf() {
		TernaryAST node = new TernaryAST(lex.consume());

		// get condition
		expect("(");
		node.left = parseExpression();
		expect(")");

		// get the body as a block or a single statement
		if (lex.nextValueIs("{")) {
			node.center = parseBlock();
		} else {
			node.center = parseStatement();
		}

		// the optional else
		if (lex.nextValueIs("else")) {
			lex.consume();

			// the else can be another if
			if (lex.nextValueIs("if")) {
				node.right = parseIf();
			} else {
				if (lex.nextValueIs("{")) {
					node.right = parseBlock();
				} else {
					node.right = parseStatement();
				}
			}

		} else {
			node.right = new ASTNode(new Token("", Token.BLANK));
		}

		return node;
	}

	/**
	 * Parses a block of code. A block consists of any number of statements.
	 * @return A NaryAST node representing the block.
	 */
	public static NaryAST parseBlock() {

		expect("{");

		NaryAST node = new NaryAST();

		node.token = new Token("block", Token.GROUPING);

		// read statements until the block closes
		while (!lex.nextValueIs("}")) {
			node.addNode(parseStatement());
		}

		expect("}");

		return node;
	}

	/**
	 * Parses a declaration statement. A declaration consists of a data 
	 * type and an assignment.
	 * 
	 * @return A TernaryAST node representing the declaration.
	 */
	public static TernaryAST parseDeclaration() {
		
		// a declaration begins with a type
		if (lex.nextTypeIs(Token.TYPE)) {
			Token t = lex.consume();
			TernaryAST node = new TernaryAST(new Token("declaration", Token.TYPE));
			node.left = new ASTNode(t); //get the type
			node.center = new ASTNode(expect(Token.IDENTIFIER)); //get the identifier
			
			//the right node is either blank or an expression
			if(lex.nextValueIs(";")){
				node.right = new ASTNode(new Token(Token.BLANK));
			} else {
				expect("="); //only the simple assignment is acceptable
				node.right = parseExpression();
			}
			
			return node;
		}

		error("type");

		return null;
	}

	/**
	 * Parses an assignment statement. An assignment consists of an 
	 * identifier, an assignment operator, and an expression.
	 * 
	 * @return A BinaryAST node representing the assignment.
	 */
	public static BinaryAST parseAssignment() {
		if (!lex.nextTypeIs(Token.IDENTIFIER)) {
			error("identifier");
		}
		
		BinaryAST node = new BinaryAST(new ASTNode(lex.consume()), null, null);

		if (lex.nextValueIs("=")) {
			node.token = lex.consume();
		} else if (lex.nextTypeIs(Token.ASSIGNMENT)){
			Token t = lex.consume();
			node.token = new Token("=", Token.OPERATOR);
			node.token.subtype = Token.ASSIGNMENT;
			
			Token op = new Token(t.value.charAt(0) + "", Token.OPERATOR);
			op.subtype = Token.MATHEMATICAL;
			
			ASTNode secondExp;
			
			if(t.value.equals("++") || t.value.equals("--")){
				secondExp = new ASTNode(new Token("1", Token.LITERAL));
				secondExp.token.subtype = Token.INTEGER;
			} else {
				secondExp = parseExpression();
			}
			
			node.right = new BinaryAST(new ASTNode(node.left.token), op, secondExp);
			
			return node;
		} else {
			error("assignment operator");
		}

		node.right = parseExpression();

		return node;
	}

	/**
	 * Recursively parses an expression using the precedence levels of the language.
	 * An expression is either a primary value (literal or variable) or something which
	 * is evaluated to a primary value.
	 * 
	 * @return An ASTNode representing the expression.
	 */
	public static ASTNode parseExpression() {
		if(lex.nextValueIs("func")){
			return parseAnonymousFunction();
		}
		
		return parseConditional();
	}

	/**
	 * Parses the conditional operator. Precedence: 2
	 * @return An ASTNode representing the expression parsed to level 2.
	 */
	public static ASTNode parseConditional() {

		ASTNode node = parseAND();

		if (lex.nextValueIs("?")) {
			Token t = lex.consume();
			node = new TernaryAST(node, parseExpression(), null, t);
			expect(":");

			((TernaryAST) node).right = parseExpression();
		}

		return node;
	}

	/**
	 * Parses && and ~NAND. Precedence: 3
	 * @return An ASTNode representing the expression parsed to level 3.
	 */
	public static ASTNode parseAND() {
		ASTNode node = parseOR();

		while (lex.nextValueIs("&&", "~NAND")) {
			node = new BinaryAST(node, lex.consume(), parseOR());
		}

		return node;
	}

	/**
	 * Pares || and ~NOR. Precedence: 4
	 * @return An ASTNode representing the expression parsed to level 4.
	 */
	public static ASTNode parseOR() {
		ASTNode node = parseEquality();

		while (lex.nextValueIs("||", "~NOR")) {
			node = new BinaryAST(node, lex.consume(), parseEquality());
		}

		return node;
	}

	/**
	 * Parses == and !=. Precedence: 5
	 * @return An ASTNode representing the expression parsed to level 5.
	 */
	public static ASTNode parseEquality() {
		ASTNode node = parseComparison();

		while (lex.nextValueIs("==", "!=")) {
			node = new BinaryAST(node, lex.consume(), parseComparison());
		}

		return node;
	}

	/**
	 * Parses comparison operators. Precedence: 6
	 * @return An ASTNode representing the expression parsed to level 6.
	 */
	public static ASTNode parseComparison() {
		ASTNode node = parseAddition();

		while (lex.nextValueIs("<", ">", "<=", ">=")) {
			node = new BinaryAST(node, lex.consume(), parseAddition());
		}

		return node;
	}

	/**
	 * Parses addition and subtraction. Precedence: 7
	 * @return An ASTNode representing the expression parsed to level 7.
	 */
	public static ASTNode parseAddition() {
		ASTNode node = parseMultiplication();

		while (lex.nextValueIs("+", "-")) {
			node = new BinaryAST(node, lex.consume(), parseMultiplication());
		}

		return node;
	}

	/**
	 * Parses multiplication and division. Precedence: 8
	 * @return An ASTNode representing the expression parsed to level 8.
	 */
	public static ASTNode parseMultiplication() {
		ASTNode node = parseExponentiation();

		while (lex.nextValueIs("*", "/", "%")) {
			node = new BinaryAST(node, lex.consume(), parseExponentiation());
		}

		return node;
	}

	/**
	 * Parses exponentiation. Precedence: 9
	 * @return An ASTNode representing the expression parsed to level 9.
	 */
	public static ASTNode parseExponentiation() {
		ASTNode node = parseUnary();

		while (lex.nextValueIs("^")) {
			node = new BinaryAST(node, lex.consume(), parseUnary());
		}

		return node;
	}

	/**
	 * Parses the unary operators - and !. Precedence: 10
	 * @return An ASTNode representing the expression parsed to level 10.
	 */
	public static ASTNode parseUnary() {

		while (lex.nextValueIs("-", "!")) {
			Token t = lex.consume();
			return new UnaryAST(parseUnary(), t);
		}

		return parseMember();
	}

	/**
	 * Parses membership. Precedence: 11
	 * @return An ASTNode representing the expression parsed to level 11.
	 */
	public static ASTNode parseMember() {
		ASTNode node = parsePrimary();

		while (lex.nextValueIs(".")) {
			node = new BinaryAST(node, lex.consume(), parsePrimary());
		}

		return node;
	}

	/**
	 * Parses a primary value (literal, identifier, or expression). Precedence: 12
	 * @return An ASTNode representing the expression parsed to level 12.
	 */
	public static ASTNode parsePrimary() {
		
		//handles literals
		if (lex.nextTypeIs(Token.LITERAL)) {
			return new ASTNode(lex.consume());
		}

		//handles variables
		if (lex.nextTypeIs(Token.IDENTIFIER)) {
			Token t = lex.consume();
			
			if(lex.nextValueIs("(")){
				NaryAST node = parseArguments();
				
				return new BinaryAST(new ASTNode(t), new Token("call", Token.GROUPING), node);
			}
			
			return new ASTNode(t);
		}

		//a type in an expression must be a cast
		if (lex.nextTypeIs(Token.TYPE)) {
			Token t = lex.consume();
			expect("(");
			UnaryAST node = new UnaryAST(parseExpression(), t);
			expect(")");

			return node;
		}

		//handles subexpressions
		if (lex.nextValueIs("(")) {
			lex.consume();
			ASTNode node = parseExpression();

			expect(")");

			return node;
		}

		// handles absolute value - |x|
		if (lex.nextValueIs("|")) {
			Token t = lex.consume();
			UnaryAST node = new UnaryAST(parseExpression(), t);

			expect("|");

			return node;
		}

		expect("(, literal, or identifier");

		return null;
	}

	/**
	 * Consumes the next token if it matches the given string, prints an error otherwise.
	 * @param str The expected value.
	 */
	public static void expect(String str) {
		if (lex.hasNextToken()) {
			if (lex.nextValueIs(str)) {
				lex.consume();
				return;
			}
		}

		error(str);

		return;
	}
	
	public static Token expect(int i){
		if(lex.nextTypeIs(i)){
			return lex.consume();
		}
		
		error(Token.names[i]);
		
		return null;
	}

	/**
	 * Called when unexpected input is received. Prints the location and the expected input.
	 * Exits after printing the error.
	 * @param str The expected input.
	 */
	public static void error(String str) {
		Token t = lex.read();

		if (t == null) {
			System.err.println("Error on final line :");
			System.err.println("  Expected : " + str);
			System.err.println("  Recieved : Nothing");
		} else {
			System.err.println("Error on line " + t.line_loc + ", character " + t.char_loc + " :");
			System.err.println("  Expected : " + str);
			System.err.println("  Recieved : " + t.value);
		}

		System.exit(0);
	}
}
