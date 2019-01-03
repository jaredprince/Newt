package parser;
import java.util.List;

/**
 * The Callable class is an object which will hold
 * @author Jared
 *
 */
public interface Callable {
  TypedObject call(Environment environment, List<TypedObject> arguments);
  int arity(); /* number of arguments */
}