import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

    private static ServerSocket socket;
    private static int port = 24104;
    private static int timeout = 10;
    private static int bufferSize = 140;
    private static InputStream input;
    private static OutputStream output;
    private static Socket client = null;

    private static void start() {

        try {

            socket = new ServerSocket(port);
            socket.setSoTimeout(timeout);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkDirectory(File directory) {

        if (directory.exists()) {
            System.out.println("Directory already exists: " + directory.getName());
        }

        if (!directory.exists()) {

            if (directory.mkdir()) {
                System.out.println("Created directory: " + directory.getName());
            } else {
                System.out.println("Failed to create directory: " + directory.getName());
                System.exit(0);
            }
        }
    }

    private static byte[] receiveMessage() {


        byte[] buffer = new byte[bufferSize];

        int b = 0;

        try {

            while (b < 1) {

                buffer = new byte[bufferSize];

                b = input.read(buffer);

                //Here we check if the connection is still open
                if (buffer[0] == 0) {
                    System.out.println("Client disconnected without sending a message. \n");
                    //client.close();
                    return buffer;
                    //break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }


    public static void main(String[] args) {

        start();

        //InputStream input;
        //OutputStream output;
        File file = null;
        File directory = null;
        Socket client = null;
        String message = null;

        while (true) {

            try {

                client = socket.accept();

                System.out.println("New connection made from: " + client.getInetAddress().getHostName());

                input = client.getInputStream();
                output = client.getOutputStream();

                byte[] buffer = receiveMessage();

                int b = buffer.length;

                /*byte[] buffer = new byte[bufferSize];

                int b = 0;

                while (b < 1) {

                    buffer = new byte[bufferSize];

                    b = input.read(buffer);

                    //Here we check if the connection is still open
                    if (buffer[0] == 0) {
                        System.out.println("Client disconnected without sending a message. \n");
                        client.close();
                        break;
                    }
                }*/

                if (!(buffer[0] == 0)) {
                //if (b > 0) {

                    byte[] messageBytes = new byte[b];
                    System.arraycopy(buffer, 0, messageBytes, 0, b);

                    message = new String(messageBytes);

                    message = message.trim();

                    if (message.equals("w")) {

                        /*buffer = new byte[bufferSize];

                        b = 0;*/

                        buffer = receiveMessage();

                        b = buffer.length;

                        /*while (b < 1) {

                            buffer = new byte[bufferSize];

                            b = input.read(buffer);

                            //Here we check if the connection is still open
                            if (buffer[0] == 0) {
                                System.out.println("Client disconnected without sending a message. \n");
                                client.close();
                                break;
                            }

                        }*/

                        if (!(buffer[0] == 0)) {

                            messageBytes = new byte[b];

                            System.arraycopy(buffer, 0, messageBytes, 0, b);

                            message = new String(messageBytes);

                            message = message.trim();

                            String time = CurrentTime.getTimeSeconds();

                            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! COULD BE MESSY
                            System.out.println("Received message \"" + message + "\" from " + client.getInetAddress().getHostName() + " at " + time);

                            String todayDate = CurrentTime.getTimeDays();

                            directory = new File("/cs/home/aci2/nginx_default/cs2003/Net1" + File.separator + todayDate);

                            checkDirectory(directory);

                            file = new File(directory.getPath() + File.separator + time);

                            if (file.exists()) {

                                System.out.println("File already exists: " + file.getName());
                                System.exit(0);
                            } else {

                                System.out.println("Created new file: " + file.getName() + "\n");
                            }

                            try {

                                FileWriter writer = new FileWriter(file);
                                writer.write(message);
                                writer.flush();
                                writer.close();
                                //client.close(); !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else if (message.startsWith("r")) {

                        buffer = receiveMessage();

                        b = buffer.length;

                        if (!(buffer[0] == 0)) {

                            messageBytes = new byte[b];

                            System.arraycopy(buffer, 0, messageBytes, 0, b);

                            message = new String(messageBytes);

                            message = message.trim();

                            /*message = message.replaceFirst("r ", "");

                            message = message.trim();*/

                            System.out.println("Retrieving all messages from: " + message);

                            String reply = ReadFiles.readDirectory("/cs/home/aci2/nginx_default/cs2003/Net1" + File.separator + message);

                            if (reply == null) {

                                byte[] notFound = "The given directory does not exist".getBytes();
                                byte[] length = new byte[1];
                                length[0] = 1;
                                output.write(length);
                                output.write(notFound);
                                System.out.println("The directory: \"" + message + "\" does not exist. \n");

                            } else {

                                System.out.println("The messages were retrieved successfully. \n");

                                byte[] replyBytes = reply.getBytes();

                                byte[] length = new byte[1];
                                Integer lengthInt = (replyBytes.length / 127) + 1;
                                length[0] = lengthInt.byteValue();

                                output.write(length);
                                output.write(replyBytes);
                            }
                        }

                    } else {
                        output.write((message + " is not a valid command").getBytes());
                    }
                }

            } catch (SocketTimeoutException e) {
                //This exception can be ignored
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}