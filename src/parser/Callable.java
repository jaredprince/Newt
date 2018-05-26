package parser;
import java.util.List;

public interface Callable {
  TypedObject call(Parser parser, List<TypedObject> arguments);
  int arity(); /* number of arguments */
}