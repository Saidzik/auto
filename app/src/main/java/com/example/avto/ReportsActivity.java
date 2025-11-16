package com.example.avto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReportsActivity extends AppCompatActivity {

    private Button btnGenerateReport, btnExportExcel, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnExportExcel = findViewById(R.id.btnExportExcel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReportsActivity.this, "Отчет сгенерирован", Toast.LENGTH_SHORT).show();
            }
        });

        btnExportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReportsActivity.this, "Экспорт в Excel выполнен", Toast.LENGTH_SHORT).show();
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

