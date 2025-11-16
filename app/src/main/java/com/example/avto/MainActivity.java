package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Client;
import com.example.avto.models.Employee;

public class MainActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPassword();
            }
        });
    }

    private void attemptLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля");
            return;
        }

        // Проверка директора (специальный пароль)
        if (password.equals("AutoBuy2024!")) {
            Employee director = databaseHelper.getEmployeeByEmail(login);
            if (director != null && director.getPosition().equals("Директор салона")) {
                showToast("Успешный вход! Добро пожаловать, " + director.getFullName());
                openRoleSpecificDashboard("director", director, null);
                return;
            }
        }

        // Проверка сотрудников (общий пароль для всех сотрудников)
        if (password.equals("Employee123!")) {
            Employee employee = databaseHelper.getEmployeeByEmail(login);
            if (employee != null) {
                showToast("Успешный вход! Добро пожаловать, " + employee.getFullName());
                openRoleSpecificDashboard("employee", employee, null);
                return;
            }
        }

        // Проверка клиентов (по телефону или email)
        Client client = databaseHelper.getClientByPhoneOrEmail(login, login);
        if (client != null && password.equals("ClientPass123!")) {
            showToast("Успешный вход! Добро пожаловать, " + client.getFullName());
            openRoleSpecificDashboard("client", null, client);
            return;
        }

        showToast("Неверный логин или пароль");
    }

    private void openRoleSpecificDashboard(String role, Employee employee, Client client) {
        Intent intent;

        switch (role) {
            case "director":
                intent = new Intent(this, ManagerDashboardActivity.class);
                intent.putExtra("EMPLOYEE_ID", employee.getId());
                break;
            case "employee":
                intent = new Intent(this, EmployeeDashboardActivity.class);
                intent.putExtra("EMPLOYEE_ID", employee.getId());
                break;
            case "client":
                intent = new Intent(this, ClientDashboardActivity.class);
                intent.putExtra("CLIENT_ID", client.getId());
                break;
            default:
                showToast("Неизвестная роль пользователя");
                return;
        }

        startActivity(intent);
        finish();
    }

    private void openForgotPassword() {
        try {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Ошибка открытия окна восстановления пароля");
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}