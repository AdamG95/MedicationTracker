package projects.medicationtracker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        String chosenTime = "At: ";
        String min;
        String amOrPm;
        int[] hourAndMin = {hourOfDay, minute};

        if (hourOfDay >= 12)
        {
            chosenTime += String.valueOf(hourOfDay - 12);
            amOrPm = " PM";
        }
        else
        {
            if (hourOfDay > 0)
                chosenTime += String.valueOf(hourOfDay);
            else
                chosenTime += "12";
            amOrPm = " AM";
        }

        if (minute < 10)
            min = "0" + String.valueOf(minute);
        else
            min = String.valueOf(minute);

        chosenTime += ":" + min + amOrPm;

        TextView textView = getActivity().findViewById(R.id.hiddenTextView);

        textView.setTag(hourAndMin);
        textView.setText(chosenTime);
    }
}
