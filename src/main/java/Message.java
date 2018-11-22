import java.rmi.RemoteException;

public class Message implements Runnable {
    public OrderingBuffer Sm;
    private final Clock clock;
    public final String message;
    private final int startDelay;
    private final int arrivalDelay;
    private final Node destination;
    private final Process process;
    public final Timestamp timestamp;
    private final Id destinationId;

    public Message(String message, OrderingBuffer Sm, Clock clock, int startDelay, int arrivalDelay, Node destination, Process process, Timestamp timestamp, Id destinationId) {
        this.Sm = Sm;
        this.message = message;
        this.clock = clock;
        this.startDelay = startDelay;
        this.arrivalDelay = arrivalDelay;
        this.destination = destination;
        this.process = process;
        this.timestamp = timestamp;
        this.destinationId = destinationId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.startDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.clock.increment();

        Timestamp stamp = this.clock.stamp();
        System.out.println("Send: " + this.message + ", " + this.Sm.buffer.toString() + ", " + stamp);

        this.Sm.put(this.destinationId, (VectorTimestamp) stamp);
        OrderingBuffer SmCopy = this.Sm.copy();

        try {
            Thread.sleep(this.arrivalDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            destination.receiveMessage(this.message, SmCopy, stamp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
