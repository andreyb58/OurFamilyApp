package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.example.killer.R;
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

        EditText etTitle       = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etTime        = view.findViewById(R.id.et_time);
        EditText etAssignedTo  = view.findViewById(R.id.et_assigned_to);
        Button btnSave         = view.findViewById(R.id.btn_save);
        Button btnCancel       = view.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(v -> {
            String title       = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String time        = etTime.getText().toString().trim();
            String assignedTo  = etAssignedTo != null ? etAssignedTo.getText().toString().trim() : "";

            if (title.isEmpty()) {
                etTitle.setError("Введите название");
                return;
            }
            if (time.isEmpty()) {
                etTime.setError("Введите время (например: 14:30)");
                return;
            }

            // Передаём данные в CalendarFragment — сохранение происходит там
            CalendarEvent event = new CalendarEvent(title, description,
                    new java.util.Date(), time, "", assignedTo);

            if (listener != null) {
                listener.onEventAdded(event);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
