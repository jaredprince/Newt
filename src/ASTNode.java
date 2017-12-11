import java.util.ArrayList;

public class ASTNode {

	static final int BLOCK = 1;
	static final int STATEMENT = 2;
	static final int ID = 3;
	static final int EXPRESSION = 4;
	static final int TERM = 5;
	static final int PROGRAM = 6;
	
	String type;	
	ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
	
	public ASTNode(String t){
		type = t;
	}
}