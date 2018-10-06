import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.*;
import java.util.Arrays;

public class Client {

    private static Socket socket;
    private static int timeout = 10;
    private static byte[] buffer;
    private static int bufferSize = 140;
    private static OutputStream output;
    private static InputStream input;

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

    private static byte[] sendMessage() {

        byte[] buffer = new byte[bufferSize];

        byte[] message = new byte[bufferSize];

        int b = 0;

        try {

            while (b < 1) {

                b = System.in.read(buffer);

            }

            message = new byte[b];

            System.arraycopy(buffer, 0, message, 0, b);

            output.write(message);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    protected void finalize() {

        try {
            byte[] closing = new byte[1];
            closing[0] = 0;
            output.write(closing);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("");
        }

        try {

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            socket = start(args[0], "24104");

            System.out.println("Successfully connected to " + socket.getInetAddress().toString());

            while (true) { //The program keeps running until the user uses control C

                output = socket.getOutputStream();
                input = socket.getInputStream();

                /*buffer = new byte[bufferSize];

                int b = 0;

                while (b < 1) {

                    b = System.in.read(buffer);

                }

                byte[] message = new byte[b];

                System.arraycopy(buffer, 0, message, 0, b);

                output.write(message);*/

                byte[] message = sendMessage();

                int b = message.length;

                if (message[0] == 114) {

                    message = sendMessage();

                    //b = message.length;

                    /*buffer = new byte[bufferSize];

                    b = 0;

                    while (b < 1) {

                        b = System.in.read(buffer);

                    }

                    message = new byte[b];

                    System.arraycopy(buffer, 0, message, 0, b);

                    output.write(message);*/

                    b = 0;

                    byte[] length = new byte[1];

                    while (b < 1) {

                        Thread.sleep(1000);

                        b = input.read(length);

                    }

                    b = 0;

                    byte[] incoming = new byte[length[0] * 127];

                    while ((b == 0)) {

                        b = input.read(incoming);
                    }

                    System.out.println(new String(incoming));
                }

            }

        } catch (SocketTimeoutException e) {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}