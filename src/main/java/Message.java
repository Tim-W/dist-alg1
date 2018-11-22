import java.rmi.RemoteException;

public class Message implements Runnable {
    public final OrderingBuffer Sm;
    private final Clock clock;
    public final String message;
    private final int startDelay;
    private final int arrivalDelay;
    private final Node destination;
    private final Process process;
    public final Timestamp timestamp;

    public Message(String message, OrderingBuffer Sm, Clock clock, int startDelay, int arrivalDelay, Node destination, Process process, Timestamp timestamp) {
        this.Sm = Sm;
        this.message = message;
        this.clock = clock;
        this.startDelay = startDelay;
        this.arrivalDelay = arrivalDelay;
        this.destination = destination;
        this.process = process;
        this.timestamp = timestamp;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.startDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.clock.increment();
        this.process.getS().put(this.process.getId(), (VectorTimestamp) this.process.getClock().stamp());

        Timestamp stamp = this.clock.stamp();

        System.out.println("Send: " + this.message + ", " + this.clock.stamp() + ", " + this.Sm.buffer.toString());

        try {
            Thread.sleep(this.arrivalDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            destination.receiveMessage(this.message, this.Sm, stamp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
