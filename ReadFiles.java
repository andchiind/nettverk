import java.io.*;

public class ReadFiles {


    public static String readFile(File file) {

        String line  = null;

        try {

            if (file.isFile()) {

                BufferedReader reader = new BufferedReader(new FileReader(file));

                line = reader.readLine();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static String readDirectory(String name) {

        File directory = new File(name);

        StringBuilder builder = new StringBuilder();

        if (directory.isDirectory()) {

            File[] files = directory.listFiles();

            for (File file : files) {

                builder.append("Message received at " + file.getName() + ":");
                builder.append(readFile(file) + "\n");

            }
        } else {
            return null;
        }
        return builder.toString();
    }

}
