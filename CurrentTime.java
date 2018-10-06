import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentTime {

    public static String getTimeSeconds() {

        Date date = new Date();

        String dateFormat = "yyyy-MM-dd_HH:mm:ss.SSS";

        SimpleDateFormat formatSeconds = new SimpleDateFormat(dateFormat);

        return formatSeconds.format(date);

    }

    public static String getTimeDays() {

        Date date = new Date();

        String dateFormatDay = "yyyy-MM-dd";

        SimpleDateFormat formatDays = new SimpleDateFormat(dateFormatDay);

        return formatDays.format(date);

    }

}
