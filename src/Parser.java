import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class Parser {

	static Lexer lex;
	static ASTNode root;
	
	static LinkedList<String[]> operators;
	
	public static void main(String[] args) throws IOException {
		
		lex = new Lexer(new File("ParserTest1.txt"));
		
//		while(lex.hasNextToken()){
//			System.out.println(lex.consume());
//		}
		
		root = parseProgram();

		ASTNode r = root;
		
		System.out.println(r.toString(0));
	}
	
	//this will replace the many operator parsing methods
	public static void operatorsPrecedence(){
		String[][] ops = {{"==", "!="}, {">=", "<=", "<", ">"}, {"+", "-"}, {"*", "/", "%"}, {"^"}, {"-", "!"}};
		
		operators = new LinkedList<String[]>();
		for(int i = 0; i < ops.length; i++){
			operators.add(ops[i]);
		}
	}
	
	public static ASTNode parseProgram(){
		NaryAST node = new NaryAST(new Token("program", Token.GROUPING));
		
		while(lex.hasNextToken()){
			node.addNode(parseStatement());
		}
		
		return node;
	}
	
	public static ASTNode parseStatement(){
		
		if(lex.read().type == Token.STATEMENT){
			ASTNode node = new ASTNode(lex.consume());
			expect(";");
			return node;
		}
		
		else if(lex.read().type == Token.STRUCTURE) {
			return parseStructure();
		}

		else if(lex.read().type == Token.TYPE){
			ASTNode node = parseDeclaration();
			expect(";");
			return node;
		}
		
		else if(lex.read().type == Token.IDENTIFIER){
			ASTNode node = parseAssignment();
			expect(";");
			return node;
		}
		
		error("Keyword or identifier");
		
		return null;
	}
	
	public static ASTNode parseStructure(){
		
		if(lex.read().value.equals("while")){
			return parseWhile();
		}
		
		if(lex.read().value.equals("for")){
			return parseFor();
		}
		
		if(lex.read().value.equals("if")){
			return parseIf();
		}
		
		if(lex.read().value.equals("do")){
			return parseDo();
		}
		
		if(lex.read().value.equals("switch")){
			return parseSwitch();
		}
		
		error("structure");
		
		return null;
	}
	
	public static BinaryAST parseSwitch(){
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		expect("(");		
		node.left = parseExpression();
		expect(")");
		expect("{");
		
		NaryAST block = new NaryAST();
		block.token = new Token("switch-body", Token.GROUPING);
		
		while(lex.read().value.equals("case")){
			block.addNode(parseCase());
		}
		
		if(lex.read().value.equals("default")){
			block.addNode(parseDefault());
		}
		
		expect("}");
		
		node.right = block;
		
		return node;
	}
	
	public static BinaryAST parseCase(){
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		expect("(");
		node.left = parseExpression();
		expect(")");
		node.right = parseBlock();
		
		return node;
	}
	
	public static UnaryAST parseDefault(){
		UnaryAST node = new UnaryAST(null, lex.consume());

		node.child = parseBlock();
		
		return node;
	}
	
	//not quite LL(1)
	public static QuaternaryAST parseFor(){
		QuaternaryAST node = new QuaternaryAST(lex.consume());
		
		//read the condition
		expect("(");
		
		//get a declaration
		if(lex.nextTypeIs(Token.TYPE)){
			node.left = parseDeclaration();
			expect(";");
		} else if(lex.nextTypeIs(Token.IDENTIFIER)) {
			Token t = lex.consume();
			
			//check for assignment
			if(lex.nextValueIs("=", "+=", "-=", "*=", "/=", "^=", "%=")){
				node.left = new BinaryAST(new ASTNode(t), lex.consume(), parseExpression());
				expect(";");
			} else {
				lex.returnToken(t);
				node.left = new ASTNode(new Token(Token.BLANK));
			}
		}
		
		//get the condition
		node.left_center = parseExpression();
		
		//get the optional final statement
		if(!lex.nextValueIs(")")){
			expect(";");
			
			if(lex.nextTypeIs(Token.IDENTIFIER)){
				node.right_center = parseAssignment();
			} else if (lex.nextTypeIs(Token.STRUCTURE)){
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
		
		//the body can be a block starting with '{' or a single statement
		if(lex.read().value.equals("{")){
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}
		
		return node;
	}
	
	//the same as while, but in a different order
	public static BinaryAST parseDo(){
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		//the body can be a block starting with '{' or a single statement
		if(lex.read().value.equals("{")){
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}
		
		//read the condition
		expect("while");
		expect("(");
		node.left = parseExpression();
		expect(")");
		expect(";");
		
		return node;
	}
	
	//a while loop is   condition token("while") block
	public static BinaryAST parseWhile(){
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		//read the condition
		expect("(");
		node.left = parseExpression();
		expect(")");
		
		//the body can be a block starting with '{' or a single statement
		if(lex.read().value.equals("{")){
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}
		
		return node;
	}
	
	//an if statement has three children - condition, if body, and optional else
	public static ASTNode parseIf(){
		BinaryAST node = new BinaryAST(null, lex.consume(), null);
		
		//get condition
		expect("(");
		node.left = parseExpression();
		expect(")");
		
		//get the body as a block or a single statement
		if(lex.read().value.equals("{")){
			node.right = parseBlock();
		} else {
			node.right = parseStatement();
		}
		
		//the optional else
		if(lex.read().value.equals("else")){
			lex.consume();
			
			TernaryAST elseNode = new TernaryAST(node.left, node.right, null, node.token);
			
			//the else can be another if
			if(lex.read().value.equals("if")){
				elseNode.right = parseIf();
			} else {
				if(lex.read().value.equals("{")){
					elseNode.right = parseBlock();
				} else {
					elseNode.right = parseStatement();
				}
			}
			
			return elseNode;
		}
		
		return node;
	}
	
	public static NaryAST parseBlock(){
		
		expect("{");
		
		NaryAST node = new NaryAST();
		
		node.token = new Token("block", Token.GROUPING);
		
		//read statements until the block closes
		while(!lex.read().value.equals("}")){
			node.addNode(parseStatement());
		}
		
		expect("}");
		
		return node;
	}
	
	public static UnaryAST parseDeclaration(){
		
		//a declaration begins with a type
		if(lex.read().type == Token.TYPE){
			Token t = lex.consume();
			return new UnaryAST(parseAssignment(), t);
		}
		
		error("type");
		
		return null;
	}
	
	//assignment is a binary node with identifier token("=", "+=", etc)
	public static BinaryAST parseAssignment(){
		if(lex.read().type != Token.IDENTIFIER){
			error("identifier");
		}
		
		BinaryAST node = new BinaryAST(new ASTNode(lex.consume()), null, null);
		
		if(match(lex.read().value, "=", "+=", "-=", "*=", "/=", "^=", "%=")){
			node.token = lex.consume();
		}
		
		node.right = parseExpression();
		
		return node;
	}
	
	public static ASTNode parseExpression(){
		return parseConditional();
	}
	
	public static ASTNode parseConditional(){
		
		ASTNode node = parseAND();
		
		if(lex.nextValueIs("?")){
			Token t = lex.consume();
			node = new TernaryAST(node, parseExpression(), null, t);
			expect(":");
			
			((TernaryAST)node).right = parseExpression();
		}
		
		return node;
	}
	
	public static ASTNode parseAND(){
		ASTNode node = parseOR();
		
		while(lex.nextValueIs("&&", "~NAND")){
			node = new BinaryAST(node, lex.consume(), parseOR());
		}
		
		return node;
	}
	
	public static ASTNode parseOR(){
		ASTNode node = parseEquality();
		
		while(lex.nextValueIs("||", "~NOR")){
			node = new BinaryAST(node, lex.consume(), parseEquality());
		}
		
		return node;
	}
	
	public static ASTNode parseEquality(){
		ASTNode node = parseComparison();
		
		while(lex.nextValueIs("==", "!=")){
			node = new BinaryAST(node, lex.consume(), parseComparison());
		}
		
		return node;
	}
	
	public static ASTNode parseComparison() {
		ASTNode node = parseAddition();
		
		while(lex.nextValueIs("<", ">", "<=", ">=")){
			node = new BinaryAST(node, lex.consume(), parseAddition());
		}
		
		return node;
	}

	public static ASTNode parseAddition() {
		ASTNode node = parseMultiplication();
		
		while(lex.nextValueIs( "+", "-")){
			node = new BinaryAST(node, lex.consume(), parseMultiplication());
		}
		
		return node;
	}
	
	public static ASTNode parseMultiplication() {
		ASTNode node = parseExponentiation();
		
		while(lex.nextValueIs("*", "/", "%")){
			node = new BinaryAST(node, lex.consume(), parseExponentiation());
		}

		return node;
	}
	
	public static ASTNode parseExponentiation() {
		ASTNode node = parseUnary();
		
		while(lex.nextValueIs("^")){
			node = new BinaryAST(node, lex.consume(), parseUnary());
		}

		return node;
	}
	
	public static ASTNode parseUnary() {
		
		while(match(lex.read().value, "-", "!")){
			Token t = lex.consume();
			return new UnaryAST(parseUnary(), t);
		}

		return parseMember();
	}
	
	public static ASTNode parseMember(){
		ASTNode node = parsePrimary();
		
		while(lex.nextValueIs(".")){
			node = new BinaryAST(node, lex.consume(), parsePrimary());
		}

		return node;
	}

	public static ASTNode parsePrimary() {
		if(lex.read().type == Token.LITERAL){
			return new ASTNode(lex.consume());
		}
		
		if(lex.read().type == Token.IDENTIFIER){
			return new ASTNode(lex.consume());
		}
		
		if(lex.read().type == Token.TYPE){
			Token t = lex.consume();
			expect("(");
			UnaryAST node = new UnaryAST(parseExpression(), t);
			expect(")");
			
			return node;
		}
		
		if(lex.read().value.equals("(")){
			lex.consume();
			ASTNode node = parseExpression();
			
			expect(")");
			
			return node;
		}
		
		//handles absolute value - |x|
		if(lex.read().value.equals("|")){
			Token t = lex.consume();
			UnaryAST node = new UnaryAST(parseExpression(), t);
			
			expect("|");
			
			return node;
		}
		
		expect("(, literal, or identifier");
		
		return null;
	}

	//checks if the first string matches any of the remaining strings
	public static boolean match(String value, String... strings){
	
		for(String str : strings){
			if(str.equals(value)){
				return true;
			}
		}
		
		return false;
	}
	
	//consumes a token if it is expected and prints an error otherwise
	public static Token expect(String str){
		if(lex.hasNextToken()){
			if(lex.read().value.equals(str)){
				return lex.consume();
			}
		}
		
		error(str);
		
		return null;
	}

	//prints an error message and exits
	public static void error(String str){
		Token t = lex.read();
		
		if(t == null){
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
