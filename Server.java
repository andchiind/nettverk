import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

    private static ServerSocket socket;
    private static final int PORT = 24104;
    private static final int TIMEOUT = 10;
    private static final int BUFFER_SIZE = 140;
    private static InputStream input;
    private static OutputStream output;
    private static Socket client = null;
    private static String initialConnection; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static String address;
    private static String currentDirectory;

    private static void start() {

        try {

            socket = new ServerSocket(PORT);
            socket.setSoTimeout(TIMEOUT);

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


        byte[] buffer = new byte[BUFFER_SIZE];

        int b = 0;

        try {

            while (b < 1) {

                buffer = new byte[BUFFER_SIZE];

                b = input.read(buffer);

                //Here we check if the connection is still open
                if (buffer[0] == 0) {
                    System.out.println("Client disconnected without sending a message. \n");
                    updateLog(client);
                    //client.close();
                    //return buffer;
                    break;
                } else if ((buffer.length < 3 && buffer[0] == 32) || buffer[0] == 10) {
                    b = 0;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    private static void updateLog(Socket client) {

        try {

            File log = new File(currentDirectory + "log/log.txt");

            BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));

            writer.write("Connected from: \"" + address +
                    "\" at: \"" + initialConnection + "\" and disconnected at: \"" + CurrentTime.getTimeSeconds() + "\" \n");

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {

        currentDirectory = System.getProperty("user.dir").replaceAll("src", "");

        start();

        File file;
        File directory;
        Socket client;
        String message;

        while (true) {

            try {

                client = socket.accept();

                initialConnection = CurrentTime.getTimeSeconds();

                address = client.getInetAddress().getHostName();

                System.out.println("New connection made from: " + client.getInetAddress().getHostName());

                input = client.getInputStream();
                output = client.getOutputStream();

                output.write("\nType 'r' to retrieve previous messages, or type 'w' to write a new message.".getBytes());

                byte[] buffer = receiveMessage();

                int b = buffer.length;

                if (!(buffer[0] == 0)) {

                    byte[] messageBytes = new byte[b];
                    System.arraycopy(buffer, 0, messageBytes, 0, b);

                    message = new String(messageBytes);

                    message = message.trim();

                    switch (message) {
                        case "w":

                            byte[] selectW = "Enter your message: ".getBytes();
                            output.write(selectW);

                            buffer = receiveMessage();

                            b = buffer.length;

                            if (!(buffer[0] == 0)) {

                                messageBytes = new byte[b];

                                System.arraycopy(buffer, 0, messageBytes, 0, b);

                                message = new String(messageBytes);

                                message = message.trim();

                                String time = CurrentTime.getTimeSeconds();

                                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! COULD BE MESSY
                                System.out.println("Received message \"" + message + "\" from " + client.getInetAddress().getHostName() + " at " + time);

                                String todayDate = CurrentTime.getTimeDays();

                                directory = new File(currentDirectory + todayDate);

                                checkDirectory(directory);

                                file = new File(directory.getPath() + time);

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
                                    updateLog(client);
                                    //client.close(); !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;
                        case "r":

                            byte[] selectR = "Enter the date of the messages you want to retrieve in the following format: \nYYYY-MM-DD".getBytes();
                            output.write(selectR);

                            buffer = receiveMessage();

                            b = buffer.length;

                            if (!(buffer[0] == 0)) {

                                messageBytes = new byte[b];

                                System.arraycopy(buffer, 0, messageBytes, 0, b);

                                message = new String(messageBytes);

                                message = message.trim();

                                System.out.println("Retrieving all messages from: " + message);

                                String reply = ReadFiles.readDirectory(currentDirectory + message);

                                if (reply == null) {

                                    byte[] notFound = "The given directory does not exist".getBytes();
                                    byte[] length = new byte[1];
                                    length[0] = 1;

                                    output.write(length);
                                    output.write(notFound);

                                    System.out.println("The directory: \"" + message + "\" does not exist. \n");

                                    updateLog(client);

                                } else {

                                    System.out.println("The messages were retrieved successfully. \n");

                                    byte[] replyBytes = reply.getBytes();

                                    byte[] length = new byte[1];
                                    Integer lengthInt = (replyBytes.length / 127) + 1;
                                    length[0] = lengthInt.byteValue();

                                    output.write(length);
                                    output.write(replyBytes);
                                    updateLog(client);
                                }
                            }

                            break;
                        default:
                            output.write(("The given input: \"" + message + "\" is not a valid command").getBytes());
                            updateLog(client);
                            break;
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