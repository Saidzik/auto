package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.CarAdapter;
import com.example.avto.adapters.DealAdapter;
import com.example.avto.adapters.EmployeeAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.example.avto.models.Employee;
import com.example.avto.models.Deal;
import com.example.avto.models.Client;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.Color;

public class ManagerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "ManagerDashboard";
    private DatabaseHelper databaseHelper;
    private Handler mainHandler;

    // Views
    private TextView tvWelcome, tvSectionTitle;
    private Button btnStaff, btnCars, btnReports, btnLogout;
    private Button btnAddEmployee, btnAddCar, btnAddDeal, btnDeals;
    private RecyclerView recyclerViewData;

    // Data lists
    private List<Employee> employeeList = new ArrayList<>();
    private List<Car> carList = new ArrayList<>();
    private List<Deal> dealList = new ArrayList<>();
    private List<Client> clientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        Log.d(TAG, "=== MANAGER DASHBOARD STARTED ===");

        databaseHelper = new DatabaseHelper(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews();
        loadManagerInfo();
        setupClickListeners();
        loadAllDataFromDB();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);

        // Основные кнопки
        btnStaff = findViewById(R.id.btnStaff);
        btnCars = findViewById(R.id.btnCars);
        btnReports = findViewById(R.id.btnReports);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeals = findViewById(R.id.btnDeals);

        // Кнопки добавления
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        btnAddCar = findViewById(R.id.btnAddCar);
        btnAddDeal = findViewById(R.id.btnAddDeal);

        recyclerViewData = findViewById(R.id.recyclerViewData);
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadManagerInfo() {
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            tvWelcome.setText("Добро пожаловать, " + userName);
        }
    }

    private void setupClickListeners() {
        // Основные разделы
        btnStaff.setOnClickListener(v -> showStaffSection());
        btnCars.setOnClickListener(v -> showCarsSection());
        btnReports.setOnClickListener(v -> showReportsSection());
        btnDeals.setOnClickListener(v -> showDealsSection());

        // Добавление данных
        btnAddEmployee.setOnClickListener(v -> addNewEmployee());
        btnAddCar.setOnClickListener(v -> addNewCar());
        btnAddDeal.setOnClickListener(v -> addNewDeal());

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadAllDataFromDB() {
        new Thread(() -> {
            try {
                // Загружаем все данные
                employeeList = databaseHelper.getAllEmployees();
                if (employeeList == null) employeeList = new ArrayList<>();

                carList = databaseHelper.getAllCars();
                if (carList == null) carList = new ArrayList<>();

                dealList = databaseHelper.getAllDeals();
                if (dealList == null) dealList = new ArrayList<>();

                clientList = databaseHelper.getAllClients();
                if (clientList == null) clientList = new ArrayList<>();

                runOnUiThread(() -> {
                    showStaffSection(); // Показываем сотрудников по умолчанию
                    Toast.makeText(ManagerDashboardActivity.this, "✅ Данные загружены", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки данных: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(ManagerDashboardActivity.this, "❌ Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ==================== РАЗДЕЛ СОТРУДНИКОВ ====================
    private void showStaffSection() {
        tvSectionTitle.setText("Список сотрудников");

        if (!employeeList.isEmpty()) {
            EmployeeAdapter adapter = new EmployeeAdapter(employeeList, databaseHelper);

            adapter.setOnEmployeeClickListener(new EmployeeAdapter.OnEmployeeClickListener() {
                @Override
                public void onEditClick(Employee employee) {
                    editEmployee(employee);
                }

                @Override
                public void onDeleteClick(Employee employee) {
                    deleteEmployee(employee);
                }

                @Override
                public void onChangePositionClick(Employee employee) {
                    changeEmployeePosition(employee);
                }
            });
            recyclerViewData.setAdapter(adapter);
        } else {
            showEmptyState("Нет сотрудников", "Добавьте первого сотрудника");
        }
    }

    private void addNewEmployee() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Добавить сотрудника");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_employee, null);
        if (dialogView != null) {
            builder.setView(dialogView);

            final EditText etName = dialogView.findViewById(R.id.etEmployeeName);
            final EditText etPosition = dialogView.findViewById(R.id.etEmployeePosition);
            final EditText etEmail = dialogView.findViewById(R.id.etEmployeeEmail);
            final EditText etPhone = dialogView.findViewById(R.id.etEmployeePhone);
            final EditText etSalary = dialogView.findViewById(R.id.etEmployeeSalary);

            builder.setPositiveButton("Добавить", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String position = etPosition.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String salaryText = etSalary.getText().toString().trim();

                if (!name.isEmpty() && !position.isEmpty() && !salaryText.isEmpty()) {
                    try {
                        double salary = Double.parseDouble(salaryText);

                        new Thread(() -> {
                            boolean success = databaseHelper.addEmployeeDirect(name, position, email, phone, salary);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(ManagerDashboardActivity.this, "Сотрудник добавлен", Toast.LENGTH_SHORT).show();
                                    loadAllDataFromDB();
                                } else {
                                    Toast.makeText(ManagerDashboardActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    } catch (NumberFormatException e) {
                        Toast.makeText(ManagerDashboardActivity.this, "Введите корректную зарплату", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerDashboardActivity.this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                }
            });
        }

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // МЕТОД РЕДАКТИРОВАНИЯ СОТРУДНИКА
    private void editEmployee(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать сотрудника");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_employee, null);
        if (dialogView == null) {
            Toast.makeText(this, "Ошибка загрузки диалога", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText etName = dialogView.findViewById(R.id.etEmployeeName);
        final EditText etPosition = dialogView.findViewById(R.id.etEmployeePosition);
        final EditText etEmail = dialogView.findViewById(R.id.etEmployeeEmail);
        final EditText etPhone = dialogView.findViewById(R.id.etEmployeePhone);
        final EditText etSalary = dialogView.findViewById(R.id.etEmployeeSalary);

        if (etName != null) etName.setText(employee.getFullName());
        if (etPosition != null) etPosition.setText(employee.getPosition());
        if (etEmail != null) etEmail.setText(employee.getEmail());
        if (etPhone != null) etPhone.setText(employee.getPhone());
        if (etSalary != null) etSalary.setText(String.valueOf(employee.getSalary()));

        builder.setView(dialogView);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String name = etName != null ? etName.getText().toString() : employee.getFullName();
            String position = etPosition != null ? etPosition.getText().toString() : employee.getPosition();
            String email = etEmail != null ? etEmail.getText().toString() : employee.getEmail();
            String phone = etPhone != null ? etPhone.getText().toString() : employee.getPhone();

            employee.setFullName(name);
            employee.setPosition(position);
            employee.setEmail(email);
            employee.setPhone(phone);

            if (etSalary != null) {
                try {
                    employee.setSalary(Double.parseDouble(etSalary.getText().toString()));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Ошибка в зарплате", Toast.LENGTH_SHORT).show();
                }
            }

            new Thread(() -> {
                boolean success = databaseHelper.updateEmployee(employee);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                        loadAllDataFromDB();
                    } else {
                        Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // МЕТОД УДАЛЕНИЯ СОТРУДНИКА
    private void deleteEmployee(Employee employee) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление сотрудника")
                .setMessage("Вы уверены, что хотите удалить сотрудника " + employee.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        boolean success = databaseHelper.deleteEmployee(employee.getId());
                        runOnUiThread(() -> {
                            if (success) {
                                employeeList.remove(employee);
                                showStaffSection();
                                Toast.makeText(this, "Сотрудник удален", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // МЕТОД ИЗМЕНЕНИЯ ДОЛЖНОСТИ СОТРУДНИКА
    private void changeEmployeePosition(Employee employee) {
        String[] positions = {
                "Директор",
                "Менеджер по продажам",
                "Администратор",
                "Консультант",
                "Бухгалтер",
                "Маркетолог"
        };

        new AlertDialog.Builder(this)
                .setTitle("Изменение должности")
                .setItems(positions, (dialog, which) -> {
                    String newPosition = positions[which];
                    new Thread(() -> {
                        boolean success = databaseHelper.updateEmployeePosition(employee.getId(), newPosition);
                        runOnUiThread(() -> {
                            if (success) {
                                employee.setPosition(newPosition);
                                showStaffSection();
                                Toast.makeText(this, "Должность изменена", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Ошибка изменения", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ==================== РАЗДЕЛ АВТОМОБИЛЕЙ ====================
    private void showCarsSection() {
        tvSectionTitle.setText("Список автомобилей");

        if (!carList.isEmpty()) {
            CarAdapter adapter = new CarAdapter(this, carList, new CarAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Car car) {
                    showCarDetails(car);
                }

                @Override
                public void onEditClick(Car car) {
                    editCar(car);
                }

                @Override
                public void onDeleteClick(Car car) {
                    deleteCar(car);
                }
            });

            recyclerViewData.setAdapter(adapter);
        } else {
            showEmptyState("Нет автомобилей", "Добавьте первый автомобиль");
        }
    }

    private void addNewCar() {
        Intent intent = new Intent(this, AddCarActivity.class);
        intent.putExtra("MANAGER_MODE", true);
        startActivity(intent);
    }

    // МЕТОД ПРОСМОТРА ДЕТАЛЕЙ АВТОМОБИЛЯ
    private void showCarDetails(Car car) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(car.getBrand() + " " + car.getModel());

        String details = "Год: " + car.getYear() + "\n" +
                "Цвет: " + (car.getColor() != null ? car.getColor() : "не указан") + "\n" +
                "Пробег: " + car.getMileage() + " км\n" +
                "Цена: " + String.format("%,.0f", car.getPrice()) + " ₽\n" +
                "Статус: " + car.getStatus();

        builder.setMessage(details);
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    // МЕТОД РЕДАКТИРОВАНИЯ АВТОМОБИЛЯ
    private void editCar(Car car) {
        Intent intent = new Intent(this, AddCarActivity.class);
        intent.putExtra("CAR_ID", car.getId());
        intent.putExtra("EDIT_MODE", true);
        startActivity(intent);
    }

    // МЕТОД УДАЛЕНИЯ АВТОМОБИЛЯ
    private void deleteCar(Car car) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление автомобиля")
                .setMessage("Удалить " + car.getBrand() + " " + car.getModel() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        boolean success = databaseHelper.deleteCar(car.getId());
                        runOnUiThread(() -> {
                            if (success) {
                                carList.remove(car);
                                showCarsSection();
                                Toast.makeText(this, "Автомобиль удален", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ==================== РАЗДЕЛ СДЕЛОК ====================
    private void showDealsSection() {
        tvSectionTitle.setText("Список сделок");

        if (!dealList.isEmpty()) {
            DealAdapter adapter = new DealAdapter(dealList, new DealAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Deal deal) {
                    showDealDetails(deal);
                }
            });

            recyclerViewData.setAdapter(adapter);
        } else {
            showEmptyState("Нет сделок", "Оформите первую сделку");
        }
    }

    private void addNewDeal() {
        Intent intent = new Intent(this, AddDealActivity.class);
        intent.putExtra("MANAGER_MODE", true);
        startActivity(intent);
    }

    // МЕТОД ПРОСМОТРА ДЕТАЛЕЙ СДЕЛКИ
    private void showDealDetails(Deal deal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сделка #" + deal.getId());

        String details = "Клиент: " + deal.getClientName() + "\n" +
                "Автомобиль: " + deal.getCarName() + "\n" +
                "Сумма: " + String.format("%,.0f", deal.getAmount()) + " ₽\n" +
                "Дата: " + deal.getDate();

        builder.setMessage(details);
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    // ==================== ОТЧЕТЫ ====================
    private void showReportsSection() {
        tvSectionTitle.setText("Отчеты и аналитика");

        String[] reports = {
                "Финансовый отчет",
                "Отчет по продажам",
                "Отчет по сотрудникам",
                "Отчет по автомобилям"
        };

        RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Button button = new Button(parent.getContext());
                button.setPadding(32, 32, 32, 32);
                button.setTextSize(16);
                button.setAllCaps(false);
                button.setBackgroundResource(R.drawable.button_background);
                button.setTextColor(Color.BLACK);
                return new RecyclerView.ViewHolder(button) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                String report = reports[position];
                Button button = (Button) holder.itemView;
                button.setText(report);
                button.setOnClickListener(v -> {
                    switch (position) {
                        case 0:
                            generateFinancialReport();
                            break;
                        case 1:
                            generateSalesReport();
                            break;
                        case 2:
                            generateEmployeeReport();
                            break;
                        case 3:
                            generateCarReport();
                            break;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return reports.length;
            }
        };
        recyclerViewData.setAdapter(adapter);
    }

    private void generateFinancialReport() {
        StringBuilder report = new StringBuilder();
        report.append("💰 ФИНАНСОВЫЙ ОТЧЕТ\n\n");

        double revenue = 0;
        for (Deal deal : dealList) {
            revenue += deal.getAmount();
        }

        double expenses = 0;
        for (Employee employee : employeeList) {
            expenses += employee.getSalary();
        }

        double profit = revenue - expenses;
        double profitability = revenue > 0 ? (profit / revenue * 100) : 0;

        report.append("📈 ДОХОДЫ:\n");
        report.append("• Выручка от продаж: ").append(String.format("%,.0f", revenue)).append(" ₽\n\n");

        report.append("📉 РАСХОДЫ:\n");
        report.append("• Фонд зарплат: ").append(String.format("%,.0f", expenses)).append(" ₽\n\n");

        report.append("💵 ФИНАНСОВЫЙ РЕЗУЛЬТАТ:\n");
        report.append("• Прибыль: ").append(String.format("%,.0f", profit)).append(" ₽\n");
        report.append("• Рентабельность: ").append(String.format("%.1f", profitability)).append("%\n");

        showReportDialog("Финансовый отчет", report.toString());
    }

    private void generateSalesReport() {
        StringBuilder report = new StringBuilder();
        report.append("📈 ОТЧЕТ ПО ПРОДАЖАМ\n\n");

        double totalRevenue = 0;
        for (Deal deal : dealList) {
            totalRevenue += deal.getAmount();
        }

        report.append("📊 ОБЩАЯ СТАТИСТИКА:\n");
        report.append("• Всего сделок: ").append(dealList.size()).append("\n");
        report.append("• Общая выручка: ").append(String.format("%,.0f", totalRevenue)).append(" ₽\n");
        report.append("• Средний чек: ").append(String.format("%,.0f", dealList.size() > 0 ? totalRevenue / dealList.size() : 0)).append(" ₽\n");

        showReportDialog("Отчет по продажам", report.toString());
    }

    private void generateEmployeeReport() {
        StringBuilder report = new StringBuilder();
        report.append("👥 ОТЧЕТ ПО СОТРУДНИКАМ\n\n");

        for (Employee employee : employeeList) {
            int salesCount = databaseHelper.getEmployeeSalesCount(employee.getId());
            double salesAmount = databaseHelper.getEmployeeTotalSales(employee.getId());

            report.append("👤 ").append(employee.getFullName()).append("\n");
            report.append("   Должность: ").append(employee.getPosition()).append("\n");
            report.append("   Продаж: ").append(salesCount).append("\n");
            report.append("   Выручка: ").append(String.format("%,.0f", salesAmount)).append(" ₽\n");
            report.append("   Зарплата: ").append(String.format("%,.0f", employee.getSalary())).append(" ₽\n");

            double efficiency = salesAmount > 0 ? (salesAmount / employee.getSalary()) : 0;
            report.append("   Эффективность: ").append(String.format("%.1f", efficiency)).append(" ₽/₽\n\n");
        }

        showReportDialog("Отчет по сотрудникам", report.toString());
    }

    private void generateCarReport() {
        StringBuilder report = new StringBuilder();
        report.append("🚗 ОТЧЕТ ПО АВТОМОБИЛЯМ\n\n");

        report.append("📊 ОБЩАЯ СТАТИСТИКА:\n");
        report.append("• Всего автомобилей: ").append(carList.size()).append("\n");

        double totalValue = 0;
        for (Car car : carList) {
            totalValue += car.getPrice();
        }
        report.append("• Общая стоимость: ").append(String.format("%,.0f", totalValue)).append(" ₽\n");
        report.append("• Средняя цена: ").append(String.format("%,.0f", carList.size() > 0 ? totalValue / carList.size() : 0)).append(" ₽\n");

        showReportDialog("Отчет по автомобилям", report.toString());
    }

    private void showReportDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    private void showEmptyState(String title, String subtitle) {
        TextView textView = new TextView(this);
        textView.setText(title + "\n\n" + subtitle);
        textView.setTextSize(18);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextColor(Color.GRAY);

        recyclerViewData.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(textView) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

            @Override
            public int getItemCount() {
                return 1;
            }
        });
    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("🚪 Выход из системы")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadAllDataFromDB();
    }
}