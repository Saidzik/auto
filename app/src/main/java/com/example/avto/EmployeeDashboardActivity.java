package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.DealAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Deal;
import com.example.avto.models.Employee;
import java.util.List;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvEmployeeInfo, tvSalesStats;
    private Button btnCars, btnDeals, btnAddCar, btnAddDeal, btnLogout;
    private RecyclerView recyclerViewDeals;
    private DealAdapter dealAdapter;
    private DatabaseHelper databaseHelper;
    private Employee currentEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadEmployeeData();
        setupDealsList();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmployeeInfo = findViewById(R.id.tvEmployeeInfo);
        tvSalesStats = findViewById(R.id.tvSalesStats);
        btnCars = findViewById(R.id.btnCars);
        btnDeals = findViewById(R.id.btnDeals);
        btnAddCar = findViewById(R.id.btnAddCar);
        btnAddDeal = findViewById(R.id.btnAddDeal);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewDeals = findViewById(R.id.recyclerViewDeals);
    }

    private void loadEmployeeData() {
        int employeeId = getIntent().getIntExtra("EMPLOYEE_ID", -1);
        if (employeeId != -1) {
            // Получаем данные сотрудника из БД
            List<Employee> employees = databaseHelper.getAllEmployees();
            for (Employee emp : employees) {
                if (emp.getId() == employeeId) {
                    currentEmployee = emp;
                    break;
                }
            }

            if (currentEmployee != null) {
                // Обновляем интерфейс данными сотрудника
                tvWelcome.setText("Сотрудник: " + currentEmployee.getFullName());

                String employeeInfo = "Должность: " + currentEmployee.getPosition() + "\n" +
                        "Отдел: " + currentEmployee.getDepartment() + "\n" +
                        "Телефон: " + currentEmployee.getPhone() + "\n" +
                        "Email: " + currentEmployee.getEmail() + "\n" +
                        "Дата найма: " + currentEmployee.getHireDate();
                tvEmployeeInfo.setText(employeeInfo);

                // Статистика продаж
                int salesCount = databaseHelper.getEmployeeSalesCount(currentEmployee.getId());
                double totalSales = databaseHelper.getEmployeeTotalSales(currentEmployee.getId());
                String salesStats = "Количество продаж: " + salesCount + "\n" +
                        "Общая сумма продаж: " + String.format("%,.0f", totalSales) + " ₽\n" +
                        "Средняя сделка: " + (salesCount > 0 ?
                        String.format("%,.0f", totalSales / salesCount) : "0") + " ₽";
                tvSalesStats.setText(salesStats);
            }
        }
    }

    private void setupDealsList() {
        if (currentEmployee != null) {
            List<Deal> dealList = databaseHelper.getEmployeeSales(currentEmployee.getId());
            dealAdapter = new DealAdapter(dealList);
            recyclerViewDeals.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewDeals.setAdapter(dealAdapter);
        }
    }

    private void setupClickListeners() {
        btnCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, CarsActivity.class);
                startActivity(intent);
            }
        });

        btnDeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, DealsActivity.class);
                startActivity(intent);
            }
        });

        btnAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, AddCarActivity.class);
                startActivity(intent);
            }
        });

        btnAddDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, AddDealActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
