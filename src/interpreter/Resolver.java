package interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import interpreter.Expr.Assign;
import interpreter.Expr.Binary;
import interpreter.Expr.Call;
import interpreter.Expr.Conditional;
import interpreter.Expr.Get;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Logical;
import interpreter.Expr.Set;
import interpreter.Expr.Sharp;
import interpreter.Expr.Unary;
import interpreter.Expr.UnaryAssign;
import interpreter.Expr.Variable;
import interpreter.Stmt.Block;
import interpreter.Stmt.Case;
import interpreter.Stmt.Class;
import interpreter.Stmt.Declare;
import interpreter.Stmt.Do;
import interpreter.Stmt.ExPrint;
import interpreter.Stmt.Expression;
import interpreter.Stmt.For;
import interpreter.Stmt.Function;
import interpreter.Stmt.If;
import interpreter.Stmt.Keyword;
import interpreter.Stmt.Mould;
import interpreter.Stmt.Print;
import interpreter.Stmt.Return;
import interpreter.Stmt.Sculpture;
import interpreter.Stmt.Struct;
import interpreter.Stmt.Switch;
import interpreter.Stmt.Undec;
import interpreter.Stmt.While;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();

	private FunctionType currentFunction = FunctionType.NONE;

	private enum FunctionType {
		NONE, FUNCTION, CONTRUCTOR, METHOD
	}

	public Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public void resolve(List<? extends Stmt> statements) {
		for (int i = 0; i < statements.size(); i++) {
			resolve((Stmt) statements.get(i));
		}
	}

	private void resolve(Stmt stmt) {
		if (stmt == null)
			return;

		stmt.accept(this);
	}

	public void resolveExpressions(List<? extends Expr> expressions) {
		for (int i = 0; i < expressions.size(); i++) {
			resolve((Expr) expressions.get(i));
		}
	}

	private void resolve(Expr expr) {
		if (expr == null)
			return;

		expr.accept(this);
	}

	private void declare(Token name) {
		if (scopes.isEmpty())
			return;

		Map<String, Boolean> scope = scopes.peek();

		if (scope.containsKey(name.lexeme)) {
			Newt.error(name, "Variable with this name already declared in this scope.");
		}

		scope.put(name.lexeme, false);
	}

	private void define(Token name) {
		if (scopes.isEmpty())
			return;
		scopes.peek().put(name.lexeme, true);
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}

		// Not found. Assume it is global.
	}

	private void resolveFunction(Stmt.Function function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for (Token param : function.parameters) {
			declare(param);
			define(param);
		}
		
		//the body expression is skipped over because it would create a second scope for the function
		resolve(function.body.statements);
		endScope();

		currentFunction = enclosingFunction;
	}

	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}

	@Override
	public Void visitKeywordStmt(Keyword stmt) {
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		if (stmt.value != null) {
			resolve(stmt.value);
		}

		if (currentFunction == FunctionType.NONE) {
			Newt.error(null, "Cannot return from top-level code.");
		}

		return null;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitExPrintStmt(ExPrint stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitDeclareStmt(Declare stmt) {
		declare(stmt.name);
		if (stmt.value != null) {
			resolve(stmt.value);
		}
		define(stmt.name);
		return null;
	}

	@Override
	public Void visitBlockStmt(Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitDoStmt(Do stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitForStmt(For stmt) {
		beginScope();
		resolve(stmt.declaration);
		resolve(stmt.condition);
		resolve(stmt.incrementor);
		resolve(stmt.body);
		endScope();
		return null;
	}

	@Override
	public Void visitSwitchStmt(Switch stmt) {
		resolveExpressions(stmt.controls);
		resolve(stmt.cases);
		resolve(stmt.defaultBody);
		return null;
	}

	@Override
	public Void visitCaseStmt(Case stmt) {
		resolve(stmt.body);
		resolveExpressions(stmt.tests);
		return null;
	}

	@Override
	public Void visitClassStmt(Class stmt) {
		declare(stmt.name);

		for (Stmt.Function method : stmt.methods) {
			FunctionType declaration = FunctionType.METHOD;
			resolveFunction(method, declaration);
		}

		define(stmt.name);
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
		resolve(stmt.condition);
		resolve(stmt.ifBody);
		if (stmt.elseBody != null)
			resolve(stmt.elseBody);
		return null;
	}

	@Override
	public Void visitUndecStmt(Undec stmt) {
		resolveExpressions(stmt.variables);
		return null;
	}

	@Override
	public Void visitStructStmt(Struct stmt) {
		resolve(stmt.mould);
		resolve(stmt.sculpture);
		return null;
	}

	@Override
	public Void visitSculptureStmt(Sculpture stmt) {
		return null;
	}

	@Override
	public Void visitMouldStmt(Mould stmt) {
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitFunctionStmt(Function stmt) {
		declare(stmt.name);
		define(stmt.name);

		resolveFunction(stmt, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visitConditionalExpr(Conditional expr) {
		resolve(expr.condition);
		resolve(expr.first);
		resolve(expr.second);
		return null;
	}

	@Override
	public Void visitBinaryExpr(Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitLogicalExpr(Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitGroupingExpr(Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpr(Literal expr) {
		return null;
	}

	@Override
	public Void visitGetExpr(Get expr) {
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitSetExpr(Set expr) {
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Unary expr) {
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitVariableExpr(Variable expr) {
		if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
			Newt.error(expr.name, "Cannot read local variable in its own initializer.");
		}

		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitAssignExpr(Assign expr) {
		resolve(expr.value);
		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitUnaryAssignExpr(UnaryAssign expr) {
		resolve(expr.name);
		return null;
	}

	@Override
	public Void visitCallExpr(Call expr) {
		resolve(expr.callee);

		for (Expr argument : expr.arguments) {
			resolve(argument);
		}

		return null;
	}

	@Override
	public Void visitSharpExpr(Sharp expr) {
		resolve(expr.name);
		return null;
	}
}