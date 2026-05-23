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

    public interface OnMemberClickListener {
        void onMemberClick(Map<String, Object> member, int position);
    }

    private final List<Map<String, Object>> members;
    private final Context context;
    private OnMemberClickListener clickListener;
    private boolean isOwner = false;

    public FamilyMemberAdapter(List<Map<String, Object>> members, Context context) {
        this.members = members;
        this.context = context;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.clickListener = listener;
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

        String roleText = "PARENT".equals(role) ? "Родитель" : "Ребенок";
        holder.tvRole.setText(roleText);
        holder.tvPoints.setText(points + " очков");

        String initials = generateInitials(name);
        holder.tvAvatarText.setText(initials);

        // Клик для смены роли (только создателю семьи)
        if (isOwner && clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onMemberClick(member, position));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String trimmedName = name.trim();
        if (trimmedName.contains(" ")) {
            String[] parts = trimmedName.split(" ");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
            }
        }
        return trimmedName.length() >= 2
                ? trimmedName.substring(0, 2).toUpperCase()
                : trimmedName.toUpperCase();
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
            tvRole       = itemView.findViewById(R.id.tv_role);
            tvPoints     = itemView.findViewById(R.id.tv_points);
        }
    }
}
