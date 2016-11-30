package layout.algo;

import util.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import javax.swing.*;


public class GeneticAlgorithm<T> implements Runnable {
  public static Random rand = new Random();

  public List<T> instances = new LinkedList<T>();
  public Maybe<Integer> desiredInstanceCount;
  Comparator<T> scoring;
  Function<List<T>, T> generator;
  Function<T, T> advance;
  public boolean running = false;
  public Maybe<Consumer<T>> bestChanged = Maybe.nothing();

  public static <T> GeneticAlgorithm<T> newGeneticAlgorithm_ListGen(Function<T, T> adv, Comparator<T> sf, Function<List<T>, T> gen){
    return new GeneticAlgorithm<>(adv, sf, Maybe.nothing(), gen);
  }
  public static <T> GeneticAlgorithm<T> newGeneticAlgorithm_FunGen(Function<T, T> adv, Comparator<T> sf, Function<T, T> gen){
    return newGeneticAlgorithm_FunGen(adv, sf, Maybe.nothing(), gen);
  }
  public static <T> GeneticAlgorithm<T> newGeneticAlgorithm_FunGen(Function<T, T> adv, Comparator<T> sf, Integer desiredIC, Function<T, T> gen){
    GeneticAlgorithm<T> ga = newGeneticAlgorithm_FunGen(adv, sf, Maybe.just(desiredIC), gen);
    ga.instances = new ArrayList<>(desiredIC);
    return ga;
  }
  public static <T> GeneticAlgorithm<T> newGeneticAlgorithm_FunGen(Function<T, T> adv, Comparator<T> sf, Maybe<Integer> desiredIC, Function<T, T> gen){
    Function<List<T>, T> genList = (l -> {
      int listElemIndex = rand.nextInt(l.size());
      T listElem = l.get(listElemIndex);
      return gen.apply(listElem);
    });
    return new GeneticAlgorithm<>(adv, sf, desiredIC, genList);
  }
  public static <T> GeneticAlgorithm<T> newGeneticAlgorithm_ListGen(Function<T, T> adv, Comparator<T> sf, Integer desiredIC, Function<List<T>, T> gen){
    GeneticAlgorithm<T> ga = new GeneticAlgorithm<>(adv, sf, Maybe.just(desiredIC), gen);
    ga.instances = new ArrayList<>(desiredIC);
    return ga;
  }

  GeneticAlgorithm(Function<T, T> adv, Comparator<T> sf, Maybe<Integer> desiredIC, Function<List<T>, T> gen){
    advance = adv;
    scoring = sf;
    desiredInstanceCount = desiredIC;
    generator = gen;
  }

  public void runRound(){
    assertInstances();
    instances = instances
      .parallelStream()
      .map(i -> advance.apply(i))
      .collect(Collectors.toList());
  }
  public void assertInstances(){
    desiredInstanceCount.andThen(ic -> {
      while(instances.size() < ic){
        instances.add(generator.apply(instances));
      }
    });
    if(instances.isEmpty()){
      throw new IllegalStateException();
    }
  }
  public void nextGeneration(){
    assertInstances();
    Collections.sort(instances, scoring);
    // instances is not empty, since (assertInstances)
    instances.remove(0);
    T newInstance = generator.apply(instances);
    instances.add(newInstance);
  }
  private void notifyChanged(){
    bestChanged.andThen(f -> {
      Collections.sort(instances, scoring);
      f.accept(instances.get(instances.size() - 1));
    });
  }
  public void runIndefinitely(){
    running = true;
    while(running){
      runRound();
      nextGeneration();
      notifyChanged();
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {
        System.out.println("Sleep interrupted!");
        //Do nothing...
      }
      
    }
  }
  public void run(){
    runIndefinitely();
  }
}