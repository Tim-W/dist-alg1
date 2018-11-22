import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;

public class Main {
    public static void main(String[] args) throws AlreadyBoundException, IOException, NotBoundException {
        ProcessBuilder pb1 =
                new ProcessBuilder("java", "ProcessStarter", "../../test-resources/scenario2/input1");
        ProcessBuilder pb2 =
                new ProcessBuilder("java", "ProcessStarter", "../../test-resources/scenario2/input2");
        ProcessBuilder pb3 =
                new ProcessBuilder("java", "ProcessStarter", "../../test-resources/scenario2/input3");

        File dir = new File("target/classes");

        File log1 = new File("test-resources/scenario2/log1");
        File log2 = new File("test-resources/scenario2/log2");
        File log3 = new File("test-resources/scenario2/log3");

        PrintWriter writer = new PrintWriter(log1);
        writer.print("");
        writer.close();
        PrintWriter writer2 = new PrintWriter(log2);
        writer2.print("");
        writer2.close();
        PrintWriter writer3 = new PrintWriter(log3);
        writer3.print("");
        writer3.close();

        pb1.redirectErrorStream(true);
        pb1.redirectOutput(ProcessBuilder.Redirect.appendTo(log1));

        pb2.redirectErrorStream(true);
        pb2.redirectOutput(ProcessBuilder.Redirect.appendTo(log2));

        pb3.redirectErrorStream(true);
        pb3.redirectOutput(ProcessBuilder.Redirect.appendTo(log3));

        pb1.directory(dir);
        pb2.directory(dir);
        pb3.directory(dir);

        pb1.start();
        pb2.start();
        pb3.start();
    }
}
