package com.example.killer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.killer.R;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Фрагмент достижений и рейтинга (лидерборд).
 * НОВАЯ ФИЧА: реализован полностью.
 */
public class AchievementsFragment extends Fragment {

    private RecyclerView rvAchievements;
    private RecyclerView rvLeaderboard;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvNoAchievements;
    private TextView tvAchievementsTitle;

    private AuthManager authManager;
    private ApiService apiService;

    // Простые адаптеры прямо в фрагменте для компактности
    private List<Map<String, Object>> achievements = new ArrayList<>();
    private List<Map<String, Object>> leaderboard = new ArrayList<>();
    private AchievementAdapter achievementAdapter;
    private LeaderboardAdapter leaderboardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        authManager = AuthManager.getInstance(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        rvAchievements  = view.findViewById(R.id.rv_achievements);
        rvLeaderboard   = view.findViewById(R.id.rv_leaderboard);
        swipeRefresh    = view.findViewById(R.id.swipe_refresh);
        tvNoAchievements= view.findViewById(R.id.tv_no_achievements);

        rvAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        achievementAdapter = new AchievementAdapter(achievements);
        rvAchievements.setAdapter(achievementAdapter);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardAdapter = new LeaderboardAdapter(leaderboard, authManager.getUserId());
        rvLeaderboard.setAdapter(leaderboardAdapter);

        swipeRefresh.setOnRefreshListener(() -> {
            loadAll();
        });

        if (!authManager.isLoggedIn()) {
            tvNoAchievements.setText("Войдите в систему");
            tvNoAchievements.setVisibility(View.VISIBLE);
            return view;
        }

        loadAll();
        return view;
    }

    private void loadAll() {
        loadLeaderboard();
        loadAchievements();
    }

    private void loadLeaderboard() {
        ApiUtils.makeApiCall(apiService.getLeaderboard(),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        List<Map<String, Object>> list =
                                (List<Map<String, Object>>) data.get("list");
                        leaderboard.clear();
                        if (list != null) leaderboard.addAll(list);
                        leaderboardAdapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(String error) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void loadAchievements() {
        ApiUtils.makeApiCall(apiService.getAchievements(null),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        List<Map<String, Object>> list =
                                (List<Map<String, Object>>) data.get("list");
                        achievements.clear();
                        if (list != null) achievements.addAll(list);
                        achievementAdapter.notifyDataSetChanged();

                        if (tvNoAchievements != null) {
                            tvNoAchievements.setVisibility(
                                    achievements.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onError(String error) { /* тихо */ }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    // ====== Адаптер лидерборда ======

    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {
        private final List<Map<String, Object>> items;
        private final int myId;

        LeaderboardAdapter(List<Map<String, Object>> items, int myId) {
            this.items = items;
            this.myId = myId;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Map<String, Object> item = items.get(pos);
            int rank  = ApiUtils.extractInt(item.get("rank"));
            int id    = ApiUtils.extractInt(item.get("id"));
            String name   = ApiUtils.extractString(item.get("name"));
            int pts   = ApiUtils.extractInt(item.get("total_points"));
            int tasks = ApiUtils.extractInt(item.get("completed_tasks"));

            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉"
                    : rank + ".";
            h.tvRank.setText(medal);
            h.tvName.setText(name + (id == myId ? " (Вы)" : ""));
            h.tvPoints.setText(pts + " очков · " + tasks + " заданий");

            // Выделяем текущего пользователя
            h.itemView.setAlpha(id == myId ? 1f : 0.85f);
            if (id == myId) {
                h.itemView.setBackgroundResource(R.drawable.bg_selected_day);
            } else {
                h.itemView.setBackground(null);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvPoints;
            VH(View v) {
                super(v);
                tvRank   = v.findViewById(R.id.tv_rank);
                tvName   = v.findViewById(R.id.tv_name);
                tvPoints = v.findViewById(R.id.tv_points);
            }
        }
    }

    // ====== Адаптер достижений ======

    static class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.VH> {
        private final List<Map<String, Object>> items;

        AchievementAdapter(List<Map<String, Object>> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Map<String, Object> item = items.get(pos);
            h.tvTitle.setText("🏅 " + ApiUtils.extractString(item.get("title")));
            h.tvDescription.setText(ApiUtils.extractString(item.get("description")));
            String earnedAt = ApiUtils.extractString(item.get("earnedAt"));
            if (earnedAt.length() > 10) earnedAt = earnedAt.substring(0, 10);
            h.tvDate.setText(earnedAt);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription, tvDate;
            VH(View v) {
                super(v);
                tvTitle       = v.findViewById(R.id.tv_achievement_title);
                tvDescription = v.findViewById(R.id.tv_achievement_description);
                tvDate        = v.findViewById(R.id.tv_achievement_date);
            }
        }
    }
}
