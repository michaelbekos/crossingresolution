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

  public GeneticAlgorithm(Function<T, T> adv, Comparator<T> sf, Maybe<Integer> desiredIC, Either<Function<T, T>, Function<List<T>, T>> gen){
    advance = adv;
    scoring = sf;
    desiredInstanceCount = desiredIC;
    desiredInstanceCount.andThen(di -> instances = new ArrayList<>(di));
    generator = gen.match(
      left -> (l -> {
        int listElemIndex = rand.nextInt(l.size());
        T listElem = l.get(listElemIndex);
        return left.apply(listElem);
      }), 
      right -> right);
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
        newInstance();
      }
    });
    if(instances.isEmpty()){
      throw new IllegalStateException();
    }
  }
  public void newInstance(){
    T newInstance = generator.apply(instances);
    for(int i = 0; i < 10; i++){
      newInstance = advance.apply(newInstance);
    }
    instances.add(newInstance);
  }

  public void nextGeneration(){
    assertInstances();
    Collections.sort(instances, scoring);
    // instances is not empty, since (assertInstances)
    for(int i = 0; i < 2; i++){
      instances.remove(0);
      newInstance();  
    }
  }
  private void notifyChanged(){
    bestChanged.andThen(f -> {
      Collections.sort(instances, scoring);
      f.accept(instances.get(instances.size() - 1));
    });
  }
  public void runRounds(int count){
    running = true;
    for(int i = 0; i < count && running; i++){
      iterate();
    } 
  }
  public void iterate(){
    runRound();
    nextGeneration();
    notifyChanged();
  }
  public void runIndefinitely(){
    running = true;
    while(running){
      iterate();
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