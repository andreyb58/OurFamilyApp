package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.example.killer.R;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.CalendarEvent;

public class AddEventDialog extends DialogFragment {

    public interface OnEventAddedListener {
        void onEventAdded(CalendarEvent event);
    }

    private OnEventAddedListener listener;

    public void setListener(OnEventAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_event, null);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etTime = view.findViewById(R.id.et_time);
        EditText etAssignedTo = view.findViewById(R.id.et_assigned_to);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String description = etDescription.getText().toString();
            String time = etTime.getText().toString();
            String assignedTo = etAssignedTo.getText().toString();

            if (!title.isEmpty() && !time.isEmpty()) {
                CalendarEvent event = new CalendarEvent(title, description,
                        new java.util.Date(), time, "", assignedTo);

                new AsyncTask<CalendarEvent, Void, Void>() {
                    @Override
                    protected Void doInBackground(CalendarEvent... events) {
                        AppDatabase.getDatabase(requireContext()).calendarEventDao().insert(events[0]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(getContext(), "Событие добавлено!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onEventAdded(event);
                        }
                    }
                }.execute(event);

                dismiss();
            } else {
                Toast.makeText(getContext(), "Заполните название и время", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}