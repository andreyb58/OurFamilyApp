package com.example.killer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;

import java.util.List;
import java.util.Map;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder> {

    private List<Map<String, Object>> members;
    private Context context;

    public FamilyMemberAdapter(List<Map<String, Object>> members, Context context) {
        this.members = members;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, Object> member = members.get(position);

        String name = (String) member.get("name");
        String role = (String) member.get("role");
        Object pointsRaw = member.get("points");
        int points = pointsRaw instanceof Number ? ((Number) pointsRaw).intValue() : 0;

        holder.tvMemberName.setText(name != null ? name : "Участник");

        // Отображаем роль
        String roleText = "Участник";
        if ("PARENT".equals(role)) {
            roleText = "👑 Родитель";
        } else if ("CHILD".equals(role)) {
            roleText = "👶 Ребенок";
        }
        holder.tvRole.setText(roleText);

        // Отображаем очки
        holder.tvPoints.setText("⭐ " + points + " очков");

        // Генерируем инициалы для аватара
        String initials = generateInitials(name);
        holder.tvAvatarText.setText(initials);
    }

    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "👤";
        }
        String trimmedName = name.trim();
        if (trimmedName.contains(" ")) {
            String[] parts = trimmedName.split(" ");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
            }
        }
        return trimmedName.length() >= 2 ?
                trimmedName.substring(0, 2).toUpperCase() :
                trimmedName.toUpperCase();
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarText;
        TextView tvMemberName;
        TextView tvRole;
        TextView tvPoints;

        public ViewHolder(View itemView) {
            super(itemView);
            tvAvatarText = itemView.findViewById(R.id.tv_avatar_text);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }
    }
}