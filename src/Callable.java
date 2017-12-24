import java.util.List;

interface Callable {
  TypedObject call(Parser parser, List<TypedObject> arguments);
  int arity();
}