package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.CarAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.example.avto.models.Client;
import java.util.ArrayList;
import java.util.List;

public class ClientDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvClientInfo;
    private Button btnSearch, btnFilters, btnProfile, btnLogout;
    private RecyclerView recyclerViewCars;
    private CarAdapter carAdapter;
    private DatabaseHelper databaseHelper;
    private Client currentClient;
    private List<Car> originalCarList;
    private LinearLayout filtersLayout;
    private boolean filtersVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadClientData();
        setupCarsList();
        setupClickListeners();
        setupSearchView();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvClientInfo = findViewById(R.id.tvClientInfo);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilters = findViewById(R.id.btnFilters);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        filtersLayout = findViewById(R.id.filtersLayout);
    }

    private void loadClientData() {
        int clientId = getIntent().getIntExtra("CLIENT_ID", -1);
        if (clientId != -1) {
            currentClient = databaseHelper.getClientById(clientId);

            if (currentClient != null) {
                tvWelcome.setText("Клиент: " + currentClient.getFullName());

                String clientInfo = "Телефон: " + currentClient.getPhone() + "\n" +
                        "Email: " + currentClient.getEmail() + "\n" +
                        "Адрес: " + currentClient.getAddress() + "\n" +
                        "Паспорт: " + currentClient.getPassportInfo() + "\n" +
                        "Дата регистрации: " + currentClient.getRegistrationDate();
                tvClientInfo.setText(clientInfo);
            } else {
                tvWelcome.setText("Клиент: Данные не найдены");
                tvClientInfo.setText("Информация о клиенте недоступна в базе данных");
            }
        } else {
            tvWelcome.setText("Клиент: Ошибка загрузки");
            tvClientInfo.setText("Не удалось загрузить данные клиента");
        }
    }

    private void setupCarsList() {
        try {
            originalCarList = databaseHelper.getAllCars();
            if (originalCarList != null && !originalCarList.isEmpty()) {
                carAdapter = new CarAdapter(originalCarList);
                recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewCars.setAdapter(carAdapter);
            } else {
                showNoCarsMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNoCarsMessage();
        }
    }

    private void setupSearchView() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (carAdapter != null) {
                    carAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchView();
            }
        });

        btnFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilters();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, ProfileActivity.class);
                if (currentClient != null) {
                    intent.putExtra("CLIENT_ID", currentClient.getId());
                }
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Настройка обработчиков для кнопок фильтров
        setupFilterButtons();
    }

    private void toggleSearchView() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
        if (searchView.getVisibility() == View.VISIBLE) {
            searchView.setVisibility(View.GONE);
            searchView.setQuery("", false);
            if (carAdapter != null) {
                carAdapter.getFilter().filter("");
            }
        } else {
            searchView.setVisibility(View.VISIBLE);
            searchView.setIconified(false);
            searchView.requestFocus();
        }
    }

    private void toggleFilters() {
        if (filtersVisible) {
            filtersLayout.setVisibility(View.GONE);
            filtersVisible = false;
            // Сбрасываем фильтры при скрытии
            resetFilters();
        } else {
            filtersLayout.setVisibility(View.VISIBLE);
            filtersVisible = true;
        }
    }

    private void setupFilterButtons() {
        // Кнопка сброса фильтров
        Button btnResetFilters = findViewById(R.id.btnResetFilters);
        btnResetFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilters();
                android.widget.Toast.makeText(ClientDashboardActivity.this, "Фильтры сброшены", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Фильтр по цене
        Button btnFilterPriceLow = findViewById(R.id.btnFilterPriceLow);
        Button btnFilterPriceMedium = findViewById(R.id.btnFilterPriceMedium);
        Button btnFilterPriceHigh = findViewById(R.id.btnFilterPriceHigh);

        btnFilterPriceLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByPrice(0, 1500000);
            }
        });

        btnFilterPriceMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByPrice(1500000, 3000000);
            }
        });

        btnFilterPriceHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByPrice(3000000, 10000000);
            }
        });

        // Фильтр по статусу
        Button btnFilterStatusAvailable = findViewById(R.id.btnFilterStatusAvailable);
        Button btnFilterStatusWaiting = findViewById(R.id.btnFilterStatusWaiting);

        btnFilterStatusAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByStatus("В продаже");
            }
        });

        btnFilterStatusWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByStatus("Ожидание");
            }
        });

        // Фильтр по году
        Button btnFilterYearNew = findViewById(R.id.btnFilterYearNew);
        Button btnFilterYearRecent = findViewById(R.id.btnFilterYearRecent);

        btnFilterYearNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByYear(2023, 2024);
            }
        });

        btnFilterYearRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByYear(2020, 2022);
            }
        });
    }

    private void filterByPrice(double minPrice, double maxPrice) {
        if (originalCarList == null) return;

        List<Car> filteredList = new ArrayList<>();
        for (Car car : originalCarList) {
            if (car.getPrice() >= minPrice && car.getPrice() <= maxPrice) {
                filteredList.add(car);
            }
        }

        updateCarList(filteredList);
        android.widget.Toast.makeText(this,
                String.format("Найдено %d авто от %.0f до %.0f ₽",
                        filteredList.size(), minPrice, maxPrice),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void filterByStatus(String status) {
        if (originalCarList == null) return;

        List<Car> filteredList = new ArrayList<>();
        for (Car car : originalCarList) {
            if (status.equals(car.getStatus())) {
                filteredList.add(car);
            }
        }

        updateCarList(filteredList);
        android.widget.Toast.makeText(this,
                String.format("Найдено %d авто со статусом '%s'",
                        filteredList.size(), status),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void filterByYear(int minYear, int maxYear) {
        if (originalCarList == null) return;

        List<Car> filteredList = new ArrayList<>();
        for (Car car : originalCarList) {
            if (car.getYear() >= minYear && car.getYear() <= maxYear) {
                filteredList.add(car);
            }
        }

        updateCarList(filteredList);
        android.widget.Toast.makeText(this,
                String.format("Найдено %d авто %d-%d года",
                        filteredList.size(), minYear, maxYear),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    private void resetFilters() {
        if (originalCarList != null && carAdapter != null) {
            carAdapter.updateData(originalCarList);
        }
        // Скрываем панель фильтров
        filtersLayout.setVisibility(View.GONE);
        filtersVisible = false;
    }

    private void updateCarList(List<Car> filteredList) {
        if (carAdapter != null) {
            carAdapter.updateData(filteredList);
        }
    }

    private void showNoCarsMessage() {
        TextView noCarsText = new TextView(this);
        noCarsText.setText("В настоящее время нет доступных автомобилей\nили ошибка загрузки данных");
        noCarsText.setTextSize(16);
        noCarsText.setPadding(20, 20, 20, 20);
        noCarsText.setTextColor(getResources().getColor(android.R.color.black));
        noCarsText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        recyclerViewCars.setVisibility(View.GONE);

        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            mainLayout.addView(noCarsText);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список автомобилей при возвращении
        refreshCarList();
    }

    private void refreshCarList() {
        List<Car> updatedCarList = databaseHelper.getAllCars();
        if (updatedCarList != null) {
            originalCarList = updatedCarList;
            if (carAdapter != null) {
                carAdapter.updateData(updatedCarList);
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