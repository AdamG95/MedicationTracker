package projects.medicationtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ConfirmMedicationDeleteFragment extends DialogFragment
{
    final DBHelper db;
    final Medication medication;

    /**
     * Creates a dialog fragment offering to delete the provided medication.
     * @param database  The DBHelper connected to the database with the medication.
     * @param medicationToDelete The medication to delete from the database.
     **************************************************************************/
    ConfirmMedicationDeleteFragment(DBHelper database, Medication medicationToDelete)
    {
        db = database;
        medication = medicationToDelete;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setTitle("Are you sure you want to delete this medication?");

        builder.setPositiveButton("Yes", ((dialogInterface, i) ->
        {
            Intent intent = new Intent(getContext(), MyMedications.class);
            db.deleteMedication(medication);
            getActivity().finish();
            startActivity(intent);
        }));

        builder.setNegativeButton("Cancel", (((dialogInterface, i) -> dismiss())));

        return builder.create();
    }
}
