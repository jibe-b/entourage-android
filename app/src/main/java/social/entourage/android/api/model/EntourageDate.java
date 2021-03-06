package social.entourage.android.api.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mihaiionescu on 01/06/16.
 * Class that handles the Date formating in retrofit requests (not in json body)
 */
public class EntourageDate {
    private static final ThreadLocal<DateFormat> DF = new ThreadLocal<DateFormat>() {
        @Override public DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
    };

    private final Date date;

    public EntourageDate(Date date) {
        this.date = date;
    }

    @Override public String toString() {
        return DF.get().format(date);
    }
}
