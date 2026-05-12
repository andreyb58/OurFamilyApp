package com.example.killer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.killer.R;
import com.example.killer.adapters.FamilyChatAdapter;
import com.example.killer.api.ApiClient;
import com.example.killer.api.ApiService;
import com.example.killer.api.ApiUtils;
import com.example.killer.auth.AuthManager;
import com.example.killer.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private FamilyChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvNoFamily;

    private AuthManager authManager;
    private ApiService  apiService;

    private int  lastMessageId = 0;
    private boolean isPolling  = false;
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private static final long POLL_MS = 3000;

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            if (isPolling && isAdded()) {
                fetchNew();
                pollingHandler.postDelayed(this, POLL_MS);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        authManager = AuthManager.getInstance(requireContext());
        apiService  = ApiClient.getClient(requireContext()).create(ApiService.class);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        etMessage    = view.findViewById(R.id.et_message);
        btnSend      = view.findViewById(R.id.btn_send);
        tvNoFamily   = view.findViewById(R.id.tv_no_family);

        if (!authManager.isLoggedIn()) {
            showNoFamily("Войдите в систему для доступа к чату");
            return view;
        }

        // Проверяем семью через сервер (не локальный ID)
        checkFamilyAndInit();
        return view;
    }

    private void checkFamilyAndInit() {
        ApiUtils.makeApiCall(apiService.getMyFamily(), new ApiUtils.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded()) return;
                if (data == null || data.isEmpty() || data.get("id") == null) {
                    requireActivity().runOnUiThread(() ->
                            showNoFamily("Чат доступен только для членов семьи.\nСоздайте или вступите в семью."));
                    return;
                }
                requireActivity().runOnUiThread(() -> initChat());
            }
            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        showNoFamily("Чат доступен только для членов семьи.\nСоздайте или вступите в семью."));
            }
        });
    }

    private void showNoFamily(String msg) {
        if (tvNoFamily != null) {
            tvNoFamily.setText(msg);
            tvNoFamily.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (etMessage    != null) etMessage.setEnabled(false);
        if (btnSend      != null) btnSend.setEnabled(false);
    }

    private void initChat() {
        if (tvNoFamily   != null) tvNoFamily.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new FamilyChatAdapter(messages, authManager.getUserId());
        recyclerView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        loadHistory();
    }

    private void loadHistory() {
        ApiUtils.makeApiCall(apiService.getFamilyChat(null),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (!isAdded()) return;
                        List<Map<String, Object>> list =
                                (List<Map<String, Object>>) data.get("list");
                        if (list != null) {
                            messages.clear();
                            for (Map<String, Object> m : list) {
                                messages.add(toMsg(m));
                                int id = extractInt(m.get("id"));
                                if (id > lastMessageId) lastMessageId = id;
                            }
                        }
                        requireActivity().runOnUiThread(() -> {
                            chatAdapter.notifyDataSetChanged();
                            scrollBottom();
                        });
                    }
                    @Override
                    public void onError(String error) { /* тихо */ }
                });
    }

    private void fetchNew() {
        if (lastMessageId <= 0) { loadHistory(); return; }
        ApiUtils.makeApiCall(apiService.getFamilyChat(lastMessageId),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (!isAdded()) return;
                        List<Map<String, Object>> list =
                                (List<Map<String, Object>>) data.get("list");
                        if (list != null && !list.isEmpty()) {
                            int start = messages.size();
                            for (Map<String, Object> m : list) {
                                messages.add(toMsg(m));
                                int id = extractInt(m.get("id"));
                                if (id > lastMessageId) lastMessageId = id;
                            }
                            requireActivity().runOnUiThread(() -> {
                                chatAdapter.notifyItemRangeInserted(start, list.size());
                                scrollBottom();
                            });
                        }
                    }
                    @Override
                    public void onError(String error) { /* тихо */ }
                });
    }

    private void sendMessage() {
        if (etMessage == null) return;
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        btnSend.setEnabled(false);

        Map<String, Object> req = new java.util.HashMap<>();
        req.put("message", text);

        ApiUtils.makeApiCall(apiService.sendFamilyChat(req),
                new ApiUtils.ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            etMessage.setText("");
                            btnSend.setEnabled(true);
                            // Добавляем своё сообщение сразу
                            int msgId = extractInt(data.get("id"));
                            if (msgId > lastMessageId) lastMessageId = msgId;
                            ChatMessage msg = new ChatMessage();
                            msg.setId(msgId);
                            msg.setUserId(authManager.getUserId());
                            msg.setUserName(authManager.getUserName());
                            msg.setMessage(text);
                            msg.setCreatedAt(new java.util.Date().toString());
                            messages.add(msg);
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            scrollBottom();
                        });
                    }
                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            btnSend.setEnabled(true);
                            Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void scrollBottom() {
        if (recyclerView != null && !messages.isEmpty())
            recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private ChatMessage toMsg(Map<String, Object> m) {
        ChatMessage msg = new ChatMessage();
        msg.setId(extractInt(m.get("id")));
        msg.setUserId(extractInt(m.get("userId")));
        msg.setUserName(ApiUtils.extractString(m.get("userName")));
        msg.setMessage(ApiUtils.extractString(m.get("message")));
        msg.setCreatedAt(ApiUtils.extractString(m.get("createdAt")));
        return msg;
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
        if (chatAdapter != null) { // чат уже инициализирован
            isPolling = true;
            pollingHandler.postDelayed(pollRunnable, POLL_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPolling = false;
        pollingHandler.removeCallbacks(pollRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isPolling = false;
        pollingHandler.removeCallbacks(pollRunnable);
    }
}
