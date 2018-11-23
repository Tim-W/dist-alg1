import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;

/**
 * Spins up multiple ProcessStarter instances using Java ProcessBuilder.
 * args[0]: The path to the test resources directory. This directory expects:
 *  - 'input' directory
 *      - Every file should be named 'input0'...'inputn' for n + 1 processes (for specification of input type see
 *      Process class).
 *  - 'log' directory
 *      - generated and cleaned on every run.
 *      - Files are named 'log0' ... 'logn' for n + 1 processes.
 */
public class Main {
    public static void main(String[] args) throws AlreadyBoundException, IOException, NotBoundException {
        String testResourcesPath = args[0];
        File classesPath = new File("target/classes");
        File testResourcesInputDir = new File(testResourcesPath + "/input");
        int networkSize = testResourcesInputDir.list().length;
        for (int i = 0; i < networkSize; i++) {
            ProcessBuilder pb =
                    new ProcessBuilder("java", "ProcessStarter", "../../" + testResourcesPath + "/input/input" + i, networkSize + "");

            File log = new File(testResourcesPath + "/log/log" + i);

            PrintWriter writer = new PrintWriter(log);
            writer.print("");
            writer.close();

            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));

            pb.directory(classesPath);

            pb.start();
        }
    }
}
