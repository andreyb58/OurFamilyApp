package com.example.killer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.killer.R;
import com.example.killer.api.ApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSettingsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_server_settings, null);

        EditText etUrl        = view.findViewById(R.id.et_server_url);
        TextView tvCurrent    = view.findViewById(R.id.tv_current_url);
        TextView tvStatus     = view.findViewById(R.id.tv_check_status);
        Button btnCheck       = view.findViewById(R.id.btn_check);
        Button btnSave        = view.findViewById(R.id.btn_save_server);
        Button btnCancel      = view.findViewById(R.id.btn_cancel_server);

        // Показываем текущий адрес
        String current = ApiClient.getBaseUrl(requireContext());
        tvCurrent.setText("Сейчас: " + current);
        etUrl.setText(current);

        btnCheck.setOnClickListener(v -> {
            String inputUrl = etUrl.getText().toString().trim();
            if (inputUrl.isEmpty()) {
                etUrl.setError("Введите адрес");
                return;
            }
            String checkUrl = normalizeUrl(inputUrl);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setBackgroundColor(0xFFE3F2FD);
            tvStatus.setTextColor(0xFF1565C0);
            tvStatus.setText("⏳ Проверяем соединение...");
            btnCheck.setEnabled(false);

            new Thread(() -> {
                String result = checkConnection(checkUrl);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnCheck.setEnabled(true);
                    tvStatus.setVisibility(View.VISIBLE);
                    if (result == null) {
                        tvStatus.setBackgroundColor(0xFFE8F5E9);
                        tvStatus.setTextColor(0xFF2E7D32);
                        tvStatus.setText("✅ Сервер доступен! Можно сохранять.");
                    } else {
                        tvStatus.setBackgroundColor(0xFFFFEBEE);
                        tvStatus.setTextColor(0xFFC62828);
                        tvStatus.setText("❌ " + result);
                    }
                });
            }).start();
        });

        btnSave.setOnClickListener(v -> {
            String inputUrl = etUrl.getText().toString().trim();
            if (inputUrl.isEmpty()) {
                etUrl.setError("Введите адрес");
                return;
            }
            String normalized = normalizeUrl(inputUrl);
            ApiClient.updateBaseUrl(normalized, requireContext());
            Toast.makeText(getContext(),
                    "Адрес сохранён: " + normalized, Toast.LENGTH_LONG).show();
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    /** Нормализует URL: добавляет http:// если нет схемы, убеждается в /api/ на конце */
    private String normalizeUrl(String raw) {
        String url = raw.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        // Убираем лишние слеши в конце, потом добавляем /api/
        if (!url.contains("/api")) {
            url = url.replaceAll("/+$", "") + "/api/";
        } else if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    /**
     * Проверяет доступность сервера.
     * @return null если успешно, иначе — текст ошибки
     */
    private String checkConnection(String baseUrl) {
        // Проверяем /api/health или просто пингуем базовый URL
        String[] candidates = {
                baseUrl + "health",
                baseUrl.replace("/api/", "/actuator/health"),
                baseUrl
        };
        for (String urlStr : candidates) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                conn.disconnect();
                if (code < 500) return null; // Любой ответ до 500 — сервер живой
            } catch (IOException e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("CLEARTEXT")) {
                    return "HTTP запрещён Android. Попробуйте https://";
                }
                // Попробуем следующий URL
            }
        }
        return "Сервер недоступен. Проверьте:\n• Правильный ли IP/домен?\n• Открыт ли порт?\n• Запущен ли сервер?";
    }
}
