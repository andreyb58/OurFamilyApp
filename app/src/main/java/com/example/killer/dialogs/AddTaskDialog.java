package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;

import com.example.killer.R;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Task;
import com.example.killer.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Диалог добавления задания.
 * ИСПРАВЛЕНО: теперь сохраняет задание на сервер через API, затем синхронизирует локальную БД.
 */
public class AddTaskDialog extends DialogFragment {

    private OnTaskAddedListener listener;
    private AuthManager authManager;
    private Calendar selectedDate;
    private EditText etDueDate;
    private Spinner spinnerAssignTo;
    private List<User> familyMembers = new ArrayList<>();
    private ProgressBar progressBar;
    private Button btnSave;

    public interface OnTaskAddedListener {
        void onTaskAdded();
    }

    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);

        authManager = AuthManager.getInstance(requireContext());
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, 1);

        initViews(view);
        loadFamilyMembersFromServer();
        updateDateField();

        return builder.setView(view).create();
    }

    private void initViews(View view) {
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etPoints = view.findViewById(R.id.et_points);
        etDueDate = view.findViewById(R.id.et_due_date);
        spinnerAssignTo = view.findViewById(R.id.spinner_assign_to);
        Button btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSave = view.findViewById(R.id.btn_save);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String pointsStr = etPoints.getText().toString().trim();

            String assignedTo = null;
            if (spinnerAssignTo != null && spinnerAssignTo.getSelectedItem() != null) {
                assignedTo = spinnerAssignTo.getSelectedItem().toString();
            }

            if (title.isEmpty()) {
                etTitle.setError("Введите название задания");
                etTitle.requestFocus();
                return;
            }
            if (pointsStr.isEmpty()) {
                etPoints.setError("Введите количество очков");
                etPoints.requestFocus();
                return;
            }
            if (assignedTo == null || assignedTo.isEmpty()) {
                Toast.makeText(getContext(), "Выберите, кому назначить задание", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int points = Integer.parseInt(pointsStr);
                if (points <= 0) {
                    etPoints.setError("Очки должны быть больше 0");
                    return;
                }

                int assignedToId = findUserIdByName(assignedTo);
                saveTaskToServer(title, description, points, assignedTo, assignedToId);
            } catch (NumberFormatException e) {
                etPoints.setError("Введите число");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    /**
     * ИСПРАВЛЕНО: Сохраняем задание через API на сервер, потом синхронизируем локально.
     */
    private void saveTaskToServer(String title, String description, int points,
                                  String assignedTo, int assignedToId) {
        if (btnSave != null) btnSave.setEnabled(false);

        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("description", description);
        request.put("points", points);
        request.put("assignedTo", assignedTo);
        request.put("assignedToId", assignedToId);

        ApiUtils.makeApiCall(apiService.createTask(request), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                // Задание создано на сервере — теперь синхронизируем локальную БД
                int serverId = ApiUtils.extractInt(data.get("id"));
                int finalPts = ApiUtils.extractInt(data.get("points"));
                if (finalPts == 0) finalPts = points;

                final int taskId = serverId;
                final int taskPoints = finalPts;
                final String taskAssignedTo = assignedTo;
                final int taskAssignedToId = assignedToId;

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        Task task = new Task(title, description, taskPoints, taskAssignedTo,
                                String.valueOf(taskAssignedToId));
                        task.setId(taskId);
                        task.setDueDate(selectedDate.getTime());
                        AppDatabase.getDatabase(requireContext()).taskDao().insert(task);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        if (btnSave != null) btnSave.setEnabled(true);
                        Toast.makeText(getContext(), " Задание добавлено!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onTaskAdded();
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

    /**
     * ИСПРАВЛЕНО: загружаем участников семьи с сервера (не только из локальной БД).
     */
    private void loadFamilyMembersFromServer() {
        // Сначала пробуем загрузить с сервера
        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.getFamilyMembers(), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Map<String, Object> data) {
                List<Map<String, Object>> memberMaps = (List<Map<String, Object>>) data.get("list");
                if (memberMaps != null && !memberMaps.isEmpty()) {
                    familyMembers.clear();
                    for (Map<String, Object> m : memberMaps) {
                        User u = new User();
                        u.setId(ApiUtils.extractInt(m.get("id")));
                        u.setName(ApiUtils.extractString(m.get("name")));
                        u.setRole(ApiUtils.extractString(m.get("role")));
                        familyMembers.add(u);
                    }
                    updateSpinner();
                } else {
                    loadFamilyMembersFromDb(); // Fallback к локальной БД
                }
            }

            @Override
            public void onError(String error) {
                loadFamilyMembersFromDb(); // Fallback к локальной БД
            }
        });
    }

    private void loadFamilyMembersFromDb() {
        new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(Void... voids) {
                try {
                    if (authManager == null || !authManager.isLoggedIn()) return new ArrayList<>();
                    User currentUser = authManager.getCurrentUser();
                    if (currentUser == null) return new ArrayList<>();
                    return AppDatabase.getDatabase(requireContext())
                            .userDao().getFamilyMembers(currentUser.getFamilyId());
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<User> members) {
                familyMembers.clear();
                if (members != null && !members.isEmpty()) {
                    familyMembers.addAll(members);
                } else if (authManager != null && authManager.isLoggedIn()) {
                    familyMembers.add(authManager.getCurrentUser());
                }
                updateSpinner();
            }
        }.execute();
    }

    private void updateSpinner() {
        if (getContext() == null || spinnerAssignTo == null) return;
        List<String> names = new ArrayList<>();
        for (User u : familyMembers) names.add(u.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(adapter);

        // Выбираем текущего пользователя по умолчанию
        if (authManager != null && authManager.isLoggedIn()) {
            int myId = authManager.getUserId();
            for (int i = 0; i < familyMembers.size(); i++) {
                if (familyMembers.get(i).getId() == myId) {
                    spinnerAssignTo.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField() {
        etDueDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(selectedDate.getTime()));
    }

    private int findUserIdByName(String name) {
        for (User member : familyMembers) {
            if (member.getName().equals(name)) return member.getId();
        }
        return authManager != null && authManager.isLoggedIn()
                ? authManager.getCurrentUser().getId() : 0;
    }
}
