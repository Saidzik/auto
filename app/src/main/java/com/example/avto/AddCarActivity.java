package com.example.avto;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.squareup.picasso.Picasso;

public class AddCarActivity extends AppCompatActivity {

    private EditText etBrand, etModel, etYear, etVin, etPrice, etMileage, etColor;
    private Spinner spinnerStatus;
    private Button btnSaveCar, btnBack, btnSelectImage;
    private ImageView ivCarImage;
    private DatabaseHelper databaseHelper;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddCarActivity";
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();

        Log.d(TAG, "AddCarActivity started");
    }

    private void initViews() {
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);
        etVin = findViewById(R.id.etVin);
        etPrice = findViewById(R.id.etPrice);
        etMileage = findViewById(R.id.etMileage);
        etColor = findViewById(R.id.etColor);
        btnSaveCar = findViewById(R.id.btnSaveCar);
        btnBack = findViewById(R.id.btnBack);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivCarImage = findViewById(R.id.ivCarImage);

        // Настройка Spinner для статуса
        spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] statuses = {"В продаже", "Продан", "Забронирован", "На обслуживании"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(0);
    }

    private void setupClickListeners() {
        btnSaveCar.setOnClickListener(v -> saveCar());
        btnBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> selectImageFromGallery());
    }

    // САМЫЙ ПРОСТОЙ МЕТОД - ДОЛЖЕН РАБОТАТЬ
    private void selectImageFromGallery() {
        try {
            // Используем ACTION_PICK который уже работал
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть галерею", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error opening gallery: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                selectedImageUri = data.getData();

                try {
                    // Показываем изображение
                    Picasso.get()
                            .load(selectedImageUri)
                            .placeholder(R.drawable.ic_car_placeholder)
                            .error(R.drawable.ic_car_placeholder)
                            .into(ivCarImage);

                    Toast.makeText(this, "Фото выбрано", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Image URI: " + selectedImageUri);

                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
                    ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                }
            }
        }
    }

    private void saveCar() {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String vin = etVin.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String mileageStr = etMileage.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String color = etColor.getText().toString().trim();

        // Проверка обязательных полей
        if (brand.isEmpty() || model.isEmpty() || yearStr.isEmpty() || vin.isEmpty() ||
                priceStr.isEmpty() || mileageStr.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (color.isEmpty()) {
            color = "Не указан";
        }

        // Получаем URI фото или пустую строку
        String imageUri = "";
        if (selectedImageUri != null) {
            imageUri = selectedImageUri.toString();
        }

        try {
            int year = Integer.parseInt(yearStr);
            double price = Double.parseDouble(priceStr);
            int mileage = Integer.parseInt(mileageStr);

            Log.d(TAG, "=== СОХРАНЕНИЕ АВТОМОБИЛЯ ===");
            Log.d(TAG, "Бренд: " + brand);
            Log.d(TAG, "Модель: " + model);
            Log.d(TAG, "VIN: " + vin);
            Log.d(TAG, "Фото URI: " + imageUri);

            // Сохраняем автомобиль
            boolean success = databaseHelper.addCarWithUri(
                    brand, model, year, vin, color,
                    imageUri, price, mileage, status);

            if (success) {
                Toast.makeText(this, "Автомобиль успешно добавлен!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте числовые поля", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Save error: " + e.getMessage(), e);
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