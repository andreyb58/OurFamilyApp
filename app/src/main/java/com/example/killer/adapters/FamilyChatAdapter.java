package com.example.killer.adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.models.ChatMessage;

import java.util.List;

/**
 * Адаптер семейного чата.
 * ИСПРАВЛЕНО: принимает myId напрямую (не через Context/AuthManager),
 * что устраняет крэш при отсутствии авторизации.
 * Используются существующие layout-файлы item_chat_message_my и item_chat_message_other.
 */
public class FamilyChatAdapter extends RecyclerView.Adapter<FamilyChatAdapter.ViewHolder> {

    private static final int TYPE_MY   = 1;
    private static final int TYPE_OTHER = 2;

    private final List<ChatMessage> messages;
    private final int myId;

    public FamilyChatAdapter(List<ChatMessage> messages, int myId) {
        this.messages = messages;
        this.myId = myId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getUserId() == myId ? TYPE_MY : TYPE_OTHER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_MY
                ? R.layout.item_chat_message_my
                : R.layout.item_chat_message_other;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ChatMessage msg = messages.get(position);

        h.tvMessage.setText(msg.getMessage());
        h.tvTime.setText(msg.getFormattedTime());

        // Имя отправителя показываем только для чужих сообщений
        if (h.tvSender != null) {
            h.tvSender.setText(msg.getUserName());
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvSender; // может быть null для "моих" сообщений
        TextView tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvSender  = itemView.findViewById(R.id.tv_sender);  // может быть null
            tvTime    = itemView.findViewById(R.id.tv_time);
        }
    }
}
