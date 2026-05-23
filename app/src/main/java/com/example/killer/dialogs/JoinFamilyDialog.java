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
import com.example.killer.auth.AuthManager;
import com.example.killer.managers.FamilyManager;
import com.example.killer.models.User;

import java.util.Map;

/**
 * Диалог присоединения к семье по коду.
 * ИСПРАВЛЕНО: использует API (раньше только локальная БД, код не находил семью с сервера).
 */
public class JoinFamilyDialog extends DialogFragment {

    private OnFamilyJoinedListener listener;
    private Button btnJoin;

    public interface OnFamilyJoinedListener {
        void onFamilyJoined();
    }

    public void setOnFamilyJoinedListener(OnFamilyJoinedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_join_family, null);

        EditText etInviteCode = view.findViewById(R.id.et_invite_code);
        btnJoin = view.findViewById(R.id.btn_join);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnJoin.setOnClickListener(v -> {
            String code = etInviteCode.getText().toString().trim().toUpperCase();
            if (code.isEmpty()) { etInviteCode.setError("Введите код приглашения"); return; }
            joinFamilyViaApi(code);
        });

        btnCancel.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    /**
     * ИСПРАВЛЕНО: присоединяемся через FamilyManager (API).
     */
    private void joinFamilyViaApi(String inviteCode) {
        if (btnJoin != null) btnJoin.setEnabled(false);

        FamilyManager.getInstance(requireContext()).joinFamily(inviteCode,
                new FamilyManager.FamilyCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (btnJoin != null) btnJoin.setEnabled(true);

                        // Обновляем family_id в AuthManager
                        Object idObj = data.get("familyId");
                        if (idObj != null) {
                            int familyId = (idObj instanceof Double) ?
                                    ((Double) idObj).intValue() : (Integer) idObj;
                            AuthManager authManager = AuthManager.getInstance(requireContext());
                            User user = authManager.getCurrentUser();
                            if (user != null) {
                                user.setFamilyId(familyId);
                                authManager.saveAuthData(authManager.getToken(), user);
                            }
                        }

                        String famName = data.get("familyName") != null ?
                                data.get("familyName").toString() : "семье";
                        Toast.makeText(getContext(),
                                " Вы присоединились к " + famName + "!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onFamilyJoined();
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        if (btnJoin != null) btnJoin.setEnabled(true);
                        Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
