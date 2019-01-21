package newt_metatypes;

import java.util.ArrayList;

import interpreter.Token;
import parser.ParseError;

/**
 * This class is a wrapper for the NewtFunction class, designed to allow function overloading.
 * Wherever a NewtFunction would be stored, a NewtFunctionGroup will replace it. When multiple functions with the same
 * name are declared in the same scope, they will be put into the same Group. When a function is called, the Group object
 * will be retrieved an searched for the function with the correct signature.
 * @author Jared
 */
public class NewtFunctionGroup {
	
	ArrayList<NewtFunction> functions;
	
	public NewtFunctionGroup(ArrayList<NewtFunction> functions) {
		this.functions = functions;
	}
	
	public void overload(NewtFunction function) {
		if(findSignature(function.getParams()) != null) {
			throw new ParseError();
		}
		
		functions.add(function);
	}
	
	public NewtFunction findSignature(ArrayList<Token> params) {
		for(NewtFunction function : functions) {
			if(compareSignature(params, function.getParams())) {
				return function;
			}
		}
		
		return null;
	}
	
	public boolean compareSignature(ArrayList<Token> params, ArrayList<Token> functionParams) {
		if(functionParams.size() == params.size()) {
			for(int i = 0; i < params.size(); i++) {
				if(!params.get(i).lexeme.equals(functionParams.get(i).lexeme)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public NewtFunctionGroup bind(NewtInstance instance) {
		NewtFunctionGroup group = new NewtFunctionGroup(null);
		
		for(NewtFunction function : functions) {
			group.overload(function.bind(instance));
		}
		
		return group;
	}
}
