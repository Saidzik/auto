package com.example.avto.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.avto.models.Car;
import com.example.avto.models.Deal;
import com.example.avto.models.Employee;
import com.example.avto.models.Client;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "car.db";
    private static final int DATABASE_VERSION = 1;
    private static String DATABASE_PATH = "";
    private SQLiteDatabase mDatabase;
    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        DATABASE_PATH = mContext.getApplicationInfo().dataDir + "/databases/";

        // Копируем БД из assets если нужно
        copyDatabaseFromAssets();
    }

    private void copyDatabaseFromAssets() {
        try {
            InputStream inputStream = mContext.getAssets().open("databases/" + DATABASE_NAME);
            String outFileName = DATABASE_PATH + DATABASE_NAME;

            File file = new File(DATABASE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }

            // Проверяем, существует ли уже БД
            if (!checkDatabaseExists()) {
                OutputStream outputStream = new FileOutputStream(outFileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                Log.d("DatabaseHelper", "Database copied from assets");
            }
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error copying database: " + e.getMessage());
        }
    }

    private boolean checkDatabaseExists() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    public void openDatabase() throws SQLException {
        String path = DATABASE_PATH + DATABASE_NAME;
        mDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Не создаем таблицы, т.к. используем готовую БД
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Не обновляем, т.к. используем готовую БД
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            openDatabase();
        }
        return mDatabase;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            openDatabase();
        }
        return mDatabase;
    }

    // Методы для работы с данными
    public Employee getEmployeeByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Employee employee = null;

        Cursor cursor = db.query("Employees", null, "Email = ?",
                new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            employee = new Employee(
                    cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FirstName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("LastName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Position")),
                    cursor.getString(cursor.getColumnIndexOrThrow("BirthDate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("HireDate")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("Salary")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Department"))
            );
            cursor.close();
        }
        return employee;
    }

    public Client getClientByPhoneOrEmail(String phone, String email) {
        SQLiteDatabase db = getReadableDatabase();
        Client client = null;

        Cursor cursor = db.query("Clients", null, "Phone = ? OR Email = ?",
                new String[]{phone, email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            client = new Client(
                    cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FirstName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("LastName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("PassportSeries")),
                    cursor.getString(cursor.getColumnIndexOrThrow("PassportNumber")),
                    cursor.getString(cursor.getColumnIndexOrThrow("IssueDate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("IssuedBy")),
                    cursor.getString(cursor.getColumnIndexOrThrow("RegistrationDate"))
            );
            cursor.close();
        }
        return client;
    }

    public Client getClientById(int clientId) {
        SQLiteDatabase db = getReadableDatabase();
        Client client = null;

        Cursor cursor = db.query("Clients", null, "ID = ?",
                new String[]{String.valueOf(clientId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            client = new Client(
                    cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("FirstName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("LastName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("PassportSeries")),
                    cursor.getString(cursor.getColumnIndexOrThrow("PassportNumber")),
                    cursor.getString(cursor.getColumnIndexOrThrow("IssueDate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("IssuedBy")),
                    cursor.getString(cursor.getColumnIndexOrThrow("RegistrationDate"))
            );
            cursor.close();
        }
        return client;
    }

    public List<Deal> getEmployeeSales(int employeeId) {
        List<Deal> sales = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT s.*, c.FirstName || ' ' || c.LastName as ClientName, " +
                "car_brand.Name as Brand, car_model.Name as Model " +
                "FROM Sales s " +
                "JOIN Clients c ON s.ClientsID = c.ID " +
                "JOIN Cars car ON s.CarsID = car.ID " +
                "JOIN CarModels car_model ON car.ModelID = car_model.ID " +
                "JOIN CarBrands car_brand ON car_model.BrandID = car_brand.ID " +
                "WHERE s.EmployeesID = ? " +
                "ORDER BY s.DateSale DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Deal deal = new Deal(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("ID"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("Brand")) + " " +
                                cursor.getString(cursor.getColumnIndexOrThrow("Model")),
                        java.sql.Date.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("DateSale"))),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ClientName"))
                );
                sales.add(deal);
            }
            cursor.close();
        }
        return sales;
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT car.*, brand.Name as BrandName, model.Name as ModelName " +
                "FROM Cars car " +
                "JOIN CarModels model ON car.ModelID = model.ID " +
                "JOIN CarBrands brand ON model.BrandID = brand.ID " +
                "WHERE car.Status = 'В продаже'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Car car = new Car(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("ID"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("BrandName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ModelName")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("Year")),
                        cursor.getString(cursor.getColumnIndexOrThrow("VIN")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("SalePrice")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("Mileage")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Photo"))
                );
                cars.add(car);
            }
            cursor.close();
        }
        return cars;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query("Employees", null, null, null, null, null, "LastName");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Employee employee = new Employee(
                        cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("FirstName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("LastName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Position")),
                        cursor.getString(cursor.getColumnIndexOrThrow("BirthDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("HireDate")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Salary")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Department"))
                );
                employees.add(employee);
            }
            cursor.close();
        }
        return employees;
    }

    public int getEmployeeSalesCount(int employeeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Sales WHERE EmployeesID = ?",
                new String[]{String.valueOf(employeeId)});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public double getEmployeeTotalSales(int employeeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(Price) FROM Sales WHERE EmployeesID = ?",
                new String[]{String.valueOf(employeeId)});

        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    // Метод для проверки структуры БД (для отладки)
    public void printDatabaseInfo() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        Log.d("DatabaseHelper", "Tables in database:");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                Log.d("DatabaseHelper", "Table: " + tableName);
            }
            cursor.close();
        }
    }
}