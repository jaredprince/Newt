package parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lexer.Lexer;

public class Parser {
	
	//TODO: Add arrays
	
	//TODO: Add sets
	
	//TODO: compare references of objects with "==="
	
	//TODO: add classes
	
	//TODO: try/catch structures
	
	static Lexer lex;
	static ASTNode root;

	static LinkedList<String[]> operators;
	
	public static Environment environment = new Environment();

	public static void main(String[] args) throws IOException {
		lex = new Lexer(new File("Parser Test.txt"));

		defineNatives();
		
		NaryAST tree = parseProgram();
		tree.visitNode();
	}
	
	public static void defineNatives() {
		//a native method
		environment.define(new Token("native", Token.DATA_TYPE), new Token("test", Token.IDENTIFIER), new TypedObject("native", new Callable(){
			
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
		
		environment.define(new Token("native", Token.DATA_TYPE), new Token("print", Token.IDENTIFIER), new TypedObject("native", new Callable(){
			
			@Override
			public TypedObject call(Parser parser, List<TypedObject> args) {
				System.out.print(args.get(0).object);
				return null;
			}

			@Override
			public int arity() {
				return 1;
			}
			
		}));
	}

	//TODO: replace multiple operator methods with a list of string[]
	public static void operatorsPrecedence() {
		String[][] ops = { { "==", "!=" }, { ">=", "<=", "<", ">" }, { "+", "-" }, { "*", "/", "%" }, { "^" },
				{ "-", "!" }, {"."} };

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
			
			//functions in the outermost scope get declared immediately so they can be used
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
				Token t = lex.consume();
				
				if(!lex.nextValueIs(";")){
					expect(";");
					return new UnaryAST(parseExpression(), t);
				} else {
					expect(";");
					return new ASTNode(t);
				}
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
		else if (lex.nextTypeIs(Token.DATA_TYPE)) {
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
		
		//give an error if no structure is found
		error("structure");

		return null;
	}
	
	//TODO: Fix to make this LL(1)
	public static ASTNode parseFunctionCallOrAssignment(){
		
		Token identifier = lex.consume();
		
		// a function call has two parts: the name (AST) and the arguments (Nary)
		
		if(lex.nextValueIs("(")){
			CallNode node = new CallNode(new ASTNode(identifier), new Token("call", Token.STRUCTURE), parseArguments());
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
	
//	public static BinaryAST parseAnonymousFunction(){
//		lex.consume();
//		return new BinaryAST(parseParameters(), new Token("function", Token.STRUCTURE), parseBlock());
//	}
	

	/**
	 * Parses a switch statement.
	 * @return A BinaryAST node representing the statement
	 */
	public static SwitchNode parseSwitch() {
		Token t = lex.consume();
		t.type = Token.GROUPING;
		SwitchNode node = new SwitchNode(t);

		//get the rest of the head
		expect("(");
		ArrayList<ASTNode> testVal = new ArrayList<ASTNode>();
		ASTNode firstVal = parseExpression();
		testVal.add(firstVal);
		
		while(lex.nextValueIs(",")){
			expect(",");
			testVal.add(parseExpression());
		}
		
		expect(")");
		expect("{");
		
		//add cases to the block
		while (lex.read().value.equals("case")) {
			node.addNode(parseCase(testVal));
		}

		//optional default statement
		if (lex.read().value.equals("default")) {
			lex.consume();
			node.defaultNode = parseBlock();
		}

		expect("}");

		return node;
	}

	/**
	 * Parses a case statement.
	 * @param testVal The value used in the switch as the test value.
	 * @return A BinaryAST node representing the case.
	 */
	public static CaseNode parseCase(ArrayList<ASTNode> testVal) {
//		lex.consume();
//		TernaryAST node = new TernaryAST(new Token("if", Token.STRUCTURE));
//
//		//get the expression
//		expect("(");
//		Token t = new Token("==", Token.OPERATOR);
//		t.subtype = Token.COMPARATIVE;
//		node.left = new BinaryAST(testVal, t, parseExpression());
//		expect(")");
//		
//		//get the following statements
//		node.center = parseBlock();
//		
//		node.right = new ASTNode(new Token("", Token.BLANK));
//
//		return node;
		
		CaseNode node = new CaseNode(lex.consume());
		
		//get the expression
		expect("(");
		Token t = new Token("==", Token.OPERATOR);
		t.subtype = Token.COMPARATIVE;
		node.value = new OperationNode(testVal.get(0), t, parseExpression());
		expect(")");
		
		node.body = parseBlock();		
		
		return node;
	}
	
	public static CaseNode parseComplexCase(ArrayList<ASTNode> testVals){
//		lex.consume();
//		TernaryAST node = new TernaryAST(new Token("if", Token.STRUCTURE));
//
//		//get the expression
//		expect("(");
//		Token equals = new Token("==", Token.OPERATOR);
//		equals.subtype = Token.COMPARATIVE;
//		
//		Token and = new Token("&&", Token.OPERATOR);
//		and.subtype = Token.LOGICAL;
//		
//		node.left = new BinaryAST(testVals.get(0), equals, parseExpression());
//		int i = 1;
//		while(i < testVals.size()){
//			if(!lex.nextValueIs(",")){
//				Token t = new Token("any", Token.LITERAL);
//				t.subtype = Token.SPECIAL_VALUE;
//				node.left = new BinaryAST(node.left, and, new BinaryAST(testVals.get(i), equals, new ASTNode(t)));
//			} else {
//				expect(",");
//				node.left = new BinaryAST(node.left, and, new BinaryAST(testVals.get(i), equals, parseExpression()));
//			}
//			
//			i++;
//		}
//		
//		expect(")");
//		
//		//get the following statements
//		node.center = parseBlock();
//		node.right = new ASTNode(new Token("", Token.BLANK));
//		return node;
		
		return null;
	}
	
	/**
	 * Parses function declarations. Called when the keyword "func" is the start of a statement.
	 * @return A Binary node representing the function.
	 */
	public static FunctionNode parseFunction(){

		//a function consists of a name (left), parameters (center), and a body (right)
		FunctionNode node = new FunctionNode(null, null, lex.consume(), null);
		
		node.name = new ASTNode(expect(Token.IDENTIFIER));
		node.params = parseParameters();		
		node.body = parseBlock();
		
		return node;
	}
	
	/**
	 * Parses a set of parameters (type, name).
	 * @return A NaryASt representing the set of parameters.
	 */
	public static ArrayList<DeclarationNode> parseParameters(){
		ArrayList<DeclarationNode> params = new ArrayList<DeclarationNode>();
		
		expect("(");
		
		//collect the parameters
		while(!lex.nextValueIs(")")){
			Token type = expect(Token.DATA_TYPE);
			Token identifier = expect(Token.IDENTIFIER);
			
			//a parameter consists of a type (left) and a name (right)
			DeclarationNode param = new DeclarationNode(new ASTNode(type), new ASTNode(identifier), new Token("declaration", Token.BLANK));
			
			params.add(param);
			
			//need a comma if there is a next parameter
			if(!lex.nextValueIs(")")){
				expect(",");
			}
		}
		
		expect(")");
		
		return params;
	}
	
	//TODO: Make for loops LL(1)
	public static ForNode parseFor() {
		ForNode node = new ForNode(lex.consume());

		// read the condition
		expect("(");

		// get a declaration
		if (lex.nextTypeIs(Token.DATA_TYPE)) {
			node.declaration = parseDeclaration();
		}
		
		expect(";");

		/*
		else if (lex.nextTypeIs(Token.IDENTIFIER)) {
			Token t = lex.consume();

			// check for assignment
			if (lex.nextValueIs("=")) {
				node.declaration = new DeclarationNode(new ASTNode(t), lex.consume(), parseExpression());
				expect(";");
			} else {
				lex.returnToken(t);
				node.declaration = null;
			}
		}
		*/

		// get the condition
		node.condition = parseExpression();

		// get the optional final statement
		if (!lex.nextValueIs(")")) {
			expect(";");

			if (lex.nextTypeIs(Token.IDENTIFIER)) {
				node.assignment = parseAssignment();
			} 
			
			/*else if (lex.nextTypeIs(Token.STRUCTURE)) {
				node.right_center = parseStructure();
			} else if (lex.nextTypeIs(Token.STATEMENT)) {
				node.right_center = new ASTNode(lex.consume());
			} 
			*/
			
			else {
				error("statement");
			}
		} else {
			node.assignment = null;
		}

		expect(")");

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.body = parseBlock();
		} else {
			node.body = new StructureBodyNode();
			node.body.addNode(parseStatement());
		}

		return node;
	}

	/**
	 * Parses a do-while loop.
	 * @return A BinaryAST node representing the loop.
	 */
	public static DoWhileNode parseDo() {
		DoWhileNode node = new DoWhileNode(null, lex.consume(), null);

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.body = parseBlock();
		} else {
			node.body = new StructureBodyNode();
			node.body.addNode(parseStatement());
		}
		
		// read the condition
		expect("while");
		expect("(");
		node.condition = parseExpression();
		expect(")");
		expect(";");

		return node;
	}

	/**
	 * Parses a while loop.
	 * @return A BinaryAST node representing the while.
	 */
	public static WhileNode parseWhile() {
		WhileNode node = new WhileNode(null, lex.consume(), null);

		// read the condition
		expect("(");
		node.condition = parseExpression();
		expect(")");

		// the body can be a block starting with '{' or a single statement
		if (lex.nextValueIs("{")) {
			node.body = parseBlock();
		} else {
			node.body = new StructureBodyNode();
			node.body.addNode(parseStatement());
		}

		return node;
	}

	/**
	 * Parses an if, else, or else if statement.
	 * @return An ASTNode representing the if statement.
	 */
	public static IfElseNode parseIf() {
		IfElseNode node = new IfElseNode(lex.consume());

		// get condition
		expect("(");
		node.condition = parseExpression();
		expect(")");

		// get the body as a block or a single statement
		if (lex.nextValueIs("{")) {
			node.ifBody = parseBlock();
		} else {
			node.ifBody = new StructureBodyNode();
			node.ifBody.addNode(parseStatement());
		}

		// the optional else
		if (lex.nextValueIs("else")) {
			lex.consume();

			// the else can be another if
			if (lex.nextValueIs("if")) {
				node.elseBody = new StructureBodyNode();
				node.elseBody.addNode(parseIf());
			} else {
				if (lex.nextValueIs("{")) {
					node.elseBody = parseBlock();
				} else {
					node.elseBody = new StructureBodyNode();
					node.elseBody.addNode(parseStatement());
				}
			}

		} else {
			node.elseBody = null;
		}

		return node;
	}

	/**
	 * Parses a block of code. A block consists of any number of statements.
	 * @return A NaryAST node representing the block.
	 */
	public static StructureBodyNode parseBlock() {

		expect("{");

		StructureBodyNode node = new StructureBodyNode();

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
	public static DeclarationNode parseDeclaration() {
		
		// a declaration begins with a type
		if (lex.nextTypeIs(Token.DATA_TYPE)) {
			Token t = lex.consume();
			DeclarationNode node = new DeclarationNode(new Token("declaration", Token.DATA_TYPE));
			node.type = new ASTNode(t); //get the type
			node.name = new ASTNode(expect(Token.IDENTIFIER)); //get the identifier
			
			//the right node is either blank or an expression
			if(!lex.nextValueIs(";")){
				//only the simple assignment is acceptable
				node.assignment = new AssignmentNode(node.name, expect("="), parseExpression());
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
	public static AssignmentNode parseAssignment() {
		if (!lex.nextTypeIs(Token.IDENTIFIER)) {
			error("identifier");
		}
		
		AssignmentNode node = new AssignmentNode(new ASTNode(lex.consume()), null, null);

		if (lex.nextValueIs("=")) {
			node.token = lex.consume();
			node.value = parseExpression();
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
			
			node.value = new OperationNode(new ASTNode(node.variable.token), op, secondExp);
			
			return node;
		} else {
			error("assignment operator");
		}

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
//		if(lex.nextValueIs("func")){
//			return parseAnonymousFunction();
//		}
		
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
			node = new TertiaryOperationNode(node, parseExpression(), t, null);
			expect(":");

			((TertiaryOperationNode) node).right = parseExpression();
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
			node = new OperationNode(node, lex.consume(), parseOR());
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
			node = new OperationNode(node, lex.consume(), parseEquality());
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
			node = new OperationNode(node, lex.consume(), parseComparison());
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
			node = new OperationNode(node, lex.consume(), parseAddition());
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
			node = new OperationNode(node, lex.consume(), parseMultiplication());
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
			node = new OperationNode(node, lex.consume(), parseExponentiation());
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
			node = new OperationNode(node, lex.consume(), parseUnary());
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
			node = new OperationNode(node, lex.consume(), parsePrimary());
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
				
				return new CallNode(new ASTNode(t), new Token("call", Token.GROUPING), node);
			}
			
			return new ASTNode(t);
		}

		//a type in an expression must be a cast
		if (lex.nextTypeIs(Token.DATA_TYPE)) {
			Token t = lex.consume();
			expect("(");
			UnaryAST node = new UnaryAST(parseExpression(), t);
			expect(")");

			return node;
		}
		
		//special values (any, inf, neginf) are literals
		if(lex.nextTypeIs(Token.SPECIAL_VALUE)){
			return new ASTNode(lex.consume());
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
	 * 
	 * @param str The expected value.
	 * @return The token consumed.
	 */
	public static Token expect(String str) {
		if (lex.hasNextToken()) {
			if (lex.nextValueIs(str)) {
				return lex.consume();
			}
		}

		error(str);

		return null;
	}
	
	/**
	 * Consumes a token if it is of the given type, prints an error otherwise.
	 * 
	 * @param i The expected type.
	 * @return The consumed token.
	 */
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
