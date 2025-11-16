package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        initViews();
        setupUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupUserInfo() {
        String userRole = getIntent().getStringExtra("USER_ROLE");
        String userLogin = getIntent().getStringExtra("USER_LOGIN");

        String welcomeText = "Добро пожаловать!\n\n" +
                "Логин: " + userLogin + "\n" +
                "Роль: " + userRole + "\n\n" +
                getRoleSpecificMessage(userRole);

        tvWelcome.setText(welcomeText);
    }

    private String getRoleSpecificMessage(String role) {
        switch (role) {
            case "Руководитель":
                return "Доступны функции:\n- Управление сотрудниками\n- Просмотр отчетов\n- Настройки системы";
            case "Сотрудник":
                return "Доступны функции:\n- Работа с заказами\n- Обслуживание клиентов\n- Ввод данных";
            case "Клиент":
                return "Доступны функции:\n- Просмотр каталога\n- История заказов\n- Личные данные";
            default:
                return "Базовые функции";
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

