package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private CarAdapter carAdapter;
    private List<Car> carList;
    private Button btnAddCar, btnBack;
    private SearchView searchView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupCarList();
        setupRecyclerView();
        setupClickListeners();
        setupSearchView();
    }

    private void initViews() {
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        btnAddCar = findViewById(R.id.btnAddCar);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
    }

    private void setupCarList() {
        carList = databaseHelper.getAllCars();
    }

    private void setupRecyclerView() {
        carAdapter = new CarAdapter(carList);
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCars.setAdapter(carAdapter);
    }

    private void setupSearchView() {
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

    private void setupClickListeners() {
        btnAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarsActivity.this, AddCarActivity.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список при возвращении на экран
        refreshCarList();
    }

    private void refreshCarList() {
        List<Car> updatedCarList = databaseHelper.getAllCars();
        if (carAdapter != null) {
            carAdapter.updateData(updatedCarList);
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
