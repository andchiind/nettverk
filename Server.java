import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Server {

    private static ServerSocket socket;
    private static int port = 24103;
    private static int timeout = 10;
    private static int bufferSize = 140;

    private static void start() {

        try {

            socket = new ServerSocket(port);
            socket.setSoTimeout(timeout);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        start();

        InputStream input;
        File file = null;
        File directory = null;

        Socket client = null;
        String message = null;

        while (true) {

            try {

                client = socket.accept();

                input = client.getInputStream();

                byte[] buffer = new byte[bufferSize];

                int b = 0;

                while (b < 1) {

                    Thread.sleep(100);

                    buffer = new byte[bufferSize];

                    b = input.read(buffer);

                }

                if (b > 0) {

                    byte[] messageBytes = new byte[b];
                    System.arraycopy(buffer, 0, messageBytes, 0, b);

                    message = new String(messageBytes);
                    Date date = new Date();
                    String dateFormat = "yyyy-MM-dd_HH:mm:ss.SSS";
                    String dateFormatDay = "yyyy-MM-dd";
                    SimpleDateFormat formatSeconds = new SimpleDateFormat(dateFormat);

                    String time = formatSeconds.format(date);

                    System.out.println("Received message from " + client.getInetAddress().getHostName() + " at " + time);

                    SimpleDateFormat formatDays = new SimpleDateFormat(dateFormatDay);

                    String todayDate = formatDays.format(date);

                    directory = new File("/cs/home/aci2/nginx_default/cs2003/W04-Practical" + File.separator + todayDate);

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

                    //file = new File(System.getProperty("user.dir") + File.separator + time + ".txt");
                    file = new File(directory.getPath() + File.separator + time + ".txt");

                    if (file.exists()) {

                        System.out.println("File already exists: " + file.getName());
                        System.exit(0);
                    }

                    try {

                        FileWriter writer = new FileWriter(file);
                        writer.write(message);
                        writer.flush();
                        writer.close();

                        client.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (SocketTimeoutException e) {
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



        }

    }
}