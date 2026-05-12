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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = AuthManager.getInstance(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация
        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите имя");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            etConfirmPassword.requestFocus();
            return;
        }

        // Показываем прогресс
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        btnRegister.setText("");

        authManager.register(email, password, name, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Зарегистрироваться");

                    Toast.makeText(RegisterActivity.this,
                            "✅ Регистрация успешна! Добро пожаловать, " + name + "!",
                            Toast.LENGTH_LONG).show();

                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Зарегистрироваться");

                    Toast.makeText(RegisterActivity.this,
                            "❌ Ошибка регистрации: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}