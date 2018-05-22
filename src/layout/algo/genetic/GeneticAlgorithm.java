package layout.algo.genetic;

import java.util.*;
import java.util.stream.Collectors;

public class GeneticAlgorithm<T> {
  private IObjective<T> objective;
  private List<T> individuals;
  private int desiredInstanceCount;
  private Random rand;

  public GeneticAlgorithm(IObjective<T> objective, Collection<T> firstIndividuals, int desiredIndividuals, Random rand) {
    this.objective = objective;
    this.individuals = new ArrayList<>(desiredInstanceCount);
    this.individuals.addAll(firstIndividuals);
    this.desiredInstanceCount = desiredIndividuals;
    this.rand = rand;
  }

  public Optional<T> getBestIndividual() {
    return individuals.stream()
        .min(objective);
  }

  public void iterate() {
    runRound();
    nextGeneration();
  }

  private void runRound() {
    assertInstances();
    individuals = individuals
        .parallelStream()
        .map(objective::advance)
        .collect(Collectors.toList());
  }

  /**
   * Produce individuals until at least desiredInstanceCount is alive. Check if at least one instance exists.
   */
  private void assertInstances() {
    if (desiredInstanceCount > 0) {
      while (individuals.size() < desiredInstanceCount) {
        newInstance();
      }
    }
    if (individuals.isEmpty()) {
      throw new IllegalStateException();
    }
  }

  /**
   * Spawn a new instance.
   */
  private void newInstance() {
    T newInstance = generate();
    for (int i = 0; i < 10; i++) {
      newInstance = objective.advance(newInstance);
    }
    individuals.add(newInstance);
  }

  private T generate() {
    int listElemIndex = rand.nextInt(individuals.size());
    T individual = individuals.get(listElemIndex);
    return objective.mutate(individual);
  }


  /**
   * do
   * check if everything's ok
   * kill the worst 2 individuals
   * spawn 2 new individuals
   */
  private void nextGeneration() {
    assertInstances();
    individuals.sort(objective);
    for (int i = 0; i < 2; i++) {
      individuals.remove(0);
      newInstance();
    }
  }
}