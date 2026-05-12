package com.example.killer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.killer.api.ApiClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ServerConfigActivity extends AppCompatActivity {

    private EditText etExternalUrl, etLocalUrl;
    private Button btnSave, btnTestExternal, btnTestLocal, btnAutoDetect;
    private MaterialToolbar toolbar;
    private RadioGroup radioConnectionType;
    private TextView tvLocalIpHint, tvDetectionResult;
    private SwitchMaterial switchAutoDetect;
    private Spinner spinnerLocalIps;

    private static final String PREFS_NAME = "server_config";
    private static final String KEY_CONNECTION_TYPE = "connection_type";
    private static final String KEY_EXTERNAL_URL = "external_url";
    private static final String KEY_LOCAL_URL = "local_url";
    private static final String KEY_AUTO_DETECT = "auto_detect";

    // Типы подключения
    private static final int TYPE_EXTERNAL = 0; // Внешний доступ (интернет)
    private static final int TYPE_LOCAL = 1;    // Локальный доступ (Wi-Fi)
    private static final int TYPE_AUTO = 2;     // Автоопределение

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        initViews();
        setupToolbar();
        loadSavedConfig();
        detectLocalIps();
        setupListeners();
    }

    private void initViews() {
        etExternalUrl = findViewById(R.id.et_external_url);
        etLocalUrl = findViewById(R.id.et_local_url);
        btnSave = findViewById(R.id.btn_save);
        btnTestExternal = findViewById(R.id.btn_test_external);
        btnTestLocal = findViewById(R.id.btn_test_local);
        btnAutoDetect = findViewById(R.id.btn_auto_detect);
        toolbar = findViewById(R.id.toolbar);
        radioConnectionType = findViewById(R.id.radio_connection_type);
        tvLocalIpHint = findViewById(R.id.tv_local_ip_hint);
        tvDetectionResult = findViewById(R.id.tv_detection_result);
        switchAutoDetect = findViewById(R.id.switch_auto_detect);
        spinnerLocalIps = findViewById(R.id.spinner_local_ips);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSavedConfig() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Загружаем сохраненные URL
        String externalUrl = prefs.getString(KEY_EXTERNAL_URL, "http://ourfamily.crazedns.ru:8080/api/");
        String localUrl = prefs.getString(KEY_LOCAL_URL, "http://192.168.1.74:8080/api/");
        int connectionType = prefs.getInt(KEY_CONNECTION_TYPE, TYPE_AUTO);
        boolean autoDetect = prefs.getBoolean(KEY_AUTO_DETECT, true);

        etExternalUrl.setText(externalUrl);
        etLocalUrl.setText(localUrl);

        // Устанавливаем тип подключения
        switchAutoDetect.setChecked(autoDetect);
        if (autoDetect) {
            radioConnectionType.check(R.id.radio_auto);
            updateUrlFieldsVisibility(false);
        } else {
            if (connectionType == TYPE_EXTERNAL) {
                radioConnectionType.check(R.id.radio_external);
                updateUrlFieldsVisibility(true, true);
            } else {
                radioConnectionType.check(R.id.radio_local);
                updateUrlFieldsVisibility(true, false);
            }
        }
    }

    private void detectLocalIps() {
        try {
            List<String> localIps = getLocalIpAddresses();
            if (!localIps.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, localIps);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLocalIps.setAdapter(adapter);

                // Попробуем определить подходящий локальный IP
                String suggestedIp = suggestLocalIp(localIps);
                if (suggestedIp != null) {
                    int position = localIps.indexOf(suggestedIp);
                    if (position >= 0) {
                        spinnerLocalIps.setSelection(position);
                    }
                    tvLocalIpHint.setText("💡 Рекомендуемый IP: " + suggestedIp);
                }

                spinnerLocalIps.setVisibility(View.VISIBLE);
            } else {
                tvLocalIpHint.setText("⚠️ Не удалось определить локальные IP");
            }
        } catch (Exception e) {
            tvLocalIpHint.setText("⚠️ Ошибка определения IP: " + e.getMessage());
        }
    }

    private List<String> getLocalIpAddresses() {
        List<String> ips = new java.util.ArrayList<>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        String ip = addr.getHostAddress();
                        // Проверяем, что это IPv4
                        if (ip.contains(".")) {
                            ips.add(ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ips;
    }

    private String suggestLocalIp(List<String> ips) {
        // Предпочитаем IP из диапазона 192.168.x.x
        for (String ip : ips) {
            if (ip.startsWith("192.168.")) {
                return ip;
            }
        }
        // Затем 10.x.x.x
        for (String ip : ips) {
            if (ip.startsWith("10.")) {
                return ip;
            }
        }
        // Затем 172.16.x.x - 172.31.x.x
        for (String ip : ips) {
            if (ip.startsWith("172.")) {
                String[] parts = ip.split("\\.");
                if (parts.length > 1) {
                    try {
                        int second = Integer.parseInt(parts[1]);
                        if (second >= 16 && second <= 31) {
                            return ip;
                        }
                    } catch (NumberFormatException e) {
                        // игнорируем
                    }
                }
            }
        }
        return ips.isEmpty() ? null : ips.get(0);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveConfig());

        btnTestExternal.setOnClickListener(v -> testConnection(etExternalUrl.getText().toString().trim(), true));
        btnTestLocal.setOnClickListener(v -> testConnection(etLocalUrl.getText().toString().trim(), false));

        btnAutoDetect.setOnClickListener(v -> autoDetectBestConnection());

        switchAutoDetect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUrlFieldsVisibility(!isChecked);
        });

        radioConnectionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (switchAutoDetect.isChecked()) {
                return;
            }
            if (checkedId == R.id.radio_external) {
                updateUrlFieldsVisibility(true, true);
            } else if (checkedId == R.id.radio_local) {
                updateUrlFieldsVisibility(true, false);
            }
        });

        spinnerLocalIps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedIp = (String) parent.getItemAtPosition(position);
                String currentUrl = etLocalUrl.getText().toString();
                // Обновляем URL с выбранным IP
                String newUrl = updateUrlWithIp(currentUrl, selectedIp);
                etLocalUrl.setText(newUrl);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateUrlFieldsVisibility(boolean enabled) {
        updateUrlFieldsVisibility(enabled, radioConnectionType.getCheckedRadioButtonId() == R.id.radio_external);
    }

    private void updateUrlFieldsVisibility(boolean enabled, boolean showExternal) {
        if (enabled) {
            etExternalUrl.setVisibility(showExternal ? View.VISIBLE : View.GONE);
            etLocalUrl.setVisibility(!showExternal ? View.VISIBLE : View.GONE);
            btnTestExternal.setVisibility(showExternal ? View.VISIBLE : View.GONE);
            btnTestLocal.setVisibility(!showExternal ? View.VISIBLE : View.GONE);
            tvLocalIpHint.setVisibility(!showExternal ? View.VISIBLE : View.GONE);
            spinnerLocalIps.setVisibility(!showExternal ? View.VISIBLE : View.GONE);
        } else {
            etExternalUrl.setVisibility(View.GONE);
            etLocalUrl.setVisibility(View.GONE);
            btnTestExternal.setVisibility(View.GONE);
            btnTestLocal.setVisibility(View.GONE);
            tvLocalIpHint.setVisibility(View.GONE);
            spinnerLocalIps.setVisibility(View.GONE);
        }
    }

    private String updateUrlWithIp(String url, String newIp) {
        if (url == null || url.isEmpty()) return "http://" + newIp + ":8080/api/";

        try {
            // Заменяем IP в URL
            java.net.URL urlObj = new java.net.URL(url);
            String host = urlObj.getHost();
            String protocol = urlObj.getProtocol();
            int port = urlObj.getPort();
            String path = urlObj.getPath();

            if (port == -1) port = 8080;

            return protocol + "://" + newIp + ":" + port + path;
        } catch (Exception e) {
            return "http://" + newIp + ":8080/api/";
        }
    }

    private void autoDetectBestConnection() {
        tvDetectionResult.setText("🔄 Проверка подключений...");
        tvDetectionResult.setVisibility(View.VISIBLE);

        String externalUrl = etExternalUrl.getText().toString().trim();
        String localUrl = etLocalUrl.getText().toString().trim();

        new Thread(() -> {
            boolean externalWorks = checkConnection(externalUrl);
            boolean localWorks = checkConnection(localUrl);

            runOnUiThread(() -> {
                StringBuilder result = new StringBuilder();
                if (externalWorks && localWorks) {
                    result.append("✅ Доступны оба подключения\n");
                    // Выбираем локальное как более быстрое
                    radioConnectionType.check(R.id.radio_local);
                    switchAutoDetect.setChecked(false);
                    updateUrlFieldsVisibility(true, false);
                    result.append("👉 Выбрано локальное подключение");
                } else if (externalWorks) {
                    result.append("✅ Доступно внешнее подключение\n");
                    radioConnectionType.check(R.id.radio_external);
                    switchAutoDetect.setChecked(false);
                    updateUrlFieldsVisibility(true, true);
                    result.append("👉 Выбрано внешнее подключение");
                } else if (localWorks) {
                    result.append("✅ Доступно локальное подключение\n");
                    radioConnectionType.check(R.id.radio_local);
                    switchAutoDetect.setChecked(false);
                    updateUrlFieldsVisibility(true, false);
                    result.append("👉 Выбрано локальное подключение");
                } else {
                    result.append("❌ Нет доступных подключений\n");
                    result.append("💡 Проверьте настройки сервера");
                }

                tvDetectionResult.setText(result.toString());
            });
        }).start();
    }

    private void testConnection(String url, boolean isExternal) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Введите URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = isExternal ? "внешнему" : "локальному";
        Toast.makeText(this, "Тестирование " + type + " подключения...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean success = checkConnection(url);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(ServerConfigActivity.this,
                            "✅ " + type + " сервер доступен!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ServerConfigActivity.this,
                            "❌ " + type + " сервер недоступен", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private boolean checkConnection(String baseUrl) {
        try {
            String testUrl = baseUrl.replace("/api/", "/api/health");
            if (!testUrl.startsWith("http")) {
                testUrl = "http://" + testUrl;
            }

            java.net.URL urlObj = new java.net.URL(testUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveConfig() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        boolean autoDetect = switchAutoDetect.isChecked();
        editor.putBoolean(KEY_AUTO_DETECT, autoDetect);

        String externalUrl = etExternalUrl.getText().toString().trim();
        String localUrl = etLocalUrl.getText().toString().trim();

        if (!externalUrl.isEmpty()) {
            if (!externalUrl.endsWith("/")) {
                externalUrl += "/";
            }
            editor.putString(KEY_EXTERNAL_URL, externalUrl);
        }

        if (!localUrl.isEmpty()) {
            if (!localUrl.endsWith("/")) {
                localUrl += "/";
            }
            editor.putString(KEY_LOCAL_URL, localUrl);
        }

        int connectionType;
        if (autoDetect) {
            connectionType = TYPE_AUTO;
        } else if (radioConnectionType.getCheckedRadioButtonId() == R.id.radio_external) {
            connectionType = TYPE_EXTERNAL;
            ApiClient.updateBaseUrl(externalUrl, this);
        } else {
            connectionType = TYPE_LOCAL;
            ApiClient.updateBaseUrl(localUrl, this);
        }

        editor.putInt(KEY_CONNECTION_TYPE, connectionType);
        editor.apply();

        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();

        // Переходим к AuthActivity
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}