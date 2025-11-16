package com.example.avto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddDealActivity extends AppCompatActivity {

    private Button btnSaveDeal, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deal);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnSaveDeal = findViewById(R.id.btnSaveDeal);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnSaveDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddDealActivity.this, "Сделка добавлена", Toast.LENGTH_SHORT).show();
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
