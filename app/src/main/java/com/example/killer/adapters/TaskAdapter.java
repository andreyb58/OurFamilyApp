package com.example.killer.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Task;
import com.example.killer.models.User;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> tasks;
    private Context context;
    private String currentUserId;
    private AuthManager authManager;
    private OnTaskCompletedListener listener;
    private static final String TAG = "TaskAdapter";

    public interface OnTaskCompletedListener {
        void onTaskCompleted(int points, int newTotalPoints);
    }

    public TaskAdapter(List<Task> tasks, Context context, String currentUserId) {
        this.tasks = tasks;
        this.context = context;
        this.currentUserId = currentUserId;
        this.authManager = AuthManager.getInstance(context);
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription() != null ? task.getDescription() : "");
        holder.tvPoints.setText("+" + task.getPoints() + " очков");

        String assignedTo = task.getAssignedTo();
        if (assignedTo == null || assignedTo.isEmpty() || assignedTo.equals("null")) {
            holder.tvAssignedTo.setText(" Для всех");
        } else {
            holder.tvAssignedTo.setText(" Для: " + assignedTo);
        }

        holder.btnComplete.setOnClickListener(v -> completeTask(task, position));
        holder.btnDelete.setOnClickListener(v -> deleteTask(task, position));
    }

    private void completeTask(Task task, int position) {
        new CompleteTaskTask(task, position).execute();
    }

    private class CompleteTaskTask extends AsyncTask<Void, Void, TaskCompletionResult> {
        private Task task;
        private int position;

        public CompleteTaskTask(Task task, int position) {
            this.task = task;
            this.position = position;
        }

        @Override
        protected TaskCompletionResult doInBackground(Void... voids) {
            try {
                Log.d(TAG, "Выполнение задания ID: " + task.getId());

                // 1. Сначала отправляем запрос на сервер
                ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
                String token = authManager.getToken();

                if (token == null) {
                    return new TaskCompletionResult(false, "Токен авторизации отсутствует", 0, 0);
                }

                Log.d(TAG, "Отправка запроса на сервер с токеном: " + token.substring(0, Math.min(10, token.length())) + "...");

                Call<Map<String, Object>> call = apiService.completeTask(task.getId());
                Response<Map<String, Object>> response = call.execute();

                int newTotalPoints = 0;
                boolean serverSuccess = false;

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Boolean success = (Boolean) body.get("success");

                    if (success != null && success) {
                        serverSuccess = true;
                        Map<String, Object> data = (Map<String, Object>) body.get("data");
                        if (data != null) {
                            Object pointsObj = data.get("newTotalPoints");
                            if (pointsObj instanceof Double) {
                                newTotalPoints = ((Double) pointsObj).intValue();
                            } else if (pointsObj instanceof Integer) {
                                newTotalPoints = (Integer) pointsObj;
                            } else if (pointsObj instanceof String) {
                                newTotalPoints = Integer.parseInt((String) pointsObj);
                            }
                            Log.d(TAG, "Сервер вернул новые очки: " + newTotalPoints);
                        }
                    } else {
                        String error = (String) body.get("error");
                        Log.e(TAG, "Ошибка сервера: " + error);
                        return new TaskCompletionResult(false, "Ошибка сервера: " + error, 0, 0);
                    }
                } else {
                    Log.e(TAG, "Ошибка HTTP: " + response.code() + " - " + response.message());
                    if (response.code() == 401) {
                        return new TaskCompletionResult(false, "Ошибка авторизации. Попробуйте выйти и зайти снова", 0, 0);
                    } else if (response.code() == 404) {
                        return new TaskCompletionResult(false, "Задание не найдено на сервере", 0, 0);
                    } else {
                        return new TaskCompletionResult(false, "Ошибка сервера: " + response.code(), 0, 0);
                    }
                }

                // 2. Если сервер успешно обработал, обновляем локальную БД
                if (serverSuccess) {
                    AppDatabase db = AppDatabase.getDatabase(context);

                    // Обновляем задание
                    task.setCompleted(true);
                    task.setCompletedDate(new Date());
                    task.setPointsAwarded(true);
                    db.taskDao().update(task);

                    // Получаем ID пользователя, которому назначено задание
                    int assignedToId;
                    try {
                        String assignedToIdStr = task.getAssignedToId();
                        if (assignedToIdStr != null && !assignedToIdStr.isEmpty() && !assignedToIdStr.equals("null")) {
                            assignedToId = Integer.parseInt(assignedToIdStr);
                        } else {
                            assignedToId = authManager.getUserId();
                        }
                    } catch (NumberFormatException e) {
                        assignedToId = authManager.getUserId();
                    }

                    // Получаем пользователя
                    User user = db.userDao().getUserById(assignedToId);
                    if (user == null) {
                        user = authManager.getCurrentUser();
                    }

                    if (user != null) {
                        // Обновляем очки в БД
                        int points = task.getPoints();
                        if (newTotalPoints == 0) {
                            newTotalPoints = user.getTotalPoints() + points;
                        }
                        user.setTotalPoints(newTotalPoints);
                        db.userDao().update(user);

                        // Обновляем AuthManager если это текущий пользователь
                        if (assignedToId == authManager.getUserId()) {
                            authManager.updateUserPoints(newTotalPoints);
                        }

                        return new TaskCompletionResult(true, "Задание выполнено!", points, newTotalPoints);
                    }
                }

                return new TaskCompletionResult(false, "Не удалось выполнить задание", 0, 0);

            } catch (IOException e) {
                Log.e(TAG, "Ошибка сети: " + e.getMessage());
                return new TaskCompletionResult(false, "Ошибка сети: " + e.getMessage(), 0, 0);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка: " + e.getMessage(), e);
                return new TaskCompletionResult(false, "Ошибка: " + e.getMessage(), 0, 0);
            }
        }

        @Override
        protected void onPostExecute(TaskCompletionResult result) {
            if (result.success) {
                tasks.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tasks.size());

                Toast.makeText(context,
                        " Задание выполнено! +" + result.points + " очков",
                        Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onTaskCompleted(result.points, result.newTotalPoints);
                }
            } else {
                Toast.makeText(context, " " + result.message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class TaskCompletionResult {
        boolean success;
        String message;
        int points;
        int newTotalPoints;

        TaskCompletionResult(boolean success, String message, int points, int newTotalPoints) {
            this.success = success;
            this.message = message;
            this.points = points;
            this.newTotalPoints = newTotalPoints;
        }
    }

    private void deleteTask(Task task, int position) {
        new AsyncTask<Task, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Task... tasks) {
                try {
                    // Сначала удаляем на сервере
                    ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
                    String token = authManager.getToken();

                    if (token != null) {
                        Call<Map<String, Object>> call = apiService.deleteTask(tasks[0].getId());
                        Response<Map<String, Object>> response = call.execute();

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> body = response.body();
                            Boolean success = (Boolean) body.get("success");
                            if (success != null && success) {
                                // Удаляем локально
                                AppDatabase.getDatabase(context).taskDao().delete(tasks[0]);
                                return true;
                            }
                        }
                    }

                    // Если не удалось удалить на сервере, удаляем только локально
                    AppDatabase.getDatabase(context).taskDao().delete(tasks[0]);
                    return true;
                } catch (Exception e) {
                    // Если ошибка сети, удаляем локально
                    try {
                        AppDatabase.getDatabase(context).taskDao().delete(tasks[0]);
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    TaskAdapter.this.tasks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, TaskAdapter.this.tasks.size());
                    Toast.makeText(context, "Задание удалено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ошибка удаления задания", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvPoints, tvAssignedTo;
        Button btnComplete;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPoints = itemView.findViewById(R.id.tv_points);
            tvAssignedTo = itemView.findViewById(R.id.tv_assigned_to);
            btnComplete = itemView.findViewById(R.id.btn_complete);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}