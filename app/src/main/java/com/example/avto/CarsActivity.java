package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.CarAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import java.util.List;

public class CarsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCars;
    private SearchView searchView;
    private CarAdapter carAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        loadCars();
    }

    private void initViews() {
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        searchView = findViewById(R.id.searchView);

        // Настройка поиска
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
                return false;
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));

        // Создаем адаптер с передачей контекста и реализацией ВСЕХ методов интерфейса
        carAdapter = new CarAdapter(this, databaseHelper.getAllCars(), new CarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Car car) {
                // Открываем детали автомобиля
                Intent intent = new Intent(CarsActivity.this, CarDetailActivity.class);
                intent.putExtra("CAR_ID", car.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Car car) {
                // Реализация для редактирования автомобиля
                // Если в CarsActivity редактирование не нужно, можно оставить пустым
                // Или открыть активность редактирования
                Intent intent = new Intent(CarsActivity.this, AddCarActivity.class);
                intent.putExtra("CAR_ID", car.getId());
                intent.putExtra("EDIT_MODE", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Car car) {
                // Реализация для удаления автомобиля
                // Если в CarsActivity удаление не нужно, можно оставить пустым
                // Или показать диалог подтверждения
            /*
            new android.app.AlertDialog.Builder(CarsActivity.this)
                .setTitle("Удаление")
                .setMessage("Удалить автомобиль " + car.getBrand() + " " + car.getModel() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    databaseHelper.deleteCar(car.getId());
                    loadCars();
                })
                .setNegativeButton("Отмена", null)
                .show();
            */
            }
        });

        recyclerViewCars.setAdapter(carAdapter);
    }

    private void loadCars() {
        new Thread(() -> {
            List<Car> cars = databaseHelper.getAllCars();

            runOnUiThread(() -> {
                if (cars != null && !cars.isEmpty()) {
                    carAdapter.updateData(cars);
                } else {
                    // Показать сообщение об отсутствии автомобилей
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
    }
}