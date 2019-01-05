package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * The GenerateAST class is used to automatically generate the Expr and Stmt files, given only
 * a list of the subclasses and their fields.
 * @author Jared
 */
public class GenerateAST {
	public static void main(String[] args) throws IOException {
		
		/* the class takes a single argument, the output directory */
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(1);
		}
		
		String outputDir = args[0];

		defineAst(outputDir, "Expr", Arrays.asList(
				"Conditional : Expr condition, Token operator, Expr first, Expr second",
				"Binary      : Expr left, Token operator, Expr right",
				"Logical     : Expr left, Token operator, Expr right",
				"Grouping    : Token grouping, Expr expression", 
				"Literal     : Object value", 
				"Unary       : Token operator, Expr right",
				"Variable    : Token name",
				"Assign      : Token name, Token operator, Expr value",
				"UnaryAssign : Token name, Token operator",
				"Call        : Expr callee, Token parenthesis, ArrayList<Expr> arguments"));

		defineAst(outputDir, "Stmt", Arrays.asList(
				"Keyword    : Token word",
				"Expression : Expr expression",
				"ExPrint    : Expr expression",
			    "Print      : Expr expression",
			    "Declare    : Token type, Token name, Expr value",
			    "Block      : ArrayList<Stmt> statements",
			    "While      : Expr condition, Stmt block",
			    "Do         : Expr condition, Stmt block",
			    "For        : Stmt declaration, Expr condition, Expr incrementor, Stmt block",
			    "Switch     : ArrayList<Expr> controls, ArrayList<Stmt.Case> cases, Stmt defaultCase",
			    "Case       : ArrayList<Expr> tests, Stmt block",
			    "If         : Expr condition, Stmt ifBlock, Stmt elseBlock",
			    "Undec      : ArrayList<Expr> variables",
			    "Struct     : Stmt template, Stmt mold",
			    "Template   : ArrayList<Object> template",
			    "Mold       : Stmt function",
			    "Function   : Token name, ArrayList<Token> types, ArrayList<Token> parameters, Stmt.Block block"));
	}

	/**
	 * Writes the Expr.java class and outputs to the specified directory.
	 * @param outputDir the directory in which to place the new class
	 * @param baseName the name of the new class
	 * @param types the names of the subclasses
	 * @throws IOException if the directory cannot be found
	 */
	private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		writer.println("package interpreter;");
		writer.println();
		writer.println("import java.util.ArrayList;");
		writer.println();
		writer.println("public abstract class " + baseName + " {");

		defineVisitor(writer, baseName, types);

		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
		}

		writer.println();
		writer.println("\tabstract <T> T accept(Visitor<T> visitor);");
		writer.println("\tabstract String toString(int i);");
		writer.println();
		
		writer.println("\tpublic String toString() {");
		writer.println("\t\treturn this.toString(0);");
		writer.println("\t}");
		writer.println();
	
		//arraylist toString method
		writer.println("\tpublic static String arrayListToString(ArrayList<?> list) {");
		writer.println("\t\tString str = \"\";");
		writer.println("\t\tfor(int i = 0; i < list.size(); i++) {");
		writer.println("\t\t\tstr = str + \"\\n\" + list.get(i);");
		writer.println("\t\t}");
		writer.println();
		writer.println("\t\treturn str;");
		writer.println("\t}");
		
		writer.println("}");
		writer.close();
	}

	/**
	 * Writes an individual subclass to the file.
	 * @param writer the PrintWriter
	 * @param baseName the name of the class
	 * @param className the name of the subclass
	 * @param fieldList the fields of the subclass
	 */
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		writer.println("\tpublic static class " + className + " extends " + baseName + " {");

		// Constructor.
		writer.println("\t\tpublic " + className + "(" + fieldList + ") {");
		
		String printTracker = "";
		String arrayListName = "";

		// Store parameters in fields.
		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			String name = field.split(" ")[1];
			writer.println("\t\t\tthis." + name + " = " + name + ";");
			
			/* generates the return line of the toString method */
			if(field.split(" ")[0].equals("Token")) {
				printTracker = "+" + name + ".lexeme+\"\\n\"" + printTracker;
			} else if (field.split(" ")[0].equals("Object")) {
				printTracker = "+" + name;
			} else if (field.split(" ")[0].startsWith("ArrayList<")) {
				printTracker += "+" + "arrayListToString(" + name + ")";
			} else {
				printTracker += "+" + name + ".toString(depth+1)+\"\\n\"";
			}
		}
		
		//remove trailing newline chars
		if(printTracker.endsWith("+\"\\n\"")){
			printTracker = printTracker.substring(0, printTracker.length() - 5);
		}

		writer.println("\t\t}");
		writer.println();
		
		defineToString(writer, printTracker, arrayListName);

		writer.println();
		writer.println("\t\t<T> T accept(Visitor<T> visitor) {");
		writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
		writer.println("\t\t}");

		// Fields.
		writer.println();
		for (String field : fields) {
			writer.println("\t\tpublic final " + field + ";");
		}

		writer.println("\t}");
		writer.println();
	}
	
	/**
	 * Generates the toString method.
	 * @param writer the PrintWriter
	 * @param returnVal the value of the return line
	 * @param arrayListName optional name of the ArrayList
	 */
	private static void defineToString(PrintWriter writer, String returnVal, String arrayListName) {
		
		/* the header and indentation loop of the toString method */
		writer.println("\t\tpublic String toString(int depth) {");
		writer.println("\t\t\tString str = \"\";");
		writer.println("\t\t\tfor(int i = 0; i < depth; i++) {");
		writer.println("\t\t\t\tstr = str + \"   \";");
		writer.println("\t\t\t}");
		writer.println();
		
		String listString = "";
		
		returnVal = returnVal.replace("+", " + ");
		listString = listString.replace("+", " + ");
		
		writer.println("\t\t\treturn str" + returnVal + listString + ";");
		writer.println("\t\t}");
	}

	/**
	 * Writes the Visitor interface.
	 * @param writer the PrintWriter
	 * @param baseName the name of the expression class
	 * @param types the subclass names
	 */
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("\tinterface Visitor<T> {");

		/* prints the visit method for each subclass */
		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("\t\tT visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("\t}");
		writer.println();
	}
}