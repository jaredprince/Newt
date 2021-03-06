package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import interpreter.Expr.Variable;
import interpreter.Stmt.Function;

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
				"Get         : Expr object, Token name",
				"Set         : Expr object, Token name, Expr value",
				"This        : Token keyword",
				"Unary       : Token operator, Expr right",
				"Variable    : Token name",
				"Assign      : Token name, Token operator, Expr value",
				"UnaryAssign : Variable name, Token operator",
				"Call        : Expr callee, Token parenthesis, ArrayList<Expr> arguments",
				"Sharp       : Expr name"));

		defineAst(outputDir, "Stmt", Arrays.asList(
				"Keyword    : Token word",
				"Return		: Expr value",
				"Expression : Expr expression",
				"ExPrint    : Expr expression",
			    "Print      : Expr expression",
			    "Declare    : Token type, Token name, Expr value",
			    "Block      : ArrayList<Stmt> statements",
			    "While      : Expr condition, Block body",
			    "Do         : Expr condition, Block body",
			    "For        : Declare declaration, Expr condition, Expr incrementor, Block body",
			    "Switch     : ArrayList<Expr> controls, ArrayList<Case> cases, Block defaultBody",
			    "Case       : ArrayList<Expr> tests, Block body",
			    "Class		: Token name, ArrayList<Function> methods, ArrayList<Declare> fields",
			    "If         : Expr condition, Block ifBody, Block elseBody",
			    "Undec      : ArrayList<Expr.Variable> variables",
			    "Struct     : Sculpture sculpture, Mould mould",
			    "Sculpture  : ArrayList<Object> sculpture",
			    "Mould      : ArrayList<Placeholder> placeholders, Block body",
			    "Function   : Token name, ArrayList<Token> types, ArrayList<Token> parameters, Block body"));
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
		writer.println("public abstract class " + baseName + " implements Cloneable {");

		defineVisitor(writer, baseName, types);

		for (String type : types) {
			String[] elements = type.split(":");
			String className = elements[0].trim();
			String fields = elements[1].trim();
			String equalsFields = elements.length < 3 ? null : elements[2];
			defineType(writer, baseName, className, fields, equalsFields);
		}

		writer.println();
		writer.println("\tabstract <T> T accept(Visitor<T> visitor);");
		writer.println("\tabstract String toString(int i);");
		writer.println("\tabstract " + baseName + " mouldClone();");
		writer.println();
		
		writer.println("\tpublic String toString() {");
		writer.println("\t\treturn this.toString(0);");
		writer.println("\t}");
		writer.println();
	
		//arraylist toString method
		writer.println("\tpublic static String arrayListToString(ArrayList<?> list) {");
		writer.println("\t\tString str = \"\";");
		writer.println();
		writer.println("\t\tfor(int i = 0; i < list.size(); i++) {");
		writer.println("\t\t\tstr = str + \"\\n\" + list.get(i);");
		writer.println("\t\t}");
		writer.println();
		writer.println("\t\treturn str;");
		writer.println("\t}");
		writer.println();
		
		
		//arraylist clone method
		writer.println("\tpublic static <T> ArrayList<T> arrayListClone(ArrayList<T> list) {");
		writer.println("\t\tif(list == null) { ");
		writer.println("\t\t\treturn null;");
		writer.println("\t\t}");
		writer.println();
		writer.println("\t\tArrayList<T> newList = new ArrayList<T>();");
		writer.println();
		writer.println("\t\tfor(int i = 0; i < list.size(); i++) {");
		writer.println("\t\t\tT obj = list.get(i);");
		writer.println();
		writer.println("\t\t\tif(obj instanceof Expr) {");
		writer.println("\t\t\t\tnewList.add((T) ((Expr) obj).mouldClone());");
		writer.println("\t\t\t} else if (obj instanceof Stmt){");
		writer.println("\t\t\t\tnewList.add((T) ((Stmt) obj).mouldClone());");
		writer.println("\t\t\t} else {");
		writer.println("\t\t\t\tnewList.add(obj);");
		writer.println("\t\t\t}");
		writer.println("\t\t}");
		writer.println();
		writer.println("\t\treturn newList;");
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
	 * @param equalsFieldList the fields of the subclass to be used for the equals method
	 */
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList, String equalsFieldList) {
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
		defineClone(writer, fieldList, className);

		writer.println();
		writer.println("\t\t<T> T accept(Visitor<T> visitor) {");
		writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
		writer.println("\t\t}");

		// Fields.
		writer.println();
		for (String field : fields) {
			writer.println("\t\tpublic final " + field + ";");
		}
		
		writer.println();
		
		if(className.equals("Variable")) {
			writer.println("\t\t@Override");
			writer.println("\t\tpublic boolean equals(Object o) {");
			writer.println("\t\t\tif (o instanceof Variable) {");
			writer.println("\t\t\t\tif (((Variable) o).name.equals(name)) {");
			writer.println("\t\t\t\t\treturn true;");
			writer.println("\t\t\t\t}");
			writer.println("\t\t\t}");
			writer.println();
			writer.println("\t\t\treturn false;");
			writer.println("\t\t}");
			writer.println();
			writer.println("\t\tpublic int hashCode() {");
			writer.println("\t\t\treturn name.hashCode();");
			writer.println("\t\t}");
		}
		
		if(className.equals("Function")) {
			writer.println("\t\t@Override");
			writer.println("\t\tpublic boolean equals(Object o) {");
			writer.println("\t\t\tif(o instanceof Function) {");
			writer.println("\t\t\t\tFunction f = (Function) o;");
			writer.println("\t\t\t\tif(f.name.lexeme.equals(name.lexeme)) {");
			writer.println("\t\t\t\t\tif(types.size() != f.types.size())");
			writer.println("\t\t\t\t\t\treturn false;");
			writer.println("\t\t\t\t\tfor(int i = 0; i < types.size(); i++) {");
			writer.println("\t\t\t\t\t\tif(!types.get(i).lexeme.equals(f.types.get(i).lexeme))");
			writer.println("\t\t\t\t\t\t\treturn false;");
			writer.println("\t\t\t\t\t}");
			writer.println();
			writer.println("\t\t\t\t\treturn true;");
			writer.println("\t\t\t\t}");
			writer.println("\t\t\t}");
			writer.println();
			writer.println("\t\t\treturn false;");
			writer.println("\t\t}");
			writer.println();
			writer.println("\t\tpublic int hashCode() {");
			writer.println("\t\t\treturn name.lexeme.hashCode();");
			writer.println("\t\t}");
		}

		writer.println("\t}");
		writer.println();
	}
	
	public static void defineClone(PrintWriter writer, String fieldList, String className) {
		
		String[] fields = fieldList.split(", ");
		String newFieldList = "";
		
		for(String field : fields) {
			String[] str = field.split(" ");
			
			if(str[0].startsWith("Stmt") || str[0].startsWith("Expr")) {
				newFieldList = newFieldList + ", " + str[1] + ".mouldClone()";
			} else if (str[0].startsWith("ArrayList")){
				newFieldList = newFieldList + ", arrayListClone(" + str[1] + ")";
			} else {
				newFieldList = newFieldList + ", " + str[1];
			}
		}
		
		newFieldList = newFieldList.substring(2);
		
		writer.println();
		writer.println("\t\tpublic " + className + " mouldClone() {");
		writer.println("\t\t\treturn new " + className + "(" + newFieldList + ");");
		writer.println("\t\t}");
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
	
	private static void defineEquals(PrintWriter writer, String className, String fields) {
		
		String[] fieldsArr = fields.split(" ");
		
		writer.println("\t\t@Override");
		writer.println("\t\tpublic boolean equals(Object o) {");
		writer.println("\t\t\tif(o instanceof " + className + ") {");
		writer.println("\t\t\t\t" + className + " var = (" + className + ") o;");
		
		for(String field : fieldsArr) {
			writer.println("if(var");
		}
		
		writer.println("\t\t\t}");
		writer.println();
		writer.println("\t\t\treturn false;");
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