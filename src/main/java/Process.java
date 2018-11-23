import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Process implements Node, Runnable {
    private final Id id;
    private final Clock clock;
    private final ArrayList<Message> B;
    private final OrderingBuffer S;
    private final Registry registry;
    private List<ProcessMessage> outbox;
    private List<Message> delivered = new ArrayList<>();

    public Process(int id, int networkSize) throws RemoteException, AlreadyBoundException {
        this.id = new Id(id);
        this.S = new OrderingBuffer();
        this.clock = new VectorClock(id - 1, networkSize);
        this.B = new ArrayList<>();

        Node stub = (Node) UnicastRemoteObject.exportObject(this, 0);
        this.registry = LocateRegistry.getRegistry("localhost", 1099);
        registry.bind(this.id.toString(), stub);
    }

    public void run() {
        // Bind the remote object's stub in the registry
        System.err.println(this.id.toString() + ".run()");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (outbox != null && outbox.size() > 0) {
            ProcessMessage outgoingMessage = outbox.get(0);
            outbox.remove(0);

            try {
                Node destination = (Node) this.registry.lookup(outgoingMessage.destination + "");

                Message message = new Message(outgoingMessage.message, S, clock,
                        outgoingMessage.startDelay, outgoingMessage.arrivalDelay, destination, clock.stamp(), new Id(outgoingMessage.destination));
                new Thread(message).start();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.flush();
        try {
            this.unbind();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void unbind() throws RemoteException, NotBoundException {
        registry.unbind(this.id.toString());
    }

    @Override
    public void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException {
        System.out.println("Receive: " + message + ", " + Sm.buffer.toString() + ", " + Vm.toString());
        Message simplifiedMessage = new Message(message, Sm, null, 0, 0, null, Vm, new Id(0));
        B.add(simplifiedMessage);
        if (canDeliver(Sm)) {
            deliver(simplifiedMessage);
        }
    }

    public void setOutbox(List<ProcessMessage> outgoing) {
        this.outbox = outgoing;
    }

    public void deliver(Message message) throws RemoteException {
        B.remove(message);
        clock.update(message.timestamp);
        clock.increment();
        S.merge(message.Sm);
        delivered.add(message);
        System.out.println("Deliver: " + message.message + ", " + S.buffer.toString() + ", " + message.timestamp.toString());
        List<Message> BCopy = new ArrayList<>(B);
        for (Message m : BCopy) {
            if (canDeliver(m.Sm)) {
                deliver(m);
            }
        }

    }

    private boolean canDeliver(OrderingBuffer Sm) {
        if (Sm.contains(id)) {
            return Sm.get(id).leq(clock.stamp());
        }
        return true;
    }

    void flush() {
        for (int i = 0; i < this.delivered.size() - 1; i++) {
            VectorTimestamp prev = (VectorTimestamp) this.delivered.get(i).timestamp;
            VectorTimestamp next = (VectorTimestamp) this.delivered.get(i + 1).timestamp;
            if (prev.gt(next)) {
                System.out.println("Timestamp " + prev + " is not less than or concurrent with " + next);
                System.out.println("Causal ordering failed in Process " + id + " :(");
                return;
            }
        }
        System.out.println("Message order correct in Process " + id + "! :D (" + delivered.size() + " messages)");
    }
}
