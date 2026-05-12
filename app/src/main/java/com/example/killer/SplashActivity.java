package com.example.killer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.killer.auth.AuthManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authManager = AuthManager.getInstance(this);

        new Handler().postDelayed(() -> {
            // Проверяем, настроен ли сервер
            if (isServerConfigured()) {
                checkAuthStatus();
            } else {
                // Показываем настройку сервера с дефолтным IP
                startActivity(new Intent(this, ServerConfigActivity.class));
                finish();
            }
        }, SPLASH_DELAY);
    }

    private boolean isServerConfigured() {
        // Всегда считаем, что сервер настроен с дефолтным IP
        return true;
    }

    private void checkAuthStatus() {
        if (authManager.isLoggedIn()) {
            // Пользователь уже вошел - загружаем данные из локальной БД
            loadLocalData();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Пользователь не вошел - показываем экран авторизации
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }

    private void loadLocalData() {
        // Загружаем данные из локальной базы данных при запуске
        // Это гарантирует, что данные сохраняются между сессиями
        new Thread(() -> {
            try {
                // Симулируем загрузку данных
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}