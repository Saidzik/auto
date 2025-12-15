package com.example.avto;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.squareup.picasso.Picasso;

public class CarDetailActivity extends AppCompatActivity {

    private static final String TAG = "CarDetailActivity";

    private DatabaseHelper databaseHelper;
    private Car currentCar;
    private String carId;

    private ImageView carImageView;
    private EditText brandEditText;
    private EditText modelEditText;
    private EditText yearEditText;
    private EditText vinEditText;
    private EditText colorEditText;
    private EditText priceEditText;
    private EditText mileageEditText;
    private EditText equipmentEditText;
    private Spinner statusSpinner;
    private Button updateButton;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        // Инициализация базы данных
        databaseHelper = new DatabaseHelper(this);

        // Получаем ID автомобиля из Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CAR_ID")) {
            carId = intent.getStringExtra("CAR_ID");
            Log.d(TAG, "Получен ID автомобиля: " + carId);
        } else {
            Toast.makeText(this, "Ошибка: автомобиль не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация View
        initViews();

        // Загрузка данных автомобиля
        loadCarData();

        // Настройка обработчиков событий
        setupListeners();
    }

    private void initViews() {
        carImageView = findViewById(R.id.carImageView);
        brandEditText = findViewById(R.id.brandEditText);
        modelEditText = findViewById(R.id.modelEditText);
        yearEditText = findViewById(R.id.yearEditText);
        vinEditText = findViewById(R.id.vinEditText);
        colorEditText = findViewById(R.id.colorEditText);
        priceEditText = findViewById(R.id.priceEditText);
        mileageEditText = findViewById(R.id.mileageEditText);
        equipmentEditText = findViewById(R.id.equipmentEditText);
        statusSpinner = findViewById(R.id.statusSpinner);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Убираем кнопку смены фото из layout или делаем её невидимой
        // Если в layout есть кнопка changePhotoButton, уберите её или скройте
        // changePhotoButton.setVisibility(View.GONE);
    }

    private void loadCarData() {
        if (carId != null) {
            currentCar = databaseHelper.getCarById(carId);

            if (currentCar != null) {
                // Заполняем поля данными автомобиля
                brandEditText.setText(currentCar.getBrand());
                modelEditText.setText(currentCar.getModel());
                yearEditText.setText(String.valueOf(currentCar.getYear()));
                vinEditText.setText(currentCar.getVin());
                colorEditText.setText(currentCar.getColor());
                priceEditText.setText(String.valueOf(currentCar.getPrice()));
                mileageEditText.setText(String.valueOf(currentCar.getMileage()));
                equipmentEditText.setText(currentCar.getEquipment());

                // Загружаем изображение
                loadCarImage(currentCar.getImageUrl());

                // Устанавливаем статус в спиннер
                setStatusInSpinner(currentCar.getStatus());

                // Устанавливаем заголовок
                setTitle("Редактирование: " + currentCar.getBrand() + " " + currentCar.getModel());
            } else {
                Toast.makeText(this, "Автомобиль не найден", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void loadCarImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Для файлов добавляем префикс file://
                if (imageUrl.startsWith("/")) {
                    imageUrl = "file://" + imageUrl;
                }

                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(carImageView);
            } else {
                carImageView.setImageResource(R.drawable.ic_car_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки изображения: " + e.getMessage());
            carImageView.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    private void setStatusInSpinner(String status) {
        String[] statusArray = getResources().getStringArray(R.array.car_statuses);
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equals(status)) {
                statusSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCar();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // Убраны обработчики для смены фото
    }

    private void updateCar() {
        // Валидация полей
        if (!validateFields()) {
            return;
        }

        try {
            // Получаем данные из полей
            String brand = brandEditText.getText().toString().trim();
            String model = modelEditText.getText().toString().trim();
            int year = Integer.parseInt(yearEditText.getText().toString().trim());
            String vin = vinEditText.getText().toString().trim();
            String color = colorEditText.getText().toString().trim();
            double price = Double.parseDouble(priceEditText.getText().toString().trim());
            int mileage = Integer.parseInt(mileageEditText.getText().toString().trim());
            String status = statusSpinner.getSelectedItem().toString();
            String equipment = equipmentEditText.getText().toString().trim();

            // Сохраняем текущий URL изображения
            String imageUrl = currentCar != null ? currentCar.getImageUrl() : "";

            Log.d(TAG, "Начинаем обновление автомобиля ID: " + carId);

            // Пробуем обновить автомобиль
            boolean success = databaseHelper.updateCarSimple(
                    carId, year, vin, color, price, mileage,
                    status, imageUrl, equipment
            );

            if (success) {
                Toast.makeText(this, "Автомобиль успешно обновлен", Toast.LENGTH_SHORT).show();

                // Обновляем текущий объект
                if (currentCar != null) {
                    currentCar.setBrand(brand);
                    currentCar.setModel(model);
                    currentCar.setYear(year);
                    currentCar.setVin(vin);
                    currentCar.setColor(color);
                    currentCar.setPrice(price);
                    currentCar.setMileage(mileage);
                    currentCar.setStatus(status);
                    currentCar.setEquipment(equipment);
                }

                // Возвращаем результат
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED", true);
                resultIntent.putExtra("UPDATED_CAR_ID", carId);
                setResult(RESULT_OK, resultIntent);

                finish();

            } else {
                Toast.makeText(this, "Ошибка при обновлении автомобиля", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Метод updateCarSimple вернул false");
            }

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Ошибка обновления: " + e.getMessage(), e);
        }
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(brandEditText.getText().toString().trim())) {
            brandEditText.setError("Введите марку");
            brandEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(modelEditText.getText().toString().trim())) {
            modelEditText.setError("Введите модель");
            modelEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(yearEditText.getText().toString().trim())) {
            yearEditText.setError("Введите год");
            yearEditText.requestFocus();
            return false;
        }

        try {
            int year = Integer.parseInt(yearEditText.getText().toString().trim());
            if (year < 1900 || year > 2100) {
                yearEditText.setError("Введите корректный год (1900-2100)");
                yearEditText.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            yearEditText.setError("Введите число");
            yearEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(priceEditText.getText().toString().trim())) {
            priceEditText.setError("Введите цену");
            priceEditText.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceEditText.getText().toString().trim());
            if (price <= 0) {
                priceEditText.setError("Цена должна быть больше 0");
                priceEditText.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            priceEditText.setError("Введите число");
            priceEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление автомобиля");
        builder.setMessage("Вы уверены, что хотите удалить автомобиль " +
                currentCar.getBrand() + " " + currentCar.getModel() + "?");

        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCar();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCar() {
        try {
            boolean success = databaseHelper.deleteCar(carId);

            if (success) {
                Toast.makeText(this, "Автомобиль успешно удален", Toast.LENGTH_SHORT).show();

                // Возвращаем результат
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DELETED", true);
                resultIntent.putExtra("DELETED_CAR_ID", carId);
                setResult(RESULT_OK, resultIntent);

                finish();
            } else {
                Toast.makeText(this, "Ошибка при удалении автомобиля", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Ошибка удаления: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}