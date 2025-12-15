package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import com.example.avto.adapters.ClientCarAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.CarAdapter;
import com.example.avto.adapters.ClientAdapter;
import com.example.avto.adapters.DealAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.example.avto.models.Client;
import com.example.avto.models.Deal;
import com.example.avto.models.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeDashboard";

    private TextView tvWelcome, tvEmployeeInfo, tvSectionTitle, tvEmptyData;
    private Button btnCars, btnClients, btnDeals, btnAddCar, btnAddClient,
            btnAddDeal, btnReports, btnProfile, btnLogout;
    private RecyclerView recyclerViewData;
    private CarAdapter carAdapter;
    private DatabaseHelper databaseHelper;
    private Handler mainHandler;
    private Employee currentEmployee;

    private List<Car> carList = new ArrayList<>();
    private List<Client> clientList = new ArrayList<>();
    private List<Deal> dealList = new ArrayList<>();

    // Текущий отображаемый раздел
    private String currentSection = "cars";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        Log.d(TAG, "=== EMPLOYEE DASHBOARD - MANAGEMENT ===");

        databaseHelper = new DatabaseHelper(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadEmployeeData();
        setupClickListeners();

        // Загрузка данных в фоновом потоке
        mainHandler.postDelayed(() -> {
            loadAllData();
            showCarsSection(); // По умолчанию показываем автомобили
        }, 300);
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmployeeInfo = findViewById(R.id.tvEmployeeInfo);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmptyData = findViewById(R.id.tvEmptyData);

        // Кнопки разделов
        btnCars = findViewById(R.id.btnCars);
        btnClients = findViewById(R.id.btnClients);
        btnDeals = findViewById(R.id.btnDeals);

        // Кнопки действий
        btnAddCar = findViewById(R.id.btnAddCar);
        btnAddClient = findViewById(R.id.btnAddClient);
        btnAddDeal = findViewById(R.id.btnAddDeal);
        btnReports = findViewById(R.id.btnReports);

        btnLogout = findViewById(R.id.btnLogout);

        // RecyclerView
        recyclerViewData = findViewById(R.id.recyclerViewData);
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadEmployeeData() {
        int employeeId = getIntent().getIntExtra("EMPLOYEE_ID", -1);
        if (employeeId != -1) {
            // Получаем сотрудника из базы
            new Thread(() -> {
                try {
                    List<Employee> employees = databaseHelper.getAllEmployees();
                    for (Employee emp : employees) {
                        if (emp.getId() == employeeId) {
                            currentEmployee = emp;
                            break;
                        }
                    }

                    mainHandler.post(() -> {
                        if (currentEmployee != null) {
                            updateEmployeeUI();
                        } else {
                            tvWelcome.setText("Сотрудник не найден");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading employee: " + e.getMessage());
                }
            }).start();
        } else {
            tvWelcome.setText("Ошибка загрузки данных сотрудника");
        }
    }

    private void updateEmployeeUI() {
        if (currentEmployee == null) return;

        tvWelcome.setText("Менеджер: " + currentEmployee.getFullName());

        String info = "Должность: " + currentEmployee.getPosition() + "\n" +
                "Отдел: " + currentEmployee.getDepartment() + "\n" +
                "Телефон: " + currentEmployee.getPhone() + "\n" +
                "Email: " + currentEmployee.getEmail() + "\n" +
                "Дата найма: " + currentEmployee.getHireDate();
        tvEmployeeInfo.setText(info);

    }


    private void loadAllData() {
        new Thread(() -> {
            try {
                carList = databaseHelper.getAllCars();
                clientList = databaseHelper.getAllClients();
                dealList = databaseHelper.getAllDeals();

                // Отладка
                Log.d(TAG, "Data loaded - Cars: " + carList.size() +
                        ", Clients: " + clientList.size() +
                        ", Deals: " + dealList.size());

                runOnUiThread(() -> {
                    // Обновляем UI в зависимости от текущего раздела
                    switch (currentSection) {
                        case "cars":
                            showCarsSection();
                            break;
                        case "clients":
                            showClientsSection();
                            break;
                        case "deals":
                            showDealsSection();
                            break;
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Ошибка при загрузке данных", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки данных: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    private void loadCars() {
        new Thread(() -> {
            List<Car> cars = databaseHelper.getAllCars();

            runOnUiThread(() -> {
                if (cars != null && !cars.isEmpty()) {
                    setupCarRecyclerView(cars); // Параметр передается
                    tvEmptyData.setVisibility(View.GONE);
                    recyclerViewData.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyData.setVisibility(View.VISIBLE);
                    recyclerViewData.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void setupCarRecyclerView(List<Car> cars) {
        CarAdapter.OnItemClickListener listener = new CarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Car car) {
                Intent intent = new Intent(EmployeeDashboardActivity.this, CarDetailActivity.class);
                intent.putExtra("CAR_ID", car.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Car car) {
                // Реализация для редактирования (для сотрудников)
                // Например, можно открыть активность редактирования с ограничениями
                Intent intent = new Intent(EmployeeDashboardActivity.this, AddCarActivity.class);
                intent.putExtra("CAR_ID", car.getId());
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("EMPLOYEE_MODE", true); // Добавьте этот флаг для ограничений
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Car car) {
                // Реализация для удаления (для сотрудников)
                // Можно показать диалог или ограничить удаление
                new android.app.AlertDialog.Builder(EmployeeDashboardActivity.this)
                        .setTitle("Удаление автомобиля")
                        .setMessage("Вы уверены, что хотите удалить " + car.getBrand() + " " + car.getModel() + "?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            new Thread(() -> {
                                boolean success = databaseHelper.deleteCar(car.getId());
                                runOnUiThread(() -> {
                                    if (success) {
                                        loadCarsFromDatabase(); // Перезагрузить список
                                        Toast.makeText(EmployeeDashboardActivity.this,
                                                "Автомобиль удален", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EmployeeDashboardActivity.this,
                                                "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }).start();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        };

        carAdapter = new CarAdapter(this, cars, listener);
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewData.setAdapter(carAdapter);
    }
    private void loadCarsFromDatabase() {
        new Thread(() -> {
            List<Car> cars = databaseHelper.getAllCars();
            runOnUiThread(() -> {
                if (cars != null && !cars.isEmpty()) {
                    setupCarRecyclerView(cars); // Используйте setupCarRecyclerView если он есть
                    // Или:
                    // carAdapter.updateData(cars);
                    // recyclerViewData.setAdapter(carAdapter);
                } else {
                    Toast.makeText(EmployeeDashboardActivity.this,
                            "Нет доступных автомобилей", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    private void setupClickListeners() {
        // Проверяем, что кнопки не null перед установкой слушателей
        if (btnCars != null) {
            btnCars.setOnClickListener(v -> showCarsSection());
        }

        if (btnClients != null) {
            btnClients.setOnClickListener(v -> showClientsSection());
        }

        if (btnDeals != null) {
            btnDeals.setOnClickListener(v -> showDealsSection());
        }

        if (btnAddCar != null) {
            btnAddCar.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddCarActivity.class);
                intent.putExtra("EMPLOYEE_ID", currentEmployee != null ? currentEmployee.getId() : -1);
                startActivity(intent);
            });
        }

        if (btnAddClient != null) {
            btnAddClient.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddClientActivity.class);
                intent.putExtra("EMPLOYEE_ID", currentEmployee != null ? currentEmployee.getId() : -1);
                startActivity(intent);
            });
        }

        if (btnAddDeal != null) {
            btnAddDeal.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddDealActivity.class);
                intent.putExtra("EMPLOYEE_ID", currentEmployee != null ? currentEmployee.getId() : -1);
                startActivity(intent);
            });
        }

        if (btnReports != null) {
            btnReports.setOnClickListener(v -> showReports());
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> showProfile());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }
    private void showCarsSection() {
        currentSection = "cars";
        tvSectionTitle.setText("УПРАВЛЕНИЕ АВТОМОБИЛЯМИ");
        updateActiveButton(btnCars);

        Log.d(TAG, "Показываем раздел автомобилей, количество: " + carList.size());

        if (carList.isEmpty()) {
            tvSectionTitle.setText("Нет автомобилей в базе");
            recyclerViewData.setAdapter(null);
            tvEmptyData.setVisibility(View.VISIBLE);
            tvEmptyData.setText("Нет автомобилей для отображения");
        } else {
            tvEmptyData.setVisibility(View.GONE);

            // Проверяем ID сотрудника - если ID = 1, это директор, иначе сотрудник
            boolean isDirector = currentEmployee != null && currentEmployee.getId() == 1;

            if (isDirector) {
                // ДИРЕКТОР: используем CarAdapter с кнопками
                CarAdapter.OnItemClickListener listener = new CarAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Car car) {
                        Intent intent = new Intent(EmployeeDashboardActivity.this, CarDetailActivity.class);
                        intent.putExtra("CAR_ID", car.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onEditClick(Car car) {
                        Intent intent = new Intent(EmployeeDashboardActivity.this, AddCarActivity.class);
                        intent.putExtra("CAR_ID", car.getId());
                        intent.putExtra("EDIT_MODE", true);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Car car) {
                        new android.app.AlertDialog.Builder(EmployeeDashboardActivity.this)
                                .setTitle("Удаление автомобиля")
                                .setMessage("Вы уверены, что хотите удалить " + car.getBrand() + " " + car.getModel() + "?")
                                .setPositiveButton("Удалить", (dialog, which) -> {
                                    deleteCar(car);
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    }
                };

                carAdapter = new CarAdapter(this, carList, listener);
                recyclerViewData.setAdapter(carAdapter);

            } else {
                // СОТРУДНИК: используем ClientCarAdapter (только просмотр)
                // УБЕДИТЕСЬ, ЧТО ПРАВИЛЬНЫЙ ИМПОРТ ДОБАВЛЕН ВВЕРХУ ФАЙЛА
                com.example.avto.adapters.ClientCarAdapter.OnItemClickListener listener = new com.example.avto.adapters.ClientCarAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Car car) {
                        Intent intent = new Intent(EmployeeDashboardActivity.this, CarDetailActivity.class);
                        intent.putExtra("CAR_ID", car.getId());
                        startActivity(intent);
                    }
                };

                ClientCarAdapter clientCarAdapter = new ClientCarAdapter(this, carList, listener);
                recyclerViewData.setAdapter(clientCarAdapter);
            }

            // Устанавливаем LayoutManager (если еще не установлен)
            if (recyclerViewData.getLayoutManager() == null) {
                recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
            }
        }
    }
    private void deleteCar(Car car) {
        new Thread(() -> {
            boolean success = databaseHelper.deleteCar(car.getId());
            runOnUiThread(() -> {
                if (success) {
                    // Обновляем список автомобилей
                    new Thread(() -> {
                        carList = databaseHelper.getAllCars();
                        runOnUiThread(() -> {
                            // Перезагружаем текущий раздел
                            switch (currentSection) {
                                case "cars":
                                    showCarsSection();
                                    break;
                            }
                            Toast.makeText(this, "Автомобиль удален", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                } else {
                    Toast.makeText(this, "Ошибка при удалении автомобиля", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    private void showClientsSection() {
        currentSection = "clients";
        tvSectionTitle.setText("УПРАВЛЕНИЕ КЛИЕНТАМИ");
        updateActiveButton(btnClients);

        if (clientList.isEmpty()) {
            tvSectionTitle.setText("Нет клиентов в базе");
            recyclerViewData.setAdapter(null);
            tvEmptyData.setVisibility(View.VISIBLE);
            tvEmptyData.setText("Нет клиентов для отображения");
        } else {
            tvEmptyData.setVisibility(View.GONE);
            // Проверяем структуру ClientAdapter
            try {
                ClientAdapter adapter = new ClientAdapter(clientList, this::openClientDetails);
                recyclerViewData.setAdapter(adapter);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания ClientAdapter: " + e.getMessage());
                Toast.makeText(this, "Ошибка отображения клиентов", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDealsSection() {
        currentSection = "deals";
        tvSectionTitle.setText("УПРАВЛЕНИЕ СДЕЛКАМИ");
        updateActiveButton(btnDeals);

        if (dealList.isEmpty()) {
            tvSectionTitle.setText("Нет сделок в базе");
            recyclerViewData.setAdapter(null);
            tvEmptyData.setVisibility(View.VISIBLE);
            tvEmptyData.setText("Нет сделок для отображения");
        } else {
            tvEmptyData.setVisibility(View.GONE);

            try {
                DealAdapter adapter = new DealAdapter(dealList, this::openDealDetails);
                recyclerViewData.setAdapter(adapter);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания DealAdapter: " + e.getMessage());
                Toast.makeText(this, "Ошибка отображения сделок", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateActiveButton(Button activeButton) {
        // Сбрасываем цвет всех кнопок
        int normalColor = ContextCompat.getColor(this, android.R.color.holo_blue_light);
        int activeColor = ContextCompat.getColor(this, android.R.color.holo_green_light);

        btnCars.setBackgroundColor(normalColor);
        btnClients.setBackgroundColor(normalColor);
        btnDeals.setBackgroundColor(normalColor);

        // Подсвечиваем активную кнопку
        activeButton.setBackgroundColor(activeColor);
    }

    // Методы для работы с автомобилями
    private void openCarDetails(Car car) {
        Intent intent = new Intent(this, CarDetailActivity.class);
        intent.putExtra("CAR_ID", car.getId());
        startActivity(intent);
    }

    // Методы для работы с клиентами
    private void openClientDetails(Client client) {
        // Временно показываем информацию в диалоге или Toast
        String info = client.getFirstName() + " " + client.getLastName() + "\n" +
                "Телефон: " + client.getPhone() + "\n" +
                "Email: " + client.getEmail() + "\n" +
                "Адрес: " + client.getAddress();
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }

    // Методы для работы со сделками
    private void openDealDetails(Deal deal) {
        String info = "Сделка #" + deal.getId() + "\n" +
                "Клиент: " + deal.getClientName() + "\n" +
                "Автомобиль: " + deal.getCarName() + "\n" +
                "Сумма: " + String.format(Locale.getDefault(), "%,.0f ₽", deal.getAmount()) + "\n" +
                "Дата: " + deal.getDate();
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }

    private void showReports() {
        // Временная реализация
        Toast.makeText(this, "Отчеты в разработке", Toast.LENGTH_SHORT).show();
    }

    private void showProfile() {
        // Временная реализация
        Toast.makeText(this, "Профиль в разработке", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран обновляем данные
        new Thread(() -> {
            loadAllData();
            mainHandler.post(() -> {
                switch (currentSection) {
                    case "cars":
                        showCarsSection();
                        break;
                    case "clients":
                        showClientsSection();
                        break;
                    case "deals":
                        showDealsSection();
                        break;
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}