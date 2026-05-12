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

public class InviteMemberDialog extends DialogFragment {

    private OnInviteListener listener;

    public interface OnInviteListener {
        void onInvite(String email, String role);
    }

    public void setOnInviteListener(OnInviteListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invite_member, null);

        EditText etEmail = view.findViewById(R.id.et_email);
        Button btnInvite = view.findViewById(R.id.btn_invite);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnInvite.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Введите email");
                return;
            }

            if (listener != null) {
                listener.onInvite(email, "CHILD");
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}