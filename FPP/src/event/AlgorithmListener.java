package event;

/**
 * This is an listener interface for an algorithm, support three methods:
 * -  algorithmStarted
 * -  algorithmFinished
 * -  algorithmStateChanged
 * @author Michael A. Bekos
 */
public interface AlgorithmListener
{
    /**
     * Algorithm Started
     */
    void algorithmStarted(event.AlgorithmEvent evt);

    /**
     * Algorithm Finished
     */
    void algorithmFinished(event.AlgorithmEvent evt);

    /**
     * Returns the current status of the algorithm.
     */
    void algorithmStateChanged(event.AlgorithmEvent evt);
}
