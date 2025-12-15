package com.example.avto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;

public class EditCarActivity extends AppCompatActivity {

    private EditText etBrand, etModel, etYear, etVin, etColor,
            etPrice, etMileage, etStatus, etEquipment;
    private ImageView ivCarImage;
    private Button btnSave, btnCancel;
    private DatabaseHelper databaseHelper;
    private Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        loadCarData();
        setupButtonListeners();
    }

    private void initViews() {
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);
        etVin = findViewById(R.id.etVin);
        etColor = findViewById(R.id.etColor);
        etPrice = findViewById(R.id.etPrice);
        etMileage = findViewById(R.id.etMileage);
        etStatus = findViewById(R.id.etStatus);
        etEquipment = findViewById(R.id.etEquipment);
        ivCarImage = findViewById(R.id.ivCarImage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadCarData() {
        String carId = getIntent().getStringExtra("CAR_ID");

        if (carId != null) {
            currentCar = databaseHelper.getCarById(carId);

            if (currentCar != null) {
                etBrand.setText(currentCar.getBrand());
                etModel.setText(currentCar.getModel());
                etYear.setText(String.valueOf(currentCar.getYear()));
                etVin.setText(currentCar.getVin());
                etColor.setText(currentCar.getColor());
                etPrice.setText(String.valueOf(currentCar.getPrice()));
                etMileage.setText(String.valueOf(currentCar.getMileage()));
                etStatus.setText(currentCar.getStatus());
                etEquipment.setText(currentCar.getEquipment());
            }
        }
    }

    private void setupButtonListeners() {
        btnSave.setOnClickListener(v -> saveCar());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveCar() {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String vin = etVin.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String mileageStr = etMileage.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String equipment = etEquipment.getText().toString().trim();

        // Валидация полей
        if (brand.isEmpty() || model.isEmpty() || yearStr.isEmpty() ||
                vin.isEmpty() || priceStr.isEmpty() || mileageStr.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            double price = Double.parseDouble(priceStr);
            int mileage = Integer.parseInt(mileageStr);

            if (currentCar != null) {
                boolean success = databaseHelper.updateCar(
                        currentCar.getId(),
                        brand, model, year, vin,
                        color, price, mileage, status,
                        currentCar.getImageUrl(),
                        equipment
                );

                if (success) {
                    Toast.makeText(this, "Автомобиль обновлен", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректные числовые значения", Toast.LENGTH_SHORT).show();
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