import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Client {

    private static Socket socket;
    private static final int TIMEOUT = 100;
    private static final int BUFFER_SIZE = 140;
    private static OutputStream output;
    private static HashMap<String, String> serverMap = new HashMap<>();
    private static String currentDirectory;
    private static BufferedReader reader;

    private static Socket start(String host, String port) {

        try {

            InetAddress address;
            int portInt;

            address = InetAddress.getByName(host);
            portInt = Integer.parseInt(port);

            socket = new Socket(address, portInt);
            socket.setSoTimeout(TIMEOUT);

        } catch (UnknownHostException e) {
            System.out.println("Failed to connect to the given server.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    private static void chooseServer(String[] args) {

        if (args.length == 2) {
            socket = start(args[0], args[1]);
        } else if (args.length == 0) {

            updateServerMap();

            System.out.println("Below is a list of some available servers:");

            for (HashMap.Entry<String, String> address : serverMap.entrySet()) {
                System.out.println(address.getKey());
            }

            System.out.println("\nIf you wish to connect to a server on the list, simply type in its code (eg. 'aci2').");
            System.out.println("If you wish to connect to a server not on the list, please type 'other'.");

            String serverString = userInput().trim();

            if (serverString.equals("other")) {

                System.out.println("Please type in the address of the server you wish to connect to below:");
                String serverAddress = userInput().trim();

                System.out.println("Please type in the port number of the server you wish to connect to below:");
                String serverPort = userInput().trim();

                socket = start(serverAddress, serverPort);

            } else if (serverMap.containsKey(serverString)) {

                socket = start(serverString + ".host.cs.st-andrews.ac.uk", serverMap.get(serverString));

            } else {

                System.out.println("The given input is not in the server list or a valid command.");
                System.exit(0);
            }

        } else {

            System.out.println("Usage: java Client <Server_Address> <Port_Number>");
            System.out.println("Or enter no arguments and follow the instructions.");
            System.exit(0);
        }

        System.out.println("Successfully connected to " + socket.getInetAddress().toString() + " on port " + socket.getPort());
    }

    private static void sendMessage() {

        byte[] buffer = new byte[BUFFER_SIZE];

        byte[] message;

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
    }

    private static String userInput() {

        int b = 0;

        String serverString = null;

        byte[] server = new byte[BUFFER_SIZE];

        try {

            while (b < 1) {

                b = System.in.read(server);
            }

            serverString = new String(server);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverString;
    }

    private static void updateServerMap() {

        File serverListFile = new File(currentDirectory + "servers.txt");

        if (serverListFile.exists() && serverListFile.isFile()) {
            String file = ReadFiles.readFile(serverListFile);

            String[] split = file.split(",");

            for (String line : split) {

                if (!line.equals("")) {

                    String[] server = line.split(" ");
                    serverMap.put(server[0], server[1]);
                }
            }
        }
    }

    private static void receiveMessage() {

        String line;

        try {
            do {

                line = reader.readLine();

                if (line != null && !line.equals("")) {
                    System.out.println(line);
                }

            } while (reader.ready());

        } catch (SocketTimeoutException ignored) { //This exception can be ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        currentDirectory = System.getProperty("user.dir").replaceAll("src", "");

        try {

            chooseServer(args);

            output = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            reader = new BufferedReader(new InputStreamReader(input));

            while (true) { //This loop only ends when the user exits with control C
                Thread.sleep(100);
                receiveMessage();
                sendMessage();
            }

        } catch (ConnectException e) {
            System.out.println("Failed to connect to the given server.");
        }catch (SocketTimeoutException ignored) { //This exception can be ignored
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}