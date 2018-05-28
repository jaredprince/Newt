package parser;
import java.util.List;

public interface Callable {
  TypedObject call(Environment environment, List<TypedObject> arguments);
  int arity(); /* number of arguments */
}