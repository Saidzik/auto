package com.example.avto;

import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import java.util.ArrayList;
import java.util.List;

public class AddDealActivity extends AppCompatActivity {

    private Spinner spinnerCar, spinnerClient, spinnerPaymentType, spinnerEmployee;
    private EditText etAmount, etDealDetails;
    private Button btnSaveDeal, btnBack;
    private DatabaseHelper databaseHelper;
    private List<Car> carList;
    private List<String> clientList;
    private List<String> employeeList; // Список сотрудников
    private List<String> carDisplayList;
    private ArrayAdapter<String> carAdapter, clientAdapter, employeeAdapter, paymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deal);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadSpinnerData();
        setupClickListeners();
    }

    private void initViews() {
        spinnerCar = findViewById(R.id.spinnerCar);
        spinnerClient = findViewById(R.id.spinnerClient);
        spinnerPaymentType = findViewById(R.id.spinnerPaymentType);
        spinnerEmployee = findViewById(R.id.spinnerEmployee); // Новый Spinner
        etAmount = findViewById(R.id.etAmount);
        etDealDetails = findViewById(R.id.etDealDetails);
        btnSaveDeal = findViewById(R.id.btnSaveDeal);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadSpinnerData() {
        // Загружаем список автомобилей В ПРОДАЖЕ
        carList = databaseHelper.getAllCarsInStock();
        carDisplayList = new ArrayList<>();

        if (carList == null || carList.isEmpty()) {
            carDisplayList.add("Нет автомобилей в продаже");
        } else {
            for (Car car : carList) {
                String displayText = String.format("%s %s (%d г., %,.0f ₽)",
                        car.getBrand(),
                        car.getModel(),
                        car.getYear(),
                        car.getPrice());
                carDisplayList.add(displayText);
            }
        }

        carAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carDisplayList);
        carAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCar.setAdapter(carAdapter);

        // Загружаем список клиентов
        clientList = databaseHelper.getAllClientNames();
        if (clientList == null || clientList.isEmpty()) {
            clientList = new ArrayList<>();
            clientList.add("Нет клиентов");
        }
        clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientList);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClient.setAdapter(clientAdapter);

        // Загружаем список сотрудников
        employeeList = databaseHelper.getAllEmployeeNames();
        if (employeeList == null || employeeList.isEmpty()) {
            employeeList = new ArrayList<>();
            employeeList.add("Нет сотрудников");
        }
        employeeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employeeList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(employeeAdapter);

        // Список типов оплаты
        String[] paymentTypes = {"Наличные", "Банковская карта", "Кредит", "Рассрочка"};
        paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentTypes);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentType.setAdapter(paymentAdapter);
    }

    private void setupClickListeners() {
        btnSaveDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDeal();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveDeal() {
        if (carDisplayList.get(0).equals("Нет автомобилей в продаже")) {
            Toast.makeText(this, "Нет автомобилей в продаже", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clientList.get(0).equals("Нет клиентов")) {
            Toast.makeText(this, "Нет клиентов. Добавьте клиента сначала", Toast.LENGTH_SHORT).show();
            return;
        }

        if (employeeList.get(0).equals("Нет сотрудников")) {
            Toast.makeText(this, "Нет сотрудников. Добавьте сотрудника сначала", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем выбранные значения
        int selectedCarPosition = spinnerCar.getSelectedItemPosition();
        String selectedClient = spinnerClient.getSelectedItem().toString();
        String selectedPayment = spinnerPaymentType.getSelectedItem().toString();
        String selectedEmployee = spinnerEmployee.getSelectedItem().toString(); // Выбранный сотрудник
        String amountStr = etAmount.getText().toString().trim();
        String details = etDealDetails.getText().toString().trim();

        // Проверка суммы
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Введите сумму сделки", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            Car selectedCar = carList.get(selectedCarPosition);
            int clientId = getClientIdFromName(selectedClient);
            int employeeId = getEmployeeIdFromName(selectedEmployee);

            if (clientId == -1) {
                Toast.makeText(this, "Клиент не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            if (employeeId == -1) {
                Toast.makeText(this, "Сотрудник не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем сделку
            boolean success = databaseHelper.addDeal(
                    selectedCar.getId(), // String carId
                    clientId,           // int clientId
                    employeeId,         // int employeeId - ВЫБРАННЫЙ СОТРУДНИК
                    amount,             // double amount
                    details,            // String details
                    selectedPayment     // String paymentType
            );

            if (success) {
                Toast.makeText(this, "Сделка успешно оформлена!", Toast.LENGTH_SHORT).show();

                new android.app.AlertDialog.Builder(this)
                        .setTitle("Успешно!")
                        .setMessage("Сделка оформлена.\n" +
                                "Автомобиль: " + selectedCar.getBrand() + " " + selectedCar.getModel() +
                                "\nСтатус: ПРОДАН" +
                                "\nКлиент: " + selectedClient +
                                "\nСотрудник: " + selectedEmployee +
                                "\nСумма: " + String.format("%,.0f ₽", amount))
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Обновляем список и очищаем форму
                            loadSpinnerData();
                            etAmount.setText("");
                            etDealDetails.setText("");
                        })
                        .show();

            } else {
                Toast.makeText(this, "Ошибка при оформлении сделки", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("AddDealActivity", "Ошибка: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int getClientIdFromName(String clientName) {
        return databaseHelper.getClientIdByName(clientName);
    }

    private int getEmployeeIdFromName(String employeeName) {
        return databaseHelper.getEmployeeIdByName(employeeName);
    }
}