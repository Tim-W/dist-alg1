import java.io.FileNotFoundException;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProcessStarter {
    public static void main(String[] args) throws AlreadyBoundException, RemoteException, FileNotFoundException {
        FileReader fileReader = new FileReader(args[0]);

        Scanner sc = new Scanner(fileReader);

        List<ProcessMessage> processMessages = new ArrayList<>();
        int processId = sc.nextInt();
        int networkSize = sc.nextInt();

        while (sc.hasNextInt()) {
            int destination = sc.nextInt();
            int startDelay = sc.nextInt();
            int endDelay = sc.nextInt();
            processMessages.add(new ProcessMessage(processId + " -> " + destination, startDelay, endDelay, destination));
        }

        Process process = new Process(processId, networkSize);

        process.setOutbox(processMessages);
        new Thread(process).start();
    }
}
