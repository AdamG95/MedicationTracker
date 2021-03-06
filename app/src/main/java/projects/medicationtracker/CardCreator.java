package projects.medicationtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class CardCreator
{
    /**
     * Creates a schedule for the given patient's medications
     *
     * @param medications An ArrayList of Medications. Will be searched for
     *                    Medications where patientName equals name passed to method.
     * @param name The name of the patient whose Medications should be displayed
     * @param db The database from which to pull data
     * @param scheduleLayout The LinearLayout into which the cards will be placed
     **************************************************************************/
    public static void createMedicationSchedule(ArrayList<Medication> medications, String name, DBHelper db, LinearLayout scheduleLayout)
    {
        ArrayList<Medication> medicationsForThisPatient = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++)
        {
            if (medications.get(i).getPatientName().equals(name))
                medicationsForThisPatient.add(medications.get(i));
        }

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int ii = 0; ii < 7; ii++)
            createDayOfWeekCards(days[ii], ii, medicationsForThisPatient, scheduleLayout, db, scheduleLayout.getContext());
    }

    /**
     * Creates a CardView for each day of the week containing information
     * on the medications to be taken that day
     *
     * @param dayOfWeek The day of the week represented by the CardView.
     * @param day The number representing the day of the week
     *            - Sunday = 0
     *            - Monday = 1
     *            - Tuesday = 2
     *            - Wednesday = 3
     *            - Thursday = 4
     *            - Friday = 5
     *            - Saturday = 6
     * @param medications The list of medications to be taken on the given day
     * @param layout The LinearLayout in which to place the CardView
     **************************************************************************/
    public static void createDayOfWeekCards (String dayOfWeek, int day, ArrayList<Medication> medications, LinearLayout layout, DBHelper db, Context context)
    {
        CardView thisDayCard = new CardView(context);
        TextView dayLabel = new TextView(context);
        LinearLayout ll = new LinearLayout(context);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.VERTICAL);

        setCardParams(thisDayCard);

        LocalDate thisSunday = TimeFormatting.whenIsSunday();

            // Add day to top of card
        TextViewUtils.setTextViewFontAndPadding(dayLabel);

        String dayLabelString = dayOfWeek + " " + TimeFormatting.localDateToString(thisSunday.plusDays(day));

        dayLabel.setText(dayLabelString);
        ll.addView(dayLabel);

        // Add medications
        thisDayCard.addView(ll);

        for (Medication medication : medications)
        {
            for (LocalDateTime time : medication.getTimes())
            {
                if (time.toLocalDate().isEqual(thisSunday.plusDays(day)))
                {
                    CheckBox thisMedication = new CheckBox(ll.getContext());
                    long medId = medication.getMedId();

                    // Set Checkbox label
                    String medName = medication.getMedName();
                    String dosage = medication.getMedDosage() + " " + medication.getMedDosageUnits();
                    String dosageTime = TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

                    String thisMedicationLabel = medName + " - " + dosage + " - " + dosageTime;
                    thisMedication.setText(thisMedicationLabel);

                    //TODO Change this so it is done with AlarmManager

                    // Check database for this dosage, if not add it
                    // if it is, get the DoseId
                    long rowid = 0;

                    if (!db.isInMedicationTracker(medication, time))
                    {
                        LocalDateTime startDate = medication.getStartDate();
                        if (!time.isBefore(startDate))
                        {
                            rowid = db.addToMedicationTracker(medication, time);
                            if (rowid == -1)
                                Toast.makeText(context, "An error occurred when attempting to write data to database", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        rowid = db.getDoseId(medId, TimeFormatting.localDateTimeToString(time));
                    }

                    if (rowid > 0)
                    {
                        thisMedication.setTag(rowid);

                        if (db.getTaken(rowid))
                            thisMedication.setChecked(true);

                        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                        {
                            final int doseId = Integer.parseInt(thisMedication.getTag().toString());

                            if (LocalDateTime.now().isBefore(time.minusHours(2)))
                            {
                                thisMedication.setChecked(false);
                                Toast.makeText(context, "Cannot take medications more than 2 hours in advance", Toast.LENGTH_SHORT).show();
                                return;
                            }


                            String now = TimeFormatting.localDateTimeToString(LocalDateTime.now());
                            db.updateDoseStatus(doseId, now, thisMedication.isChecked());
                        });

                        ll.addView(thisMedication);
                    }
                }
            }
        }

        if (ll.getChildCount() == 1)
        {
            TextView textView = new TextView(thisDayCard.getContext());
            String noMed = "No medications for " + dayOfWeek;

            TextViewUtils.setTextViewParams(textView, noMed, ll);
        }

        layout.addView(thisDayCard);
    }

    /**
     * Creates a CardView containing all information on a Medication
     * @param medication The Medication whose details will be displayed.
     * @param baseLayout The LinearLayout in which to place the card
     **************************************************************************/
    public static void createMyMedCards(Medication medication, LinearLayout baseLayout, Activity activity)
    {
        Context context = baseLayout.getContext();
        CardView thisMedCard = new CardView(context);
        LinearLayout thisMedLayout = new LinearLayout(context);
        thisMedLayout.setOrientation(LinearLayout.VERTICAL);
        baseLayout.addView(thisMedCard);

        setCardParams(thisMedCard);

        thisMedCard.addView(thisMedLayout);

        // Add name to thisMedLayout
        TextView name = new TextView(context);
        String nameLabel = "Medication name: " + medication.getMedName();
        TextViewUtils.setTextViewParams(name, nameLabel, thisMedLayout);

        // Add Dosage
        TextView doseInfo = new TextView(context);
        String doseInfoLabel = "Dosage: " + medication.getMedDosage() + " " + medication.getMedDosageUnits();
        TextViewUtils.setTextViewParams(doseInfo, doseInfoLabel, thisMedLayout);

        // Add Frequency
        TextView freq = new TextView(context);
        String freqLabel;

        if (medication.getMedFrequency() == 1440 && (medication.getTimes().length == 1))
        {
            String time = TimeFormatting.localTimeToString(medication.getTimes()[0].toLocalTime());
            freqLabel = "Taken daily at: " + time;
        }
        else if (medication.getMedFrequency() == 1440 && (medication.getTimes().length > 1))
        {
            freqLabel = "Taken daily at: ";

            for (int i = 0; i < medication.getTimes().length; i++)
            {
                LocalTime time = medication.getTimes()[i].toLocalTime();
                freqLabel += TimeFormatting.localTimeToString(time);

                if (i != (medication.getTimes().length - 1))
                    freqLabel += ", ";
            }
        }
        else
            freqLabel = "Taken every: " + TimeFormatting.freqConversion(medication.getMedFrequency());

        TextViewUtils.setTextViewParams(freq, freqLabel, thisMedLayout);

        // Add alias (if exists)
        if (!medication.getAlias().equals(""))
        {
            TextView alias = new TextView(context);
            String aliasLabel = "Alias: " + medication.getAlias();
            TextViewUtils.setTextViewParams(alias, aliasLabel, thisMedLayout);
        }

        // Add start date
        TextView startDate = new TextView(context);
        String startDateLabel = "Taken Since: " + TimeFormatting.localDateToString(medication.getStartDate().toLocalDate());
        TextViewUtils.setTextViewParams(startDate, startDateLabel, thisMedLayout);

        // Add LinearLayout for buttons
        Intent intent = new Intent(context, MedicationNotes.class);
        intent.putExtra("medId", medication.getMedId());
        ButtonManager.createActivityButton("Notes", thisMedLayout, context, intent, activity);

        intent = new Intent(context, EditMedication.class);
        intent.putExtra("medId", medication.getMedId());
        ButtonManager.createActivityButton("Edit", thisMedLayout, context, intent, activity);
    }

    /**
     * Creates a CardView with a note in it
     * @param note The Note in the CardView
     * @param baseLayout The LinearLayout the holds the CardView
     **************************************************************************/
    public static void createNoteCard(Note note, LinearLayout baseLayout)
    {
        Context context = baseLayout.getContext();
        CardView noteCard = new CardView(context);
        LinearLayout cardLayout = new LinearLayout(context);

        cardLayout.setOrientation(LinearLayout.VERTICAL);
        setCardParams(noteCard);

        baseLayout.addView(noteCard);
        noteCard.addView(cardLayout);

        TextView noteText = new TextView(context);
        TextViewUtils.setTextViewParams(noteText, "\"" + note.getNote() + "\"", cardLayout);

        TextView noteDate = new TextView(context);
        String noteDateLabel = "Date: " + TimeFormatting.localDateToString(note.getNoteTime().toLocalDate())
                + " at: " + TimeFormatting.localTimeToString(note.getNoteTime().toLocalTime());
        TextViewUtils.setTextViewParams(noteDate, noteDateLabel, cardLayout);

        noteText.setTag(note);
    }

    /**
     * Sets a CardView's parameters to a set standard
     * @param cardView The CardView whose parameters will be set
     **************************************************************************/
    public static void setCardParams(CardView cardView)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        marginLayoutParams.setMargins(25, 40, 25, 20);
        cardView.requestLayout();
        cardView.setRadius(30);
    }

    /**
     * Creates a card in which users may edit the given medication
     * @param editMedLayout The LinearLayout inside the CardView
     * @param medication The medication to edit
     **************************************************************************/
    public static void createEditMedCard(LinearLayout editMedLayout, Medication medication)
    {
        final Context context = editMedLayout.getContext();

        String medNameLabel = "Name: " + medication.getMedName();

        Switch medNameSwitch = new Switch(context);
        medNameSwitch.setText(medNameLabel);
        editMedLayout.addView(medNameSwitch);
    }
}
