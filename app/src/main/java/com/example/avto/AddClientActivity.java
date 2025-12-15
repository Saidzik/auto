package com.example.avto;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;

public class AddClientActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etEmail, etAddress;
    private Button btnSave, btnCancel;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveClient());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveClient() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Проверка обязательных полей
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Введите имя и фамилию", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка телефона или email (хотя бы одно должно быть заполнено)
        if (phone.isEmpty() && email.isEmpty()) {
            Toast.makeText(this, "Введите телефон или email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Добавляем клиента в базу
        try {
            boolean success = databaseHelper.addClient(firstName, lastName, phone, email, address);

            if (success) {
                Toast.makeText(this, "Клиент " + firstName + " " + lastName + " успешно добавлен",
                        Toast.LENGTH_SHORT).show();
                finish(); // Закрываем активность после успешного сохранения
            } else {
                Toast.makeText(this, "Ошибка при добавлении клиента", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddClientActivity", "Ошибка при сохранении клиента: " + e.getMessage(), e);
            Toast.makeText(this, "Произошла ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
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