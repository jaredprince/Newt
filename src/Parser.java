import java.io.File;
import java.io.IOException;

public class Parser {

	static Lexer lex;
	static ASTNode root;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		lex = new Lexer(new File("src/Lexer.java"));
		
//		while(l.hasNextToken()){
//			System.out.println(l.getNextToken());
//		}
		
		parseProgram();
	}
	
	public static void parseProgram(){
		Token t = lex.getNextToken();
		
		if(t.type.equals("identifier")){
			parseStatement(t);
		}
	}
	
	public static void parseStatement(Token t){
		
	}
}
