package com.example.killer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.adapters.RewardAdapter;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Reward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Фрагмент списка наград.
 * ИСПРАВЛЕНО: загружает награды с сервера (основной источник).
 */
public class RewardsFragment extends Fragment {

    private RecyclerView rewardsRecyclerView;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards = new ArrayList<>();
    private View emptyState;
    private TextView tvPointsInfo;
    private AuthManager authManager;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);
        authManager = AuthManager.getInstance(requireContext());

        if (!authManager.isLoggedIn()) {
            TextView tvNoAuth = new TextView(getContext());
            tvNoAuth.setText("Для просмотра наград войдите в систему");
            tvNoAuth.setTextSize(16);
            tvNoAuth.setPadding(32, 32, 32, 32);
            return tvNoAuth;
        }

        currentUserId = String.valueOf(authManager.getCurrentUser().getId());

        rewardsRecyclerView = view.findViewById(R.id.rewards_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        tvPointsInfo = view.findViewById(R.id.tv_points_info);

        rewardsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rewardAdapter = new RewardAdapter(rewards, requireContext(), currentUserId);
        rewardsRecyclerView.setAdapter(rewardAdapter);

        rewardAdapter.setOnRewardClaimedListener(() -> {
            // Обновляем очки с сервера после получения награды
            loadUserPointsFromServer();
            loadRewards();
        });

        loadUserPointsFromServer();
        loadRewards();

        return view;
    }

    /**
     * ИСПРАВЛЕНО: очки берём с сервера (актуальные после получения наград).
     */
    private void loadUserPointsFromServer() {
        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.getProfile(), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                int pts = ApiUtils.extractInt(data.get("total_points"));
                authManager.updateUserPoints(pts);
                if (tvPointsInfo != null) {
                    tvPointsInfo.setText(" Ваши очки: " + pts);
                }
            }

            @Override
            public void onError(String error) {
                // Fallback из SharedPreferences
                if (tvPointsInfo != null) {
                    tvPointsInfo.setText(" Ваши очки: " + authManager.getUserPoints());
                }
            }
        });
    }

    /**
     * ИСПРАВЛЕНО: загружаем награды с сервера, fallback — локальная БД.
     */
    public void loadRewards() {
        if (currentUserId == null) return;

        ApiService apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.getRewards(null), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Map<String, Object> data) {
                List<Map<String, Object>> rewardMaps = (List<Map<String, Object>>) data.get("list");
                if (rewardMaps == null) rewardMaps = new ArrayList<>();

                List<Reward> serverRewards = new ArrayList<>();
                for (Map<String, Object> m : rewardMaps) {
                    Reward r = new Reward(
                            ApiUtils.extractString(m.get("title")),
                            ApiUtils.extractString(m.get("description")),
                            ApiUtils.extractInt(m.get("cost"))
                    );
                    r.setId(ApiUtils.extractInt(m.get("id")));
                    r.setClaimed(ApiUtils.extractBool(m.get("isClaimed")));
                    r.setClaimedBy(ApiUtils.extractString(m.get("claimedBy")));
                    serverRewards.add(r);
                }

                final List<Reward> finalRewards = serverRewards;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        AppDatabase db = AppDatabase.getDatabase(requireContext());
                        for (Reward r : finalRewards) {
                            try { db.rewardDao().insert(r); } catch (Exception ignored) {}
                        }
                        return null;
                    }
                }.execute();

                updateUI(serverRewards);
            }

            @Override
            public void onError(String error) {
                // Fallback: локальная БД
                new AsyncTask<Void, Void, List<Reward>>() {
                    @Override
                    protected List<Reward> doInBackground(Void... v) {
                        try {
                            return AppDatabase.getDatabase(requireContext())
                                    .rewardDao().getAvailableRewards();
                        } catch (Exception e) { return new ArrayList<>(); }
                    }

                    @Override
                    protected void onPostExecute(List<Reward> local) {
                        updateUI(local != null ? local : new ArrayList<>());
                    }
                }.execute();
            }
        });
    }

    private void updateUI(List<Reward> loaded) {
        rewards.clear();
        if (loaded != null) rewards.addAll(loaded);
        rewardAdapter.notifyDataSetChanged();

        boolean empty = rewards.isEmpty();
        rewardsRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    public void updatePointsInfo() {
        loadUserPointsFromServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserPointsFromServer();
        loadRewards();
    }
}
