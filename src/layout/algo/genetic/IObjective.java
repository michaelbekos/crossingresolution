package layout.algo.genetic;

import java.util.Comparator;

public interface IObjective<T> extends Comparator<T> {
  T advance(T individual);
  T mutate(T individual);
}
