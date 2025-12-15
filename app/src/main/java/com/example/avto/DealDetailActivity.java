package com.example.avto;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Deal;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DealDetailActivity extends AppCompatActivity {

    private TextView tvCarName, tvDate, tvAmount, tvClient, tvEmployee, tvDealId;
    private ImageView ivCarImage;
    private Button btnBack;
    private DatabaseHelper databaseHelper;
    private Deal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_detail);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadDealData();
        setupClickListeners();
    }

    private void initViews() {
        tvCarName = findViewById(R.id.tvCarName);
        tvDate = findViewById(R.id.tvDate);
        tvAmount = findViewById(R.id.tvAmount);
        tvClient = findViewById(R.id.tvClient);
        tvEmployee = findViewById(R.id.tvEmployee);
        tvDealId = findViewById(R.id.tvDealId);
        ivCarImage = findViewById(R.id.ivCarImage);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadDealData() {
        String dealId = getIntent().getStringExtra("DEAL_ID");

        if (dealId != null) {
            // Здесь нужно получить детальную информацию о сделке из БД
            // Пока используем данные из Intent
            String carName = getIntent().getStringExtra("CAR_NAME");
            String date = getIntent().getStringExtra("DATE");
            double amount = getIntent().getDoubleExtra("AMOUNT", 0);
            String clientName = getIntent().getStringExtra("CLIENT_NAME");
            String imageUrl = getIntent().getStringExtra("IMAGE_URL");

            tvCarName.setText(carName);
            tvDate.setText("Дата сделки: " + date);
            tvAmount.setText(String.format(Locale.getDefault(), "Сумма: %.0f ₽", amount));
            tvClient.setText("Клиент: " + clientName);
            tvDealId.setText("ID сделки: " + dealId);
            tvEmployee.setText("Менеджер: " + getIntent().getStringExtra("EMPLOYEE_NAME"));

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivCarImage);
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}