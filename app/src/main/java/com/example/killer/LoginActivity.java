package com.example.killer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.killer.auth.AuthManager;
import com.example.killer.models.User;
import com.google.android.material.button.MaterialButton;

/**
 * Активность для входа пользователя
 * Реализует логику аутентификации с валидацией полей
 */
public class LoginActivity extends AppCompatActivity {

    // UI элементы
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private AuthManager authManager; // Менеджер авторизации

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance(this); // Инициализация AuthManager

        // Если пользователь уже авторизован, переходим в главное приложение
        if (authManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Закрываем LoginActivity
            return;
        }

        initializeViews(); // Инициализация UI элементов
        setupClickListeners(); // Настройка обработчиков кликов
    }

    /**
     * Инициализация всех View элементов
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.et_email); // Поле email
        etPassword = findViewById(R.id.et_password); // Поле пароля
        btnLogin = findViewById(R.id.btn_login); // Кнопка входа
        tvRegister = findViewById(R.id.tv_register); // Ссылка на регистрацию
        tvForgotPassword = findViewById(R.id.tv_forgot_password); // Восстановление пароля
        progressBar = findViewById(R.id.progress_bar); // Индикатор загрузки
    }

    /**
     * Настройка обработчиков нажатий
     */
    private void setupClickListeners() {
        // Обработчик кнопки входа
        btnLogin.setOnClickListener(v -> loginUser());

        // Обработчик перехода к регистрации
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Анимация
        });

        // Обработчик восстановления пароля (в разработке)
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Функция восстановления пароля в разработке", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Логика входа пользователя
     * Включает валидацию полей и вызов API
     */
    private void loginUser() {
        // Получаем значения из полей ввода
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Валидация email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            etEmail.requestFocus(); // Фокусируемся на поле
            return;
        }

        // Валидация пароля
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        // Проверка минимальной длины пароля
        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            etPassword.requestFocus();
            return;
        }

        // Показываем индикатор загрузки и блокируем кнопку
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Вызываем метод входа AuthManager
        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Скрываем индикатор и разблокируем кнопку
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                // Показываем приветственное сообщение
                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            "Вход выполнен успешно! Добро пожаловать, " + user.getName() + "!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();
                }

                // Переходим в главное приложение
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish(); // Закрываем LoginActivity
            }

            @Override
            public void onError(String error) {
                // Скрываем индикатор и разблокируем кнопку
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                // Показываем ошибку
                Toast.makeText(LoginActivity.this,
                        "Ошибка входа: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Обработка нажатия кнопки "Назад"
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Анимация
    }
}