package interpreter;

import java.util.ArrayList;

public abstract class Stmt implements Cloneable {
	interface Visitor<T> {
		T visitKeywordStmt(Keyword stmt);
		T visitExpressionStmt(Expression stmt);
		T visitExPrintStmt(ExPrint stmt);
		T visitPrintStmt(Print stmt);
		T visitDeclareStmt(Declare stmt);
		T visitBlockStmt(Block stmt);
		T visitWhileStmt(While stmt);
		T visitDoStmt(Do stmt);
		T visitForStmt(For stmt);
		T visitSwitchStmt(Switch stmt);
		T visitCaseStmt(Case stmt);
		T visitIfStmt(If stmt);
		T visitUndecStmt(Undec stmt);
		T visitStructStmt(Struct stmt);
		T visitSculptureStmt(Sculpture stmt);
		T visitMouldStmt(Mould stmt);
		T visitFunctionStmt(Function stmt);
	}

	public static class Keyword extends Stmt {
		public Keyword(Token word) {
			this.word = word;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + word.lexeme;
		}

		public Keyword mouldClone() {
			return new Keyword(word);
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitKeywordStmt(this);
		}

		public final Token word;
	}

	public static class Expression extends Stmt {
		public Expression(Expr expression) {
			this.expression = expression;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + expression.toString(depth + 1);
		}

		public Expression mouldClone() {
			return new Expression(expression.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		public final Expr expression;
	}

	public static class ExPrint extends Stmt {
		public ExPrint(Expr expression) {
			this.expression = expression;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + expression.toString(depth + 1);
		}

		public ExPrint mouldClone() {
			return new ExPrint(expression.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitExPrintStmt(this);
		}

		public final Expr expression;
	}

	public static class Print extends Stmt {
		public Print(Expr expression) {
			this.expression = expression;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + expression.toString(depth + 1);
		}

		public Print mouldClone() {
			return new Print(expression.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitPrintStmt(this);
		}

		public final Expr expression;
	}

	public static class Declare extends Stmt {
		public Declare(Token type, Token name, Expr value) {
			this.type = type;
			this.name = name;
			this.value = value;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + name.lexeme + "\n" + type.lexeme + "\n" + value.toString(depth + 1);
		}

		public Declare mouldClone() {
			return new Declare(type, name, value.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitDeclareStmt(this);
		}

		public final Token type;
		public final Token name;
		public final Expr value;
	}

	public static class Block extends Stmt {
		public Block(ArrayList<Stmt> statements) {
			this.statements = statements;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(statements);
		}

		public Block mouldClone() {
			return new Block(arrayListClone(statements));
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitBlockStmt(this);
		}

		public final ArrayList<Stmt> statements;
	}

	public static class While extends Stmt {
		public While(Expr condition, Stmt block) {
			this.condition = condition;
			this.block = block;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + condition.toString(depth + 1) + "\n" + block.toString(depth + 1);
		}

		public While mouldClone() {
			return new While(condition.mouldClone(), block.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitWhileStmt(this);
		}

		public final Expr condition;
		public final Stmt block;
	}

	public static class Do extends Stmt {
		public Do(Expr condition, Stmt block) {
			this.condition = condition;
			this.block = block;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + condition.toString(depth + 1) + "\n" + block.toString(depth + 1);
		}

		public Do mouldClone() {
			return new Do(condition.mouldClone(), block.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitDoStmt(this);
		}

		public final Expr condition;
		public final Stmt block;
	}

	public static class For extends Stmt {
		public For(Stmt declaration, Expr condition, Expr incrementor, Stmt block) {
			this.declaration = declaration;
			this.condition = condition;
			this.incrementor = incrementor;
			this.block = block;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + declaration.toString(depth + 1) + "\n" + condition.toString(depth + 1) + "\n" + incrementor.toString(depth + 1) + "\n" + block.toString(depth + 1);
		}

		public For mouldClone() {
			return new For(declaration.mouldClone(), condition.mouldClone(), incrementor.mouldClone(), block.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitForStmt(this);
		}

		public final Stmt declaration;
		public final Expr condition;
		public final Expr incrementor;
		public final Stmt block;
	}

	public static class Switch extends Stmt {
		public Switch(ArrayList<Expr> controls, ArrayList<Stmt.Case> cases, Stmt defaultCase) {
			this.controls = controls;
			this.cases = cases;
			this.defaultCase = defaultCase;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(controls) + arrayListToString(cases) + defaultCase.toString(depth + 1);
		}

		public Switch mouldClone() {
			return new Switch(arrayListClone(controls), arrayListClone(cases), defaultCase.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitSwitchStmt(this);
		}

		public final ArrayList<Expr> controls;
		public final ArrayList<Stmt.Case> cases;
		public final Stmt defaultCase;
	}

	public static class Case extends Stmt {
		public Case(ArrayList<Expr> tests, Stmt block) {
			this.tests = tests;
			this.block = block;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(tests) + block.toString(depth + 1);
		}

		public Case mouldClone() {
			return new Case(arrayListClone(tests), block.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitCaseStmt(this);
		}

		public final ArrayList<Expr> tests;
		public final Stmt block;
	}

	public static class If extends Stmt {
		public If(Expr condition, Stmt ifBlock, Stmt elseBlock) {
			this.condition = condition;
			this.ifBlock = ifBlock;
			this.elseBlock = elseBlock;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + condition.toString(depth + 1) + "\n" + ifBlock.toString(depth + 1) + "\n" + elseBlock.toString(depth + 1);
		}

		public If mouldClone() {
			return new If(condition.mouldClone(), ifBlock.mouldClone(), elseBlock.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitIfStmt(this);
		}

		public final Expr condition;
		public final Stmt ifBlock;
		public final Stmt elseBlock;
	}

	public static class Undec extends Stmt {
		public Undec(ArrayList<Expr> variables) {
			this.variables = variables;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(variables);
		}

		public Undec mouldClone() {
			return new Undec(arrayListClone(variables));
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitUndecStmt(this);
		}

		public final ArrayList<Expr> variables;
	}

	public static class Struct extends Stmt {
		public Struct(Stmt.Sculpture sculpture, Stmt.Mould mould) {
			this.sculpture = sculpture;
			this.mould = mould;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + sculpture.toString(depth + 1) + "\n" + mould.toString(depth + 1);
		}

		public Struct mouldClone() {
			return new Struct(sculpture.mouldClone(), mould.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitStructStmt(this);
		}

		public final Stmt.Sculpture sculpture;
		public final Stmt.Mould mould;
	}

	public static class Sculpture extends Stmt {
		public Sculpture(ArrayList<Object> sculpture) {
			this.sculpture = sculpture;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(sculpture);
		}

		public Sculpture mouldClone() {
			return new Sculpture(arrayListClone(sculpture));
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitSculptureStmt(this);
		}

		public final ArrayList<Object> sculpture;
	}

	public static class Mould extends Stmt {
		public Mould(ArrayList<Placeholder> placeholders, Stmt.Block mould) {
			this.placeholders = placeholders;
			this.mould = mould;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + arrayListToString(placeholders) + mould.toString(depth + 1);
		}

		public Mould mouldClone() {
			return new Mould(arrayListClone(placeholders), mould.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitMouldStmt(this);
		}

		public final ArrayList<Placeholder> placeholders;
		public final Stmt.Block mould;
	}

	public static class Function extends Stmt {
		public Function(Token name, ArrayList<Token> types, ArrayList<Token> parameters, Stmt.Block block) {
			this.name = name;
			this.types = types;
			this.parameters = parameters;
			this.block = block;
		}

		public String toString(int depth) {
			String str = "";
			for(int i = 0; i < depth; i++) {
				str = str + "   ";
			}

			return str + name.lexeme + "\n" + arrayListToString(types) + arrayListToString(parameters) + block.toString(depth + 1);
		}

		public Function mouldClone() {
			return new Function(name, arrayListClone(types), arrayListClone(parameters), block.mouldClone());
		}

		<T> T accept(Visitor<T> visitor) {
			return visitor.visitFunctionStmt(this);
		}

		public final Token name;
		public final ArrayList<Token> types;
		public final ArrayList<Token> parameters;
		public final Stmt.Block block;
	}


	abstract <T> T accept(Visitor<T> visitor);
	abstract String toString(int i);
	abstract Stmt mouldClone();

	public String toString() {
		return this.toString(0);
	}

	public static String arrayListToString(ArrayList<?> list) {
		String str = "";

		for(int i = 0; i < list.size(); i++) {
			str = str + "\n" + list.get(i);
		}

		return str;
	}

	public static <T> ArrayList<T> arrayListClone(ArrayList<T> list) {
		if(list == null) { 
			return null;
		}

		ArrayList<T> newList = new ArrayList<T>();

		for(int i = 0; i < list.size(); i++) {
			T obj = list.get(i);

			if(obj instanceof Expr) {
				newList.add((T) ((Expr) obj).mouldClone());
			} else if (obj instanceof Stmt){
				newList.add((T) ((Stmt) obj).mouldClone());
			} else {
				newList.add(obj);
			}
		}

		return newList;
	}
}
