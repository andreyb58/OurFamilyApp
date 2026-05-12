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
import com.example.killer.models.ChatMessage;
import com.example.killer.auth.AuthManager;

public class NewChatDialog extends DialogFragment {

    private OnMessageSentListener listener;
    private AuthManager authManager;
    private int familyId;

    private static final String ARG_FAMILY_ID = "family_id";

    public interface OnMessageSentListener {
        void onMessageSent(ChatMessage message);
    }

    public void setOnMessageSentListener(OnMessageSentListener listener) {
        this.listener = listener;
    }

    public static NewChatDialog newInstance(int familyId) {
        NewChatDialog dialog = new NewChatDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_FAMILY_ID, familyId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_chat, null);

        authManager = AuthManager.getInstance(requireContext());

        if (getArguments() != null) {
            familyId = getArguments().getInt(ARG_FAMILY_ID);
        }

        EditText etMessage = view.findViewById(R.id.et_message);
        Button btnSend = view.findViewById(R.id.btn_send);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (message.isEmpty()) {
                etMessage.setError("Введите сообщение");
                return;
            }

            sendMessage(message);
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void sendMessage(String text) {
        new SendMessageTask().execute(text);
    }

    private class SendMessageTask extends AsyncTask<String, Void, ChatMessage> {

        @Override
        protected ChatMessage doInBackground(String... params) {
            try {
                String message = params[0];

                AppDatabase db = AppDatabase.getDatabase(requireContext());
                int currentUserId = authManager.getCurrentUser().getId();
                String currentUserName = authManager.getCurrentUser().getName();

                // Создаем сообщение
                ChatMessage chatMessage = new ChatMessage(
                        familyId,
                        currentUserId,
                        currentUserName,
                        message
                );

                // Сохраняем в базу
                db.chatMessageDao().insert(chatMessage);

                return chatMessage;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ChatMessage chatMessage) {
            if (chatMessage != null) {
                Toast.makeText(getContext(), "Сообщение отправлено", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onMessageSent(chatMessage);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
            }
        }
    }
}