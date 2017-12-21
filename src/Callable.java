import java.util.List;

interface Callable {
  Object call(Parser parser, List<Object> arguments);
  int arity();
}