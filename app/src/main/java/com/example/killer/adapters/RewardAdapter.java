package com.example.killer.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.database.AppDatabase;
import com.example.killer.models.Reward;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Адаптер наград.
 * ИСПРАВЛЕНО: claim и delete теперь идут через API на сервер,
 * а не только в локальную БД.
 */
public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

    private final List<Reward> rewards;
    private final Context context;
    private final String currentUserId;
    private final AuthManager authManager;
    private OnRewardClaimedListener listener;

    public interface OnRewardClaimedListener {
        void onRewardClaimed();
    }

    public RewardAdapter(List<Reward> rewards, Context context, String currentUserId) {
        this.rewards = rewards;
        this.context = context;
        this.currentUserId = currentUserId;
        this.authManager = AuthManager.getInstance(context);
    }

    public void setOnRewardClaimedListener(OnRewardClaimedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Reward reward = rewards.get(position);

        h.tvTitle.setText(reward.getTitle());
        h.tvDescription.setText(reward.getDescription() != null ? reward.getDescription() : "");
        h.tvCost.setText(reward.getCost() + " очков");

        if (reward.isClaimed()) {
            h.btnClaim.setVisibility(View.GONE);
            h.tvStatus.setVisibility(View.VISIBLE);
            if (h.tvClaimDate != null) h.tvClaimDate.setVisibility(View.GONE);
            if (h.btnDelete != null) h.btnDelete.setVisibility(View.VISIBLE);

            String claimedBy = reward.getClaimedBy() != null ? reward.getClaimedBy() : "Кто-то";
            h.tvStatus.setText("✅ Получено: " + claimedBy);
        } else {
            h.btnClaim.setVisibility(View.VISIBLE);
            h.tvStatus.setVisibility(View.GONE);
            if (h.tvClaimDate != null) h.tvClaimDate.setVisibility(View.GONE);
            if (h.btnDelete != null) h.btnDelete.setVisibility(View.GONE);

            // Проверяем, хватает ли очков
            int myPoints = authManager.getUserPoints();
            boolean canAfford = myPoints >= reward.getCost();
            h.btnClaim.setEnabled(canAfford);
            h.btnClaim.setAlpha(canAfford ? 1f : 0.5f);
            h.btnClaim.setText(canAfford ? "ПОЛУЧИТЬ" : "Нужно " + reward.getCost() + " очков");
        }

        h.btnClaim.setOnClickListener(v -> claimRewardViaApi(reward, position, h.btnClaim));

        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> deleteRewardViaApi(reward, position));
        }
    }

    /**
     * ИСПРАВЛЕНО: получаем награду через API (не только локально).
     */
    private void claimRewardViaApi(Reward reward, int position, Button btnClaim) {
        btnClaim.setEnabled(false);

        ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.claimReward(reward.getId()),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        int newPoints = ApiUtils.extractInt(data.get("newTotalPoints"));

                        // Обновляем очки локально
                        authManager.updateUserPoints(newPoints);

                        // Обновляем локальную БД
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... v) {
                                try {
                                    reward.setClaimed(true);
                                    reward.setClaimedBy(authManager.getUserName());
                                    reward.setClaimDate(new Date());
                                    AppDatabase.getDatabase(context).rewardDao().update(reward);
                                } catch (Exception ignored) {}
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void v) {
                                reward.setClaimed(true);
                                reward.setClaimedBy(authManager.getUserName());
                                notifyItemChanged(position);

                                Toast.makeText(context,
                                        "🎁 Награда получена! Осталось: " + newPoints + " очков",
                                        Toast.LENGTH_SHORT).show();

                                if (listener != null) listener.onRewardClaimed();
                            }
                        }.execute();
                    }

                    @Override
                    public void onError(String error) {
                        btnClaim.setEnabled(true);
                        Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * ИСПРАВЛЕНО: удаляем награду через API.
     */
    private void deleteRewardViaApi(Reward reward, int position) {
        ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
        ApiUtils.makeApiCall(apiService.deleteReward(reward.getId()),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... v) {
                                try { AppDatabase.getDatabase(context).rewardDao().delete(reward); }
                                catch (Exception ignored) {}
                                return null;
                            }
                        }.execute();

                        rewards.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, rewards.size());
                        Toast.makeText(context, "Награда удалена", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        // Если не нашли на сервере — всё равно удаляем локально
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... v) {
                                try { AppDatabase.getDatabase(context).rewardDao().delete(reward); }
                                catch (Exception ignored) {}
                                return null;
                            }
                        }.execute();
                        rewards.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, rewards.size());
                    }
                });
    }

    @Override
    public int getItemCount() { return rewards.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvCost, tvStatus, tvClaimDate;
        Button btnClaim;
        ImageButton btnDelete;

        public ViewHolder(View v) {
            super(v);
            tvTitle       = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvCost        = v.findViewById(R.id.tv_cost);
            tvStatus      = v.findViewById(R.id.tv_status);
            tvClaimDate   = v.findViewById(R.id.tv_claim_date);
            btnClaim      = v.findViewById(R.id.btn_claim);
            btnDelete     = v.findViewById(R.id.btn_delete);
        }
    }
}
