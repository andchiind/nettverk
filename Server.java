import javax.security.sasl.SaslException;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    private static String initialConnection;
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

    private static String getFolders() {

        StringBuilder builder = new StringBuilder();
        File currentFolder = new File(currentDirectory + File.separator + "web");
        File[] listDays = currentFolder.listFiles();
        for (File day : listDays) {
            if (day.isDirectory() && !(day.listFiles().length < 1)) {
                builder.append(day.getName() + "\n");
            }
        }

        return builder.toString();
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
                    updateLog();
                    break;
                } else if (buffer[0] == 10) {
                    b = 0;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    private static void updateLog() {

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

                System.out.println("\nNew connection made from: " + client.getInetAddress().getHostName());

                input = client.getInputStream();
                OutputStream output = client.getOutputStream();

                output.write("Type 'r' to retrieve previous messages, 'w' to write a new message or 'e' to edit or delete a message.\n".getBytes());

                byte[] buffer = receiveMessage();

                int b = buffer.length;

                if (!(buffer[0] == 0)) {

                    byte[] messageBytes = new byte[b];
                    System.arraycopy(buffer, 0, messageBytes, 0, b);

                    message = new String(messageBytes);

                    message = message.trim();

                    switch (message) {
                        case "w":

                            output.write("Enter your message: \n".getBytes());

                            buffer = receiveMessage();

                            b = buffer.length;

                            if (!(buffer[0] == 0)) {

                                messageBytes = new byte[b];

                                System.arraycopy(buffer, 0, messageBytes, 0, b);

                                message = new String(messageBytes);

                                message = message.trim();

                                String time = CurrentTime.getTimeSeconds();

                                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! COULD BE MESSY
                                System.out.println("Received message: \"" + message + "\" \nfrom " + client.getInetAddress().getHostName() + " at " + time);

                                String todayDate = CurrentTime.getTimeDays();

                                directory = new File(currentDirectory + File.separator + "web" + File.separator + todayDate); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                                checkDirectory(directory);

                                file = new File(directory.getPath() + File.separator + time);

                                if (file.exists()) {

                                    System.out.println("File already exists: " + file.getName());
                                    System.exit(0);
                                } else {

                                    System.out.println("Created new file: " + file.getName() + "\n");
                                }

                                try {

                                    FileWriter fileWriter = new FileWriter(file);
                                    fileWriter.write(message);
                                    fileWriter.flush();
                                    fileWriter.close();
                                    updateLog();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;
                        case "r":

                            output.write(("Here is a list of all of the days with messages: \n" + getFolders() +
                                    "Enter the date of the messages you want to retrieve in the following format: \nYYYY-MM-DD \n").getBytes());

                            buffer = receiveMessage();

                            if (!(buffer[0] == 0)) {

                                messageBytes = new byte[b];

                                System.arraycopy(buffer, 0, messageBytes, 0, b);

                                message = new String(messageBytes).trim();

                                System.out.println("Retrieving all messages from: " + message);

                                String reply = ReadFiles.readDirectory(currentDirectory + File.separator + "web" + File.separator + message);

                                if (reply == null) {

                                    output.write(("The given directory: \"" + message + "\" does not exist \n").getBytes());

                                    System.out.println("The directory: \"" + message + "\" does not exist. \n");

                                    updateLog();

                                } else {

                                    System.out.println("The messages were retrieved successfully. \n");

                                    byte[] replyBytes = reply.getBytes();
                                    output.write(replyBytes);
                                }
                            }

                            updateLog();

                            break;
                        case "e":

                            boolean success = false;

                            output.write(("Here is a list of all of the days with messages: \n" + getFolders() +
                                    "Please type 'd' for delete or 'e' for edit followed by the name of the file you wish to edit. \n").getBytes());
                            buffer = receiveMessage();
                            String editCommand = new String(buffer);

                            if (editCommand.startsWith("d ")) {

                                if (editCommand.contains("_") && editCommand.contains("d ")) {

                                    editCommand = editCommand.replaceFirst("d ", "").trim();

                                    String folder = editCommand.substring(0, editCommand.indexOf("_"));

                                    File dayFolder = new File(currentDirectory + File.separator + "web" + File.separator + folder);

                                    if (dayFolder.isDirectory()) {

                                        File[] secondFiles = dayFolder.listFiles();

                                        for (File secondFile : secondFiles) {
                                            if (secondFile.getName().equals(editCommand)) {

                                                if (secondFile.delete()) {

                                                    output.write(("Successfully removed file: " + editCommand + "\n").getBytes());
                                                    System.out.println("Successfully removed file: " + editCommand);

                                                    updateLog();
                                                    success = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                            } else if (editCommand.startsWith("e ")) {

                                if (editCommand.contains("_") && editCommand.contains("e ")) {

                                    editCommand = editCommand.replaceFirst("e ", "").trim();
                                    String folder = editCommand.substring(0, editCommand.indexOf("_"));
                                    File dayFolder = new File(currentDirectory + File.separator + "web" + File.separator + folder);

                                    if (dayFolder.isDirectory()) {
                                        File[] secondFiles = dayFolder.listFiles();

                                        for (File secondFile : secondFiles) {
                                            if (secondFile.getName().equals(editCommand)) {

                                                output.write("Please type in what you want to change the message to.\n".getBytes());

                                                String newMessage = (new String(receiveMessage())).trim();
                                                FileWriter editFile = new FileWriter(secondFile);

                                                editFile.write(newMessage);
                                                output.write(("Successfully edited file: " + editCommand + "\n").getBytes());
                                                System.out.println("Successfully edited file: " + editCommand);

                                                editFile.flush();
                                                editFile.close();

                                                updateLog();
                                                success = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (!success) {
                                System.out.println("Failed to delete/edit file: " + editCommand);
                                output.write(("Failed to delete/edit file: " + editCommand + "\n").getBytes());
                            }

                            updateLog();
                            break;
                        default:

                            output.write(("The given input: \"" + message + "\" is not a valid command\n").getBytes());
                            System.out.println("The given input: \"" + message + "\" is not a valid command");
                            updateLog();
                            break;
                    }
                }

            } catch (SocketTimeoutException ignored) { //This exception can be ignored
            } catch (SaslException e) {
                System.out.println("Lost connection to the client.");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}