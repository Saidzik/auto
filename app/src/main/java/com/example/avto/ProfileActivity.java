package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Client;
import com.example.avto.models.Employee;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileTitle, tvProfileInfo;
    private Button btnBack, btnLogout;
    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadProfileData();
        setupClickListeners();
    }

    private void initViews() {
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        tvProfileInfo = findViewById(R.id.tvProfileInfo);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadProfileData() {
        // Проверяем, какой тип пользователя загружать
        int clientId = getIntent().getIntExtra("CLIENT_ID", -1);
        int employeeId = getIntent().getIntExtra("EMPLOYEE_ID", -1);

        if (clientId != -1) {
            // Загрузка данных клиента
            currentUserId = clientId;
            userType = "client";
            loadClientProfile(clientId);
        } else if (employeeId != -1) {
            // Загрузка данных сотрудника
            currentUserId = employeeId;
            userType = "employee";
            loadEmployeeProfile(employeeId);
        } else {
            tvProfileTitle.setText("Профиль пользователя");
            tvProfileInfo.setText("Информация о профиле недоступна\n\nПользователь не найден в системе");
        }
    }

    private void loadClientProfile(int clientId) {
        Client client = databaseHelper.getClientById(clientId);
        if (client != null) {
            tvProfileTitle.setText("Профиль клиента: " + client.getFullName());

            String profileInfo = "Личная информация:\n\n" +
                    "ФИО: " + client.getFullName() + "\n" +
                    "Телефон: " + client.getPhone() + "\n" +
                    "Email: " + client.getEmail() + "\n" +
                    "Адрес: " + client.getAddress() + "\n\n" +
                    "Паспортные данные:\n" +
                    "Серия и номер: " + client.getPassportInfo() + "\n" +
                    "Дата выдачи: " + client.getIssueDate() + "\n" +
                    "Кем выдан: " + client.getIssuedBy() + "\n\n" +
                    "Дата регистрации: " + client.getRegistrationDate() + "\n\n" +
                    "Статус: Активный клиент";

            tvProfileInfo.setText(profileInfo);
        } else {
            tvProfileTitle.setText("Профиль клиента");
            tvProfileInfo.setText("Данные клиента не найдены в базе данных\n\nОбратитесь к администратору системы");
        }
    }

    private void loadEmployeeProfile(int employeeId) {
        // Получаем список всех сотрудников и находим нужного
        List<Employee> employees = databaseHelper.getAllEmployees();
        Employee employee = null;

        for (Employee emp : employees) {
            if (emp.getId() == employeeId) {
                employee = emp;
                break;
            }
        }

        if (employee != null) {
            // Получаем статистику продаж
            int salesCount = databaseHelper.getEmployeeSalesCount(employeeId);
            double totalSales = databaseHelper.getEmployeeTotalSales(employeeId);

            tvProfileTitle.setText("Профиль сотрудника: " + employee.getFullName());

            String profileInfo = "Служебная информация:\n\n" +
                    "ФИО: " + employee.getFullName() + "\n" +
                    "Должность: " + employee.getPosition() + "\n" +
                    "Отдел: " + employee.getDepartment() + "\n" +
                    "Телефон: " + employee.getPhone() + "\n" +
                    "Email: " + employee.getEmail() + "\n\n" +
                    "Личная информация:\n" +
                    "Дата рождения: " + employee.getBirthDate() + "\n" +
                    "Дата приема на работу: " + employee.getHireDate() + "\n" +
                    "Зарплата: " + String.format("%,.0f", employee.getSalary()) + " ₽\n\n" +
                    "Статистика продаж:\n" +
                    "Количество продаж: " + salesCount + "\n" +
                    "Общая сумма продаж: " + String.format("%,.0f", totalSales) + " ₽\n" +
                    "Средняя сделка: " + (salesCount > 0 ?
                    String.format("%,.0f", totalSales / salesCount) : "0") + " ₽\n\n" +
                    "Статус: Активный сотрудник";

            tvProfileInfo.setText(profileInfo);
        } else {
            tvProfileTitle.setText("Профиль сотрудника");
            tvProfileInfo.setText("Данные сотрудника не найдены в базе данных\n\nОбратитесь к администратору системы");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Возврат на предыдущий экран
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выход в главное меню
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран профиля можно обновить данные
        if (currentUserId != -1) {
            if ("client".equals(userType)) {
                loadClientProfile(currentUserId);
            } else if ("employee".equals(userType)) {
                loadEmployeeProfile(currentUserId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
