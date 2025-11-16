package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.DealAdapter;
import com.example.avto.models.Deal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DealsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDeals;
    private DealAdapter dealAdapter;
    private Button btnAddDeal, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals);

        initViews();
        setupDealsList();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        recyclerViewDeals = findViewById(R.id.recyclerViewDeals);
        btnAddDeal = findViewById(R.id.btnAddDeal);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupDealsList() {
        List<Deal> dealList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        try {
            dealList.add(new Deal("1", "Ford Focus", sdf.parse("01.05.2024"), 1100000, "Иван Петров"));
            dealList.add(new Deal("2", "BMW 320I", sdf.parse("23.04.2024"), 3500000, "Анна Сидорова"));
            dealList.add(new Deal("3", "Audi A4", sdf.parse("15.04.2024"), 2800000, "Петр Иванов"));
            dealList.add(new Deal("4", "Volkswagen Passat", sdf.parse("12.04.2024"), 1900000, "Мария Козлова"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        dealAdapter = new DealAdapter(dealList);
    }

    private void setupRecyclerView() {
        recyclerViewDeals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDeals.setAdapter(dealAdapter);
    }

    private void setupClickListeners() {
        btnAddDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DealsActivity.this, AddDealActivity.class);
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
}
