package com.example.avto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddCarActivity extends AppCompatActivity {

    private Button btnSaveCar, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnSaveCar = findViewById(R.id.btnSaveCar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnSaveCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddCarActivity.this, "Автомобиль добавлен", Toast.LENGTH_SHORT).show();
                finish();
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
