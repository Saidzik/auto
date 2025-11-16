package com.example.avto;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Client;
import com.example.avto.models.Employee;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnRecover, btnBack;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnRecover = findViewById(R.id.btnRecover);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        setLoading(false);
    }

    private void setupClickListeners() {
        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLogin();
            }
        });
    }

    private void recoverPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showToast("Пожалуйста, введите email");
            return;
        }

        if (!isValidEmail(email)) {
            showToast("Введите корректный email адрес");
            return;
        }

        // Проверяем существование пользователя в БД
        Client client = databaseHelper.getClientByPhoneOrEmail("", email);
        Employee employee = databaseHelper.getEmployeeByEmail(email);

        if (client == null && employee == null) {
            showToast("Пользователь с таким email не найден");
            return;
        }

        // Имитируем отправку email
        setLoading(true);
        tvStatus.setText("Отправка письма...");

        new SimulateEmailTask().execute(email, getPasswordForUser(client, employee));
    }

    private String getPasswordForUser(Client client, Employee employee) {
        if (employee != null) {
            return "Employee123!";
        } else if (client != null) {
            return "ClientPass123!";
        }
        return "";
    }

    private String getUserName(Client client, Employee employee) {
        if (employee != null) {
            return employee.getFullName();
        } else if (client != null) {
            return client.getFullName();
        }
        return "Пользователь";
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class SimulateEmailTask extends AsyncTask<String, Void, Boolean> {
        private String email;
        private String password;
        private String userName;

        @Override
        protected Boolean doInBackground(String... params) {
            email = params[0];
            password = params[1];

            // Получаем имя пользователя для персонализации
            Client client = databaseHelper.getClientByPhoneOrEmail("", email);
            Employee employee = databaseHelper.getEmployeeByEmail(email);
            userName = getUserName(client, employee);

            try {
                // Имитируем задержку отправки письма (2 секунды)
                Thread.sleep(2000);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            setLoading(false);

            if (success) {
                tvStatus.setText("Письмо с паролем отправлено на " + email);

                // Показываем диалог с информацией о пароле (для демонстрации)
                showPasswordDialog(userName, password, email);
            } else {
                tvStatus.setText("Ошибка отправки письма");
                showToast("Ошибка при отправке письма. Попробуйте позже.");
            }
        }
    }

    private void showPasswordDialog(String userName, String password, String email) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Пароль отправлен")
                .setMessage("Письмо с вашим паролем было отправлено на адрес:\n" + email +
                        "\n\nИмитация отправки - в реальном приложении пароль был бы отправлен на почту." +
                        "\n\nВаши данные для входа:" +
                        "\nИмя: " + userName +
                        "\nПароль: " + password +
                        "\n\n(В реальном приложении пароль не показывается здесь)")
                .setPositiveButton("OK", (dialog, which) -> {
                    backToLogin();
                })
                .setCancelable(false)
                .show();
    }

    private void backToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRecover.setEnabled(false);
            btnBack.setEnabled(false);
            etEmail.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRecover.setEnabled(true);
            btnBack.setEnabled(true);
            etEmail.setEnabled(true);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}