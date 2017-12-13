package event;

/**
 * This class corresponds to an event produced by an algorithm, which has a certain status within [0,100].
 *
 * @author Michael A. Bekos
 */
public class AlgorithmEvent extends java.util.EventObject
{
    private int currentStatus;

    /**
     * Constructor for objects of type AlgorithmEvent
     */
    public AlgorithmEvent(Object source, int currentStatus)
    {
        super(source);
        this.currentStatus = currentStatus;
    }

    /**
     * Returns current status within [0,100].
     */
    public synchronized int currentStatus()
    {
        return this.currentStatus;
    }

    /**
     * Sets current status within [0,100].
     */
    public synchronized void currentStatus(int currentStatus)
    {
        this.currentStatus = currentStatus;
    }


}
