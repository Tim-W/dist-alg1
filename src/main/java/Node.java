import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a node in the network that can receive and send messages.
 */
public interface Node extends Remote {

    void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException;
}
