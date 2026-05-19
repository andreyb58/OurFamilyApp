package com.example.killer.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.LoginActivity;
import com.example.killer.R;
import com.example.killer.adapters.CalendarProfileAdapter;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.database.CalendarDao;
import com.example.killer.database.RewardDao;
import com.example.killer.database.TaskDao;
import com.example.killer.models.Calendar;
import com.example.killer.models.Reward;
import com.example.killer.models.Task;
import com.example.killer.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvTotalPoints, avatarText;
    private TextView tvCompletedTasks, tvClaimedRewards, tvCalendarCount, tvLastSync;
    private Button btnCreateCalendar, btnCreateTask, btnCreateReward;
    private ImageButton btnLogout, btnEditProfile;
    private RecyclerView recyclerCalendars;

    private CalendarProfileAdapter calendarAdapter;
    private final List<Calendar> userCalendars = new ArrayList<>();

    private CalendarDao calendarDao;
    private TaskDao taskDao;
    private RewardDao rewardDao;
    private AuthManager authManager;
    private int currentUserId;

    private final String[] avatarColors = {
            "#FF6B6B", "#4ECDC4", "#FFD166", "#06D6A0",
            "#118AB2", "#EF476F", "#073B4C", "#7209B7"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        authManager = AuthManager.getInstance(requireContext());

        if (!authManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Войдите в систему", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = authManager.getCurrentUser().getId();

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        calendarDao = db.calendarDao();
        taskDao     = db.taskDao();
        rewardDao   = db.rewardDao();

        initViews(view);
        loadProfileFromServer();
        loadCalendars();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvUserName       = view.findViewById(R.id.tv_user_name);
        tvUserEmail      = view.findViewById(R.id.tv_user_email);
        tvTotalPoints    = view.findViewById(R.id.tv_total_points);
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);
        tvClaimedRewards = view.findViewById(R.id.tv_claimed_rewards);
        tvCalendarCount  = view.findViewById(R.id.tv_calendar_count);
        tvLastSync       = view.findViewById(R.id.tv_last_sync);
        avatarText       = view.findViewById(R.id.avatar_text);

        btnCreateCalendar = view.findViewById(R.id.btn_create_calendar);
        btnCreateTask     = view.findViewById(R.id.btn_create_task);
        btnCreateReward   = view.findViewById(R.id.btn_create_reward);
        btnLogout         = view.findViewById(R.id.btn_logout);
        btnEditProfile    = view.findViewById(R.id.btn_edit_profile);

        recyclerCalendars = view.findViewById(R.id.recycler_calendars);
        recyclerCalendars.setLayoutManager(new LinearLayoutManager(getContext()));
        calendarAdapter = new CalendarProfileAdapter(userCalendars, requireContext());
        recyclerCalendars.setAdapter(calendarAdapter);

        btnCreateCalendar.setOnClickListener(v -> showCreateCalendarDialog());
        btnCreateTask.setOnClickListener(v -> showCreateTaskDialog());
        btnCreateReward.setOnClickListener(v -> showCreateRewardDialog());
        btnLogout.setOnClickListener(v -> logout());
        if (btnEditProfile != null) btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        updateLastSyncTime();
    }

    /** Загружаем актуальный профиль с сервера */
    private void loadProfileFromServer() {
        ApiService api = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(api.getProfile(), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded() || data == null) return;
                String name  = data.get("name")  != null ? data.get("name").toString()  : "";
                String email = data.get("email") != null ? data.get("email").toString() : "";
                int pts = extractInt(data.get("total_points"));

                // Сохраняем актуальные данные
                authManager.updateUserPoints(pts);
                User user = authManager.getCurrentUser();
                if (user != null) {
                    user.setName(name);
                    user.setEmail(email);
                    user.setTotalPoints(pts);
                    authManager.saveAuthData(authManager.getToken(), user);
                }

                requireActivity().runOnUiThread(() -> {
                    tvUserName.setText(name);
                    tvUserEmail.setText(email);
                    tvTotalPoints.setText(String.valueOf(pts));
                    if (!name.isEmpty()) avatarText.setText(name.substring(0, 1).toUpperCase());
                    setAvatarColor(0);
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return; // фрагмент уже закрыт
                // Показываем из кэша
                requireActivity().runOnUiThread(() -> loadProfileFromCache());
            }
        });
    }

    private void loadProfileFromCache() {
        User user = authManager.getCurrentUser();
        if (user == null) return;
        String name = user.getName() != null ? user.getName() : "Пользователь";
        tvUserName.setText(name);
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvTotalPoints.setText(String.valueOf(user.getTotalPoints()));
        if (!name.isEmpty()) avatarText.setText(name.substring(0, 1).toUpperCase());
        setAvatarColor(0);
    }

    private void setAvatarColor(int idx) {
        try {
            View parent = (View) avatarText.getParent();
            if (parent != null)
                parent.setBackgroundColor(Color.parseColor(avatarColors[idx % avatarColors.length]));
        } catch (Exception ignored) {}
    }

    /** Диалог редактирования профиля */
    private void showEditProfileDialog() {
        User user = authManager.getCurrentUser();
        if (user == null) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        EditText etName = new EditText(requireContext());
        etName.setHint("Имя");
        etName.setText(user.getName());
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(etName);

        EditText etOldPass = new EditText(requireContext());
        etOldPass.setHint("Старый пароль (если меняете)");
        etOldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOldPass);

        EditText etNewPass = new EditText(requireContext());
        etNewPass.setHint("Новый пароль (мин. 6 символов)");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPass);

        new AlertDialog.Builder(requireContext())
                .setTitle("✏️ Редактировать профиль")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String newName    = etName.getText().toString().trim();
                    String oldPass    = etOldPass.getText().toString().trim();
                    String newPass    = etNewPass.getText().toString().trim();
                    saveProfile(newName, oldPass, newPass);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveProfile(String newName, String oldPass, String newPass) {
        ApiService api = ApiClient.getClient(requireContext()).create(ApiService.class);

        // Обновляем имя если изменилось
        User user = authManager.getCurrentUser();
        if (!newName.isEmpty() && (user == null || !newName.equals(user.getName()))) {
            Map<String, Object> body = new HashMap<>();
            body.put("name", newName);
            ApiUtils.makeApiCall(api.updateProfile(body), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    if (!isAdded()) return;
                    User u = authManager.getCurrentUser();
                    if (u != null) {
                        u.setName(newName);
                        authManager.saveAuthData(authManager.getToken(), u);
                    }
                    requireActivity().runOnUiThread(() -> {
                        tvUserName.setText(newName);
                        avatarText.setText(newName.substring(0, 1).toUpperCase());
                        Toast.makeText(getContext(), "✅ Имя обновлено", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }

        // Меняем пароль если заполнено
        if (!oldPass.isEmpty() && !newPass.isEmpty()) {
            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Новый пароль минимум 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("oldPassword", oldPass);
            body.put("newPassword", newPass);
            ApiUtils.makeApiCall(api.changePassword(body), new ApiUtils.ApiCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "✅ Пароль изменён", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Ошибка пароля: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void loadCalendars() {
        new AsyncTask<Void, Void, List<Calendar>>() {
            @Override
            protected List<Calendar> doInBackground(Void... v) {
                return calendarDao.getUserCalendars(String.valueOf(currentUserId));
            }
            @Override
            protected void onPostExecute(List<Calendar> cals) {
                if (!isAdded()) return;
                userCalendars.clear();
                if (cals != null) userCalendars.addAll(cals);
                calendarAdapter.notifyDataSetChanged();
                tvCalendarCount.setText(String.valueOf(userCalendars.size()));
            }
        }.execute();
    }

    private void loadStatistics() {
        // Сначала грузим с сервера
        ApiService api = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(api.getStats(), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded() || data == null) return;
                int done    = extractInt(data.get("completed_tasks"));
                int claimed = extractInt(data.get("claimed_rewards"));
                requireActivity().runOnUiThread(() -> {
                    tvCompletedTasks.setText(String.valueOf(done));
                    tvClaimedRewards.setText(String.valueOf(claimed));
                });
            }
            @Override
            public void onError(String error) {
                // Fallback: локальная БД
                new AsyncTask<Void, Void, int[]>() {
                    @Override
                    protected int[] doInBackground(Void... v) {
                        int done = 0, claimed = 0;
                        try {
                            List<Task>   tasks   = taskDao.getCompletedTasksForUser(String.valueOf(currentUserId));
                            List<Reward> rewards = rewardDao.getClaimedRewardsByUser(String.valueOf(currentUserId));
                            done    = tasks   != null ? tasks.size()   : 0;
                            claimed = rewards != null ? rewards.size() : 0;
                        } catch (Exception ignored) {}
                        return new int[]{done, claimed};
                    }
                    @Override
                    protected void onPostExecute(int[] res) {
                        if (!isAdded()) return; // фрагмент уже закрыт
                        tvCompletedTasks.setText(String.valueOf(res[0]));
                        tvClaimedRewards.setText(String.valueOf(res[1]));
                    }
                }.execute();
            }
        });
    }

    private void updateLastSyncTime() {
        tvLastSync.setText("Синхронизация: " +
                new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void showCreateCalendarDialog() {
        com.example.killer.dialogs.CreateCalendarDialog d = new com.example.killer.dialogs.CreateCalendarDialog();
        d.setOnCalendarCreatedListener(() -> {
            loadCalendars();
            Toast.makeText(getContext(), "Календарь создан", Toast.LENGTH_SHORT).show();
        });
        d.show(getParentFragmentManager(), "CreateCalendarDialog");
    }

    private void showCreateTaskDialog() {
        com.example.killer.dialogs.AddTaskDialog d = new com.example.killer.dialogs.AddTaskDialog();
        d.setOnTaskAddedListener(() -> {
            loadStatistics();
            Toast.makeText(getContext(), "Задание создано", Toast.LENGTH_SHORT).show();
        });
        d.show(getParentFragmentManager(), "AddTaskDialog");
    }

    private void showCreateRewardDialog() {
        com.example.killer.dialogs.AddRewardDialog d = new com.example.killer.dialogs.AddRewardDialog();
        d.setOnRewardAddedListener(() -> {
            loadStatistics();
            Toast.makeText(getContext(), "Награда создана", Toast.LENGTH_SHORT).show();
        });
        d.show(getParentFragmentManager(), "AddRewardDialog");
    }

    private void logout() {
        authManager.logout(() -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });
    }

    private int extractInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double)  return ((Double) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileFromServer();
        loadStatistics();
    }
}