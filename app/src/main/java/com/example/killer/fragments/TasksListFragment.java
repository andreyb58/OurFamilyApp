package com.example.killer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.adapters.TaskAdapter;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Список заданий.
 * ИСПРАВЛЕНО: теперь загружает задания с сервера (основной источник),
 * локальная БД используется как fallback и кэш.
 */
public class TasksListFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> tasks = new ArrayList<>();
    private TextView tvNoTasks;
    private AuthManager authManager;
    private String currentUserId;
    private TextView tvPointsDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks_list, container, false);

        authManager = AuthManager.getInstance(requireContext());

        if (!authManager.isLoggedIn()) {
            TextView tvNoAuth = new TextView(getContext());
            tvNoAuth.setText("Для просмотра заданий войдите в систему");
            tvNoAuth.setTextSize(16);
            tvNoAuth.setPadding(32, 32, 32, 32);
            return tvNoAuth;
        }

        currentUserId = String.valueOf(authManager.getCurrentUser().getId());

        initViews(view);
        setupAdapter();
        loadTasks();
        updatePointsDisplay();

        return view;
    }

    private void initViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasks_recycler_view);
        tvNoTasks = view.findViewById(R.id.tv_no_tasks);
        tvPointsDisplay = view.findViewById(R.id.tv_points_display);
        if (tvPointsDisplay != null) {
            tvPointsDisplay.setText("💰 Ваши очки: " + authManager.getUserPoints());
        }
    }

    private void setupAdapter() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(tasks, requireContext(), currentUserId);

        taskAdapter.setOnTaskCompletedListener((points, newTotalPoints) -> {
            updatePointsDisplay();
            Toast.makeText(getContext(),
                    "🎉 Получено " + points + " очков! Всего: " + newTotalPoints,
                    Toast.LENGTH_SHORT).show();
            loadTasks();
        });

        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void updatePointsDisplay() {
        if (tvPointsDisplay != null && authManager != null) {
            tvPointsDisplay.setText("💰 Ваши очки: " + authManager.getUserPoints());
        }
    }

    /**
     * ИСПРАВЛЕНО: основной источник данных — сервер, fallback — локальная БД.
     */
    public void loadTasks() {
        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.getTasks("active"), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Map<String, Object> data) {
                List<Map<String, Object>> taskMaps = (List<Map<String, Object>>) data.get("list");
                if (taskMaps == null) taskMaps = new ArrayList<>();

                List<Task> serverTasks = new ArrayList<>();
                for (Map<String, Object> m : taskMaps) {
                    Task t = new Task(
                            ApiUtils.extractString(m.get("title")),
                            ApiUtils.extractString(m.get("description")),
                            ApiUtils.extractInt(m.get("points")),
                            ApiUtils.extractString(m.get("assignedTo")),
                            String.valueOf(ApiUtils.extractInt(m.get("assignedToId")))
                    );
                    t.setId(ApiUtils.extractInt(m.get("id")));
                    t.setCompleted(ApiUtils.extractBool(m.get("isCompleted")));
                    serverTasks.add(t);
                }

                // Синхронизируем в локальную БД асинхронно
                final List<Task> finalTasks = serverTasks;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        AppDatabase db = AppDatabase.getDatabase(requireContext());
                        for (Task t : finalTasks) {
                            try { db.taskDao().insert(t); } catch (Exception ignored) {}
                        }
                        return null;
                    }
                }.execute();

                updateUI(serverTasks);
            }

            @Override
            public void onError(String error) {
                // Fallback: грузим из локальной БД
                new AsyncTask<Void, Void, List<Task>>() {
                    @Override
                    protected List<Task> doInBackground(Void... v) {
                        try {
                            return AppDatabase.getDatabase(requireContext())
                                    .taskDao().getActiveTasksForUser(currentUserId);
                        } catch (Exception e) { return new ArrayList<>(); }
                    }

                    @Override
                    protected void onPostExecute(List<Task> localTasks) {
                        updateUI(localTasks != null ? localTasks : new ArrayList<>());
                    }
                }.execute();
            }
        });
    }

    private void updateUI(List<Task> loaded) {
        tasks.clear();
        if (loaded != null) tasks.addAll(loaded);
        taskAdapter.notifyDataSetChanged();

        boolean empty = tasks.isEmpty();
        tasksRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvNoTasks.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        updatePointsDisplay();
    }
}
