package projects.medicationtracker;

import android.icu.text.SimpleDateFormat;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class TimeFormatting
{
    public static String formatTime(int hour, int minute)
    {
        String chosenTime = "At: ";
        String min;
        String amOrPm;

        if (hour >= 12)
        {
            chosenTime += String.valueOf(hour - 12);
            amOrPm = " PM";
        }
        else
        {
            if (hour > 0)
                chosenTime += String.valueOf(hour);
            else if (hour == 0)
                chosenTime += "12";
            amOrPm = " AM";
        }

        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);

        return chosenTime + ":" + min + amOrPm;
    }

    // Returns a string with the hour and formatted for the database
    public static String formatTimeForDB(int hour, int minute)
    {
        String time;

        if (hour < 10)
            time = "0" + hour;
        else
            time = String.valueOf(hour);

        if (minute < 10)
            time += ":0" + minute;
        else
            time += ":" + minute;

        return time + ":00";
    }

    // Set text views to current time and date
    public static void getCurrentTimeAndDate(TextView date, TextView time)
    {
        String[] dateForUser = new String[2];
        String dateTime;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateForDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Date myDate = new Date();
        dateForUser[0] = dateFormat.format(myDate);
        dateForUser[1] = dateFormat1.format(myDate);
        dateTime = dateForDb.format(myDate);

        date.setTag(dateTime);
        date.setText(dateForUser[0]);
        time.setText(dateForUser[1]);
    }
}
