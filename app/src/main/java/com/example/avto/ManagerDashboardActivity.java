package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.CarAdapter;
import com.example.avto.adapters.EmployeeAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.example.avto.models.Employee;
import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvManagerInfo, tvCompanyStats, tvSectionTitle;
    private Button btnStaff, btnCars, btnReports, btnExport, btnLogout;
    private RecyclerView recyclerViewData;
    private DatabaseHelper databaseHelper;
    private Employee currentManager;

    // Адаптеры для разных типов данных
    private EmployeeAdapter employeeAdapter;
    private CarAdapter carAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadManagerData();
        setupCompanyStats();
        setupClickListeners();

        // По умолчанию показываем персонал
        showStaffSection();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvManagerInfo = findViewById(R.id.tvManagerInfo);
        tvCompanyStats = findViewById(R.id.tvCompanyStats);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        btnStaff = findViewById(R.id.btnStaff);
        btnCars = findViewById(R.id.btnCars);
        btnReports = findViewById(R.id.btnReports);
        btnExport = findViewById(R.id.btnExport);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewData = findViewById(R.id.recyclerViewData);
    }

    private void loadManagerData() {
        int managerId = getIntent().getIntExtra("EMPLOYEE_ID", -1);
        if (managerId != -1) {
            List<Employee> employees = databaseHelper.getAllEmployees();
            for (Employee emp : employees) {
                if (emp.getId() == managerId && "Директор салона".equals(emp.getPosition())) {
                    currentManager = emp;
                    break;
                }
            }

            if (currentManager != null) {
                tvWelcome.setText("Директор: " + currentManager.getFullName());

                String managerInfo = "Должность: " + currentManager.getPosition() + "\n" +
                        "Телефон: " + currentManager.getPhone() + "\n" +
                        "Email: " + currentManager.getEmail() + "\n" +
                        "Дата приема: " + currentManager.getHireDate() + "\n" +
                        "Зарплата: " + String.format("%,.0f", currentManager.getSalary()) + " ₽";
                tvManagerInfo.setText(managerInfo);
            }
        }
    }

    private void setupCompanyStats() {
        List<Employee> employees = databaseHelper.getAllEmployees();
        int totalEmployees = employees.size();
        int totalSales = 0;
        double totalRevenue = 0;

        for (Employee emp : employees) {
            totalSales += databaseHelper.getEmployeeSalesCount(emp.getId());
            totalRevenue += databaseHelper.getEmployeeTotalSales(emp.getId());
        }

        List<Car> allCars = databaseHelper.getAllCars();
        int totalCars = allCars != null ? allCars.size() : 0;

        String companyStats = "Общая статистика салона:\n\n" +
                "Количество сотрудников: " + totalEmployees + "\n" +
                "Общее количество продаж: " + totalSales + "\n" +
                "Автомобилей в продаже: " + totalCars + "\n" +
                "Общая выручка: " + String.format("%,.0f", totalRevenue) + " ₽\n" +
                "Средняя продажа: " + (totalSales > 0 ?
                String.format("%,.0f", totalRevenue / totalSales) : "0") + " ₽";
        tvCompanyStats.setText(companyStats);
    }

    private void setupClickListeners() {
        btnStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStaffSection();
            }
        });

        btnCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCarsSection();
            }
        });

        btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerDashboardActivity.this, ReportsActivity.class);
                startActivity(intent);
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.widget.Toast.makeText(ManagerDashboardActivity.this, "Экспорт данных выполнен", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerDashboardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showStaffSection() {
        tvSectionTitle.setText("Персонал салона");

        List<Employee> employeeList = databaseHelper.getAllEmployees();
        if (employeeList != null && !employeeList.isEmpty()) {
            employeeAdapter = new EmployeeAdapter(employeeList, databaseHelper);
            recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewData.setAdapter(employeeAdapter);

            // Обновляем статистику по сотрудникам
            updateStaffStats(employeeList);
        } else {
            showEmptyState("Нет данных о сотрудниках");
        }

        // Визуальное выделение активной кнопки
        updateButtonStates(btnStaff);
    }

    private void showCarsSection() {
        tvSectionTitle.setText("Автомобили в продаже");

        List<Car> carList = databaseHelper.getAllCars();
        if (carList != null && !carList.isEmpty()) {
            carAdapter = new CarAdapter(carList);
            recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewData.setAdapter(carAdapter);

            // Обновляем статистику по автомобилям
            updateCarsStats(carList);
        } else {
            showEmptyState("Нет автомобилей в продаже");
        }

        // Визуальное выделение активной кнопки
        updateButtonStates(btnCars);
    }

    private void updateStaffStats(List<Employee> employees) {
        int totalSales = 0;
        double totalRevenue = 0;
        Employee bestEmployee = null;
        double bestSales = 0;

        for (Employee emp : employees) {
            int empSales = databaseHelper.getEmployeeSalesCount(emp.getId());
            double empRevenue = databaseHelper.getEmployeeTotalSales(emp.getId());

            totalSales += empSales;
            totalRevenue += empRevenue;

            if (empRevenue > bestSales) {
                bestSales = empRevenue;
                bestEmployee = emp;
            }
        }

        String stats = "Статистика персонала:\n\n" +
                "Всего сотрудников: " + employees.size() + "\n" +
                "Общее количество продаж: " + totalSales + "\n" +
                "Общая выручка: " + String.format("%,.0f", totalRevenue) + " ₽\n" +
                (bestEmployee != null ?
                        "Лучший продавец: " + bestEmployee.getFullName() + " (" +
                                String.format("%,.0f", bestSales) + " ₽)" : "");

        // Можно добавить отображение этой статистики где-то в интерфейсе
        android.widget.Toast.makeText(this, stats, android.widget.Toast.LENGTH_LONG).show();
    }

    private void updateCarsStats(List<Car> cars) {
        double totalInventoryValue = 0;
        Car mostExpensiveCar = null;
        Car cheapestCar = null;

        for (Car car : cars) {
            totalInventoryValue += car.getPrice();

            if (mostExpensiveCar == null || car.getPrice() > mostExpensiveCar.getPrice()) {
                mostExpensiveCar = car;
            }

            if (cheapestCar == null || car.getPrice() < cheapestCar.getPrice()) {
                cheapestCar = car;
            }
        }

        String stats = "Статистика автомобилей:\n\n" +
                "Всего в продаже: " + cars.size() + " авто\n" +
                "Общая стоимость инвентаря: " + String.format("%,.0f", totalInventoryValue) + " ₽\n" +
                (mostExpensiveCar != null ?
                        "Самый дорогой: " + mostExpensiveCar.getFullName() + " (" +
                                String.format("%,.0f", mostExpensiveCar.getPrice()) + " ₽)\n" : "") +
                (cheapestCar != null ?
                        "Самый доступный: " + cheapestCar.getFullName() + " (" +
                                String.format("%,.0f", cheapestCar.getPrice()) + " ₽)" : "");

        android.widget.Toast.makeText(this, stats, android.widget.Toast.LENGTH_LONG).show();
    }

    private void showEmptyState(String message) {
        recyclerViewData.setAdapter(null);
        // Можно добавить TextView с сообщением
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void updateButtonStates(Button activeButton) {
        // Сбрасываем все кнопки к обычному состоянию
        btnStaff.setBackgroundTintList(getResources().getColorStateList(com.example.avto.R.color.blue));
        btnCars.setBackgroundTintList(getResources().getColorStateList(com.example.avto.R.color.blue));
        btnReports.setBackgroundTintList(getResources().getColorStateList(com.example.avto.R.color.blue));

        // Выделяем активную кнопку
        activeButton.setBackgroundTintList(getResources().getColorStateList(com.example.avto.R.color.green));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран можно обновить данные
        if (btnStaff != null) {
            showStaffSection(); // По умолчанию показываем персонал
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
