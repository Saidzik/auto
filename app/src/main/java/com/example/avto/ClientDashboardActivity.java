package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.ClientCarAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.example.avto.models.Client;
import java.util.ArrayList;
import java.util.List;

public class ClientDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvClientInfo;
    private RecyclerView recyclerViewCars;
    private SearchView searchView;
    private LinearLayout filtersLayout;
    private Button btnLogout;
    private Button btnFilterPriceLow, btnFilterPriceMedium, btnFilterPriceHigh;
    private Button btnResetFilters;

    private ClientCarAdapter carAdapter;
    private DatabaseHelper databaseHelper;
    private Client currentClient;
    private List<Car> originalCarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        databaseHelper = new DatabaseHelper(this);

        // Получаем данные из Intent
        Intent intent = getIntent();
        int clientId = intent.getIntExtra("CLIENT_ID", -1);
        String clientName = intent.getStringExtra("USER_NAME");

        initViews();
        loadClientData(clientId, clientName);
        loadCarsFromDatabase();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvClientInfo = findViewById(R.id.tvClientInfo);
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        searchView = findViewById(R.id.searchView);
        filtersLayout = findViewById(R.id.filtersLayout);

        // Кнопки
        btnLogout = findViewById(R.id.btnLogout);

        // Кнопки фильтров цены
        btnFilterPriceLow = findViewById(R.id.btnFilterPriceLow);
        btnFilterPriceMedium = findViewById(R.id.btnFilterPriceMedium);
        btnFilterPriceHigh = findViewById(R.id.btnFilterPriceHigh);
        btnResetFilters = findViewById(R.id.btnResetFilters);

        originalCarList = new ArrayList<>();

        setupRecyclerView();
        setupClickListeners();
    }

    private void loadClientData(int clientId, String clientName) {
        if (clientId != -1) {
            currentClient = databaseHelper.getClientById(clientId);

            if (currentClient != null) {
                String displayName = currentClient.getFullName();
                if (clientName != null && !clientName.isEmpty()) {
                    displayName = clientName;
                }

                tvWelcome.setText("Добро пожаловать, " + displayName + "!");

                String info = "";
                if (currentClient.getEmail() != null && !currentClient.getEmail().isEmpty()) {
                    info += "📧 " + currentClient.getEmail() + "\n";
                }
                if (currentClient.getPhone() != null && !currentClient.getPhone().isEmpty()) {
                    info += "📞 " + currentClient.getPhone();
                }

                if (!info.isEmpty()) {
                    tvClientInfo.setText(info);
                } else {
                    tvClientInfo.setVisibility(View.GONE);
                }
            } else {
                if (clientName != null) {
                    tvWelcome.setText("Добро пожаловать, " + clientName + "!");
                } else {
                    tvWelcome.setText("Добро пожаловать!");
                }
                tvClientInfo.setText("👤 Клиент");
            }
        } else if (clientName != null) {
            tvWelcome.setText("Добро пожаловать, " + clientName + "!");
            tvClientInfo.setText("👤 Клиент");
        } else {
            tvWelcome.setText("Добро пожаловать!");
            tvClientInfo.setText("👤 Гость");
        }
    }

    private void setupRecyclerView() {
        // Создаем простой адаптер для клиентов
        ClientCarAdapter.OnItemClickListener listener = new ClientCarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Car car) {
                showCarInfoPopup(car);
            }
        };

        carAdapter = new ClientCarAdapter(this, originalCarList, listener);
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCars.setAdapter(carAdapter);
    }

    private void showCarInfoPopup(Car car) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(car.getBrand() + " " + car.getModel());

        StringBuilder details = new StringBuilder();
        details.append("📅 Год выпуска: ").append(car.getYear()).append("\n");
        details.append("🎨 Цвет: ").append(car.getColor() != null ? car.getColor() : "не указан").append("\n");
        details.append("📏 Пробег: ").append(car.getMileage()).append(" км\n");
        details.append("💰 Цена: ").append(String.format("%,.0f", car.getPrice())).append(" ₽\n");
        details.append("📊 Статус: ").append(car.getStatus()).append("\n");

        if (car.getVin() != null && !car.getVin().isEmpty()) {
            details.append("🔢 VIN: ").append(car.getVin()).append("\n");
        }

        if (car.getEquipment() != null && !car.getEquipment().isEmpty()) {
            details.append("⚙️ Комплектация: ").append(car.getEquipment());
        }

        builder.setMessage(details.toString());
        builder.setPositiveButton("Закрыть", null);
        builder.show();
    }

    private void setupClickListeners() {
        // Поиск
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (carAdapter != null) {
                    carAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        // Фильтры по цене
        btnFilterPriceLow.setOnClickListener(v -> filterByPrice(0, 1500000));
        btnFilterPriceMedium.setOnClickListener(v -> filterByPrice(1500000, 3000000));
        btnFilterPriceHigh.setOnClickListener(v -> filterByPrice(3000000, Double.MAX_VALUE));

        // Сброс фильтров
        btnResetFilters.setOnClickListener(v -> {
            carAdapter.updateData(originalCarList);
            searchView.setQuery("", false);
            Toast.makeText(this, "Фильтры сброшены", Toast.LENGTH_SHORT).show();
        });

        // Выход
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadCarsFromDatabase() {
        new Thread(() -> {
            try {
                List<Car> cars = databaseHelper.getAllCars();

                // Фильтруем только автомобили в продаже
                List<Car> availableCars = new ArrayList<>();
                for (Car car : cars) {
                    if ("В продаже".equals(car.getStatus())) {
                        availableCars.add(car);
                    }
                }

                runOnUiThread(() -> {
                    originalCarList.clear();
                    originalCarList.addAll(availableCars);
                    carAdapter.updateData(availableCars);

                    if (availableCars.isEmpty()) {
                        Toast.makeText(ClientDashboardActivity.this,
                                "Нет доступных автомобилей",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ClientDashboardActivity.this,
                                "Доступно " + availableCars.size() + " автомобилей",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки автомобилей", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void filterByPrice(double minPrice, double maxPrice) {
        List<Car> filtered = new ArrayList<>();
        for (Car car : originalCarList) {
            if (car.getPrice() >= minPrice && car.getPrice() <= maxPrice) {
                filtered.add(car);
            }
        }
        carAdapter.updateData(filtered);
        Toast.makeText(this, "Найдено: " + filtered.size() + " авто", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCarsFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}