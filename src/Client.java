import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Client {
    private static BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    private static DatagramSocket clientSocket;
    private static InetAddress inetAddress;
    private static byte[] receiveData = new byte[1024];
    private static String clientName = "Client";
    private static int port;
    private static Thread thread1;
    private static Thread thread2;

    public static void main(String[] args) throws SocketException, UnknownHostException {
        if (args.length != 1) {
            System.out.println("example: java Client 1234");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        execute();
    }

    private static void execute() throws SocketException, UnknownHostException {
        Server.startMessage();
        clientSocket = new DatagramSocket();
        inetAddress = InetAddress.getByName("localhost");

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
        clientSocket.receive(receivePacket);
        var message = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println(message);
    }

    private static void writeMessage() throws IOException {
        String sentence = inFromUser.readLine();

        if (sentence.startsWith("@name") && sentence.length() > 6) {
            clientName = sentence.substring(6);
        } else if (sentence.startsWith("@quit")) {
            thread1.interrupt();
            thread2.interrupt();
            clientSocket.close();
        } else if (!sentence.isEmpty()) {
            sentence = clientName + ": " + sentence;
            byte[] sendData = sentence.getBytes();
            var sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, port);
            clientSocket.send(sendPacket);
        }
    }
}