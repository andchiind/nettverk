import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.*;

public class Client {

    private static Socket socket;
    private static int timeout = 10;
    private static byte[] buffer;
    private static int bufferSize = 140;

    private static Socket start(String host, String port) {

        try {

            InetAddress address;
            int portInt;

            address = InetAddress.getByName(host);
            portInt = Integer.parseInt(port);

            socket = new Socket(address, portInt);
            socket.setSoTimeout(timeout);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    public static void main(String[] args) {

        try {

            OutputStream output;

            socket = start("pc3-053-l.cs.st-andrews.ac.uk", "24103");

            while (true) {

                int b = 0;

                output = socket.getOutputStream();

                buffer = new byte[bufferSize];

                while (b < 1) {

                    b = System.in.read(buffer);

                }

                if (b > 0) {

                    byte[] message = new byte[b];

                    System.arraycopy(buffer, 0, message, 0, b);

                    output.write(message);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}