import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadFiles {


    public static String readFile(File file) {

        String line  = null;
        StringBuilder builder = new StringBuilder();

        try {

            if (file.isFile()) {

                BufferedReader reader = new BufferedReader(new FileReader(file));

                do {
                    line = reader.readLine();
                    if (line != null && !line.equals("")) {
                        builder.append(line + ",");
                    }
                }
                while (line != null && !line.equals(","));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static String readDirectory(String name) {

        File directory = new File(name);

        StringBuilder builder = new StringBuilder();

        File[] files = directory.listFiles();

        if (directory.isDirectory() && files != null) {

            for (File file : files) {

                builder.append("Message received at " + file.getName() + ": ");

                String line = readFile(file);

                //This accounts for the ',' at the end of each line
                builder.append(line, 0, line.length() - 1);

                builder.append(System.getProperty("line.separator"));

            }

        } else {
            return null;
        }
        return builder.toString();
    }

}
