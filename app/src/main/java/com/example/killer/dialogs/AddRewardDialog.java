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
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Reward;

import java.util.HashMap;
import java.util.Map;

/**
 * Диалог добавления награды.
 * ИСПРАВЛЕНО: сохраняет награду на сервер через API, затем синхронизирует локально.
 */
public class AddRewardDialog extends DialogFragment {

    private OnRewardAddedListener listener;
    private Button btnSave;

    public interface OnRewardAddedListener {
        void onRewardAdded();
    }

    public void setOnRewardAddedListener(OnRewardAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_add_reward, null);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etCost = view.findViewById(R.id.et_cost);
        btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String costStr = etCost.getText().toString().trim();

            if (title.isEmpty()) { etTitle.setError("Введите название"); return; }
            if (costStr.isEmpty()) { etCost.setError("Введите стоимость"); return; }

            try {
                int cost = Integer.parseInt(costStr);
                if (cost <= 0) { etCost.setError("Стоимость > 0"); return; }
                saveRewardToServer(title, description, cost);
            } catch (NumberFormatException e) {
                etCost.setError("Введите число");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    /**
     * ИСПРАВЛЕНО: сначала сохраняем на сервер, потом синхронизируем локально.
     */
    private void saveRewardToServer(String title, String description, int cost) {
        if (btnSave != null) btnSave.setEnabled(false);

        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("description", description);
        request.put("cost", cost);

        ApiUtils.makeApiCall(apiService.createReward(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                int serverId = ApiUtils.extractInt(data.get("id"));

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        Reward reward = new Reward(title, description, cost);
                        if (serverId > 0) reward.setId(serverId);
                        AppDatabase.getDatabase(requireContext()).rewardDao().insert(reward);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        if (btnSave != null) btnSave.setEnabled(true);
                        Toast.makeText(getContext(), " Награда '" + title + "' добавлена!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onRewardAdded();
                        dismiss();
                    }
                }.execute();
            }

            @Override
            public void onError(String error) {
                if (btnSave != null) btnSave.setEnabled(true);
                Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
