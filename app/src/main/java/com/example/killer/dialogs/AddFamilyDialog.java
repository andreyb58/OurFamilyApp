package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;

import com.example.killer.R;
import com.example.killer.auth.AuthManager;
import com.example.killer.managers.FamilyManager;
import com.example.killer.models.User;

import java.util.Map;

/**
 * Диалог создания семьи.
 * ИСПРАВЛЕНО: создаёт семью через API на сервере (раньше только локально).
 */
public class AddFamilyDialog extends DialogFragment {

    private OnFamilyCreatedListener listener;
    private Button btnCreate;

    public interface OnFamilyCreatedListener {
        void onFamilyCreated();
    }

    public void setOnFamilyCreatedListener(OnFamilyCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_add_family, null);

        EditText etFamilyName = view.findViewById(R.id.et_family_name);
        EditText etDescription = view.findViewById(R.id.et_description);
        btnCreate = view.findViewById(R.id.btn_create);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnCreate.setOnClickListener(v -> {
            String name = etFamilyName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (name.isEmpty()) { etFamilyName.setError("Введите название семьи"); return; }
            createFamilyViaApi(name, description);
        });

        btnCancel.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    /**
     * ИСПРАВЛЕНО: создаём семью через FamilyManager (API), не через локальную БД.
     */
    private void createFamilyViaApi(String name, String description) {
        if (btnCreate != null) btnCreate.setEnabled(false);

        FamilyManager.getInstance(requireContext()).createFamily(name, description,
                new FamilyManager.FamilyCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (btnCreate != null) btnCreate.setEnabled(true);

                        // Обновляем family_id в AuthManager
                        Object idObj = data.get("id");
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

                        Toast.makeText(getContext(), "🏠 Семья создана!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onFamilyCreated();
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        if (btnCreate != null) btnCreate.setEnabled(true);
                        Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
