import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {
    private static BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    private static DatagramSocket serverSocket;
    private static InetAddress inetAddress;
    private static int port;
    private static byte[] receiveData = new byte[1024];
    private static String serverName = "Server";
    private static Thread thread1;
    private static Thread thread2;

    public static void main(String[] args) throws SocketException {
        if (args.length != 1) {
            System.out.println("example: java Server 1234");
            System.exit(0);
        }
        port = Integer.parseInt(args[0]);
        execute();
    }

    private static void execute() throws SocketException {
        serverSocket = new DatagramSocket(port);
        System.out.println("\nServer launched on " + port + " port\n");
        
        startMessage();

        thread1 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    getMessage();
                } catch (IOException e) {

                }
            }
        });

        thread2 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    writeMessage();
                } catch (IOException e) {

                }
            }
        });

        thread1.start();
        thread2.start();
    }

    private static void getMessage() throws IOException {
        var receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        var message = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println(message);
        inetAddress = receivePacket.getAddress();
        port = receivePacket.getPort();
    }

    public static void writeMessage() throws IOException {

        String sentence = inFromUser.readLine();

        if (sentence.startsWith("@name") && sentence.length() > 6) {
            serverName = sentence.substring(6);

        } else if (sentence.startsWith("@quit")) {
            thread1.interrupt();
            thread2.interrupt();
            serverSocket.close();

        } else if (!sentence.isEmpty()) {
            sentence = serverName + ": " + sentence;
            byte[] sendData = sentence.getBytes();
            var sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, port);
            if (inetAddress != null) {
                serverSocket.send(sendPacket);
            }
        }
    }

    public static void startMessage() {
        System.out.println("""

                Welcome to the Chat.

                You can: 1. Set username (@name Bob)
                         2. Send message (Hello)
                         3. Exit (@quit)

                """);
    }
}
