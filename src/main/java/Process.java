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

    public Process(int id, int networkSize) throws RemoteException, AlreadyBoundException {
        this.id = new Id(id);
        this.S = new OrderingBuffer();
        this.clock = new VectorClock(id - 1, networkSize);
        this.B = new ArrayList<>();

        Node stub = (Node) UnicastRemoteObject.exportObject(this, 0);
        this.registry = LocateRegistry.getRegistry("localhost", 1099);
        registry.bind(this.id.toString(), stub);
    }

    public Clock getClock() {
        return clock;
    }

    public OrderingBuffer getS() {
        return S;
    }

    public Id getId() {
        return id;
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
                        outgoingMessage.startDelay, outgoingMessage.arrivalDelay, destination, this, clock.stamp());
                new Thread(message).start();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        System.out.println("Receive: " + message + ", " + Vm.toString() + ", " + Sm.buffer.toString());
        if (canDeliver(Sm)) {
            deliver(message, Sm, Vm);
        } else {
            B.add(new Message(message, Sm, null, 0, 0, null, null, Vm));
        }
    }

    public void setOutbox(List<ProcessMessage> outgoing) {
        this.outbox = outgoing;
    }

    @Override
    public void deliver(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException {
        clock.update(Vm);
        clock.increment();
        S.merge(Sm);
        System.out.println("Deliver: " + message + ", " + clock.stamp().toString() + ", " + Sm.buffer.toString());
        for (Message m : B) {
            if (canDeliver(m.Sm)) {
                clock.update(m.timestamp);
                clock.increment();
                B.remove(m);
                S.merge(m.Sm);
                System.out.println("Deliver: " + m.message + ", " + clock.stamp().toString() + ", " + Sm.buffer.toString());
            }
        }

    }

    private boolean canDeliver(OrderingBuffer Sm) {
        if (Sm.contains(id)) {
            return Sm.get(id).leq(clock.stamp());
        }
        return true;
    }
}
