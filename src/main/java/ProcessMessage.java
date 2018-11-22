public class ProcessMessage {
    public final String message;
    public final int destination;
    public final int startDelay;
    public final int arrivalDelay;

    public ProcessMessage(String message, int startDelay, int arrivalDelay, int destination) {
        this.message = message;
        this.startDelay = startDelay;
        this.arrivalDelay = arrivalDelay;
        this.destination = destination;
    }
}
