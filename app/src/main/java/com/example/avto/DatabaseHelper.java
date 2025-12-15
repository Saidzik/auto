package com.example.avto.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;
import com.example.avto.models.Car;
import com.example.avto.models.Client;
import com.example.avto.models.Deal;
import com.example.avto.models.Employee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cars.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private static String DATABASE_PATH = "";
    private final Context context;
    private SQLiteDatabase database;

    // Константы для таблицы Cars (соответствуют вашей БД)
    public static final String TABLE_CARS = "Cars";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_MODEL_ID = "ModelID";
    public static final String COLUMN_YEAR = "Year";
    public static final String COLUMN_VIN = "VIN";
    public static final String COLUMN_COLOR = "Color";
    public static final String COLUMN_PHOTO = "Photo";
    public static final String COLUMN_SALE_PRICE = "SalePrice";
    public static final String COLUMN_PURCHASE_PRICE = "PurchasePrice";
    public static final String COLUMN_MILEAGE = "Mileage";
    public static final String COLUMN_STATUS = "Status";
    public static final String COLUMN_EQUIPMENT = "Equipment";
    public static final String COLUMN_ENGINE_TYPE_ID = "EngineTypeID";
    public static final String COLUMN_VOLUME = "Volume";
    public static final String COLUMN_POWER = "Power";
    public static final String COLUMN_TRANSMISSION_TYPE_ID = "TransmissionTypeID";
    public static final String COLUMN_DRIVE_TYPE_ID = "DriveTypeID";
    public static final String COLUMN_WEIGHT = "Weight";
    public static final String COLUMN_ARRIVAL_DATE = "ArrivalDate";

    // Константы для других таблиц
    public static final String TABLE_SALES = "Sales";
    public static final String TABLE_EMPLOYEES = "Employees";
    public static final String TABLE_CLIENTS = "Clients";
    public static final String TABLE_CAR_BRANDS = "CarBrands";
    public static final String TABLE_CAR_MODELS = "CarModels";
    public static final String TABLE_ENGINE_TYPES = "EngineTypes";
    public static final String TABLE_TRANSMISSION_TYPES = "TransmissionTypes";
    public static final String TABLE_DRIVE_TYPES = "DriveTypes";
    public static final String TABLE_PAYMENT_TYPES = "PaymentTypes";

    // Константы для упрощенной таблицы автомобилей (если создаете новую)
    public static final String COLUMN_BRAND = "Brand";
    public static final String COLUMN_MODEL = "Model";
    public static final String COLUMN_IMAGE_URL = "ImageUrl";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_PRICE = "Price";

    // Формат даты
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Конструктор
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath();

        // Копируем БД из assets при первом запуске
        if (!checkDatabaseExists()) {
            try {
                copyDatabaseFromAssets();
            } catch (IOException e) {
                Log.e(TAG, "Ошибка копирования БД из assets", e);
            }
        }
    }

    // ============== РАБОТА С БАЗОЙ ДАННЫХ ==============

    private boolean checkDatabaseExists() {
        File dbFile = new File(DATABASE_PATH);
        return dbFile.exists() && dbFile.length() > 0;
    }

    private void copyDatabaseFromAssets() throws IOException {
        // Создаем папку databases если её нет
        File dbDir = new File(DATABASE_PATH).getParentFile();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        // Копируем БД из assets
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getAssets().open("databases/" + DATABASE_NAME);
            outputStream = new FileOutputStream(DATABASE_PATH);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            Log.d(TAG, "База данных скопирована из assets/databases/" + DATABASE_NAME);

        } finally {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
        }
    }
    // В DatabaseHelper добавьте этот метод:
    public void copyDatabaseWithPermissions() {
        try {
            // Закрываем текущее соединение если открыто
            if (database != null && database.isOpen()) {
                database.close();
            }

            // Копируем БД из assets если её нет
            if (!checkDatabaseExists()) {
                copyDatabaseFromAssets();
            }

            // Устанавливаем правильные права
            File dbFile = new File(DATABASE_PATH);
            if (dbFile.exists()) {
                // Даем права на чтение и запись
                boolean setReadable = dbFile.setReadable(true, false);
                boolean setWritable = dbFile.setWritable(true, false);
                boolean setExecutable = dbFile.setExecutable(true, false);

                Log.d(TAG, "Права базы данных: readable=" + setReadable +
                        ", writable=" + setWritable +
                        ", executable=" + setExecutable);

                // Также даем права на папку
                File dbDir = dbFile.getParentFile();
                if (dbDir != null && dbDir.exists()) {
                    dbDir.setReadable(true, false);
                    dbDir.setWritable(true, false);
                    dbDir.setExecutable(true, false);
                }
            }

            // Открываем базу данных заново
            database = SQLiteDatabase.openDatabase(DATABASE_PATH, null,
                    SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка настройки прав базы данных: " + e.getMessage(), e);
            // Если не удалось, создаем новую базу
            database = super.getWritableDatabase();
        }
    }

    // Обновите метод getWritableDatabase:

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (database != null && database.isOpen()) {
            return database;
        }

        // Если БД еще не скопирована, копируем
        if (!checkDatabaseExists()) {
            try {
                copyDatabaseFromAssets();
            } catch (IOException e) {
                Log.e(TAG, "Не удалось скопировать БД, создаем новую", e);
                return super.getReadableDatabase();
            }
        }

        database = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Если БД уже скопирована из assets, не создаем таблицы заново
        if (!checkDatabaseExists()) {
            // Создаем только упрощенную таблицу cars для совместимости
            createSimpleCarsTable(db);
            Log.d(TAG, "Создана новая база данных с упрощенной таблицей Cars");
        } else {
            Log.d(TAG, "Используется существующая база данных из assets");
        }
        createSimpleCarsTable(db);
        Log.d(TAG, "Создана упрощенная таблица CarsSimple");
        String CREATE_SALES_TABLE = "CREATE TABLE Sales (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "CarsID INTEGER," +
                "ClientsID INTEGER," +
                "EmployeesID INTEGER," +
                "Price REAL," +
                "Details TEXT," + // Добавьте эту колонку
                "DateSale TEXT," +
                "PaymentTypesID INTEGER," +
                "FOREIGN KEY (CarsID) REFERENCES Cars(ID)," +
                "FOREIGN KEY (ClientsID) REFERENCES Clients(ID)," +
                "FOREIGN KEY (EmployeesID) REFERENCES Employees(ID)," +
                "FOREIGN KEY (PaymentTypesID) REFERENCES PaymentTypes(ID)" +
                ")";
        db.execSQL(CREATE_SALES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Пока не реализовано
        Log.d(TAG, "Обновление базы данных с версии " + oldVersion + " до " + newVersion);
    }

    // ============== СОЗДАНИЕ УПРОЩЕННОЙ ТАБЛИЦЫ CARS ==============

    private void createSimpleCarsTable(SQLiteDatabase db) {
        String CREATE_SIMPLE_CARS_TABLE = "CREATE TABLE IF NOT EXISTS CarsSimple (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Brand TEXT NOT NULL," +
                "Model TEXT NOT NULL," +
                "Year INTEGER NOT NULL," +
                "VIN TEXT UNIQUE NOT NULL," +
                "Color TEXT," +
                "ImageUrl TEXT," +
                "Price REAL NOT NULL," +
                "Mileage INTEGER NOT NULL," +
                "Status TEXT DEFAULT 'В продаже'," +
                "Equipment TEXT DEFAULT 'Стандарт'," +
                "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_SIMPLE_CARS_TABLE);
    }

    // ============== МЕТОДЫ ДЛЯ РАБОТЫ С АВТОМОБИЛЯМИ ==============

    public Car getCarById(String id) {
        Car car = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Сначала пробуем получить данные из исходной таблицы Cars
            String query = "SELECT " +
                    "c.ID, c.VIN, c.Color, c.SalePrice, c.PurchasePrice, " +
                    "c.Year, c.Mileage, c.Status, c.Photo, " +
                    "c.Equipment, c.ArrivalDate, " +
                    "cm.Name as ModelName, cb.Name as BrandName " +
                    "FROM Cars c " +
                    "LEFT JOIN CarModels cm ON c.ModelID = cm.ID " +
                    "LEFT JOIN CarBrands cb ON cm.BrandID = cb.ID " +
                    "WHERE c.ID = ?";

            cursor = db.rawQuery(query, new String[]{id});

            if (cursor != null && cursor.moveToFirst()) {
                car = createCarFromCursor(cursor);
            } else {
                // Если не нашли в основной таблице, пробуем упрощенную
                if (cursor != null) cursor.close();
                cursor = db.rawQuery("SELECT * FROM " + TABLE_CARS + " WHERE ID = ?", new String[]{id});
                if (cursor != null && cursor.moveToFirst()) {
                    car = createSimpleCarFromCursor(cursor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении автомобиля по ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return car;
    }
    // В классе DatabaseHelper добавьте этот метод:

    // Или полная версия метода:
    public boolean addCarWithUrl(String brand, String model, int year, String vin,
                                 String color, String imageUrl, double price,
                                 int mileage, String status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Проверяем, существует ли уже автомобиль с таким VIN
            Cursor cursor = db.rawQuery("SELECT ID FROM Cars WHERE VIN = ?", new String[]{vin});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                Log.w(TAG, "Автомобиль с таким VIN уже существует: " + vin);
                return false;
            }
            if (cursor != null) cursor.close();

            // Создаем бренд и модель
            int brandId = findOrCreateBrand(brand, db);
            int modelId = findOrCreateModel(model, brandId, db);

            values.put("ModelID", modelId);
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("Photo", imageUrl);
            values.put("SalePrice", price);
            values.put("PurchasePrice", price * 0.8); // Закупочная цена 80% от продажной
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", "Стандарт");
            values.put("ArrivalDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            long result = db.insert(TABLE_CARS, null, values);

            if (result != -1) {
                Log.d(TAG, "Автомобиль успешно добавлен: " + brand + " " + model + ", VIN: " + vin);
                return true;
            } else {
                Log.e(TAG, "Ошибка при добавлении автомобиля в базу");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в методе addCarWithUrl: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.ID, c.VIN, c.Color, c.SalePrice as Price, c.Year, c.Mileage, " +
                "c.Status, c.Photo as ImageUrl, cm.Name as ModelName, " +
                "cb.Name as BrandName FROM Cars c " +
                "LEFT JOIN CarModels cm ON c.ModelID = cm.ID " +
                "LEFT JOIN CarBrands cb ON cm.BrandID = cb.ID " +
                "WHERE c.Status = 'В продаже' " + // Только автомобили в продаже
                "ORDER BY cb.Name, cm.Name";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Car car = new Car();
                car.setId(String.valueOf(cursor.getInt(0)));
                car.setVin(cursor.getString(1));
                car.setColor(cursor.getString(2));
                car.setPrice(cursor.getDouble(3));
                car.setYear(cursor.getInt(4));
                car.setMileage(cursor.getInt(5));
                car.setStatus(cursor.getString(6));
                car.setImageUrl(cursor.getString(7));
                car.setModel(cursor.getString(8));
                car.setBrand(cursor.getString(9));
                cars.add(car);
            }
            cursor.close();
        }
        db.close();
        return cars;
    }
    // Метод для добавления сделки с String carId
    public boolean addDealWithStringCarId(String carIdStr, int clientId, int employeeId,
                                          double amount, String details, String paymentType) {
        try {
            int carId = Integer.parseInt(carIdStr);
            return addDeal(carId, clientId, employeeId, amount, details, paymentType);
        } catch (NumberFormatException e) {
            Log.e("DatabaseHelper", "Ошибка преобразования carId: " + carIdStr);
            return false;
        }
    }
    public int getClientIdByName(String clientName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int clientId = -1;

        // Предполагаем, что clientName в формате "Имя Фамилия"
        String[] nameParts = clientName.split(" ");
        if (nameParts.length >= 2) {
            String firstName = nameParts[0];
            String lastName = nameParts[1];

            String query = "SELECT ID FROM Clients WHERE FirstName = ? AND LastName = ?";
            Cursor cursor = db.rawQuery(query, new String[]{firstName, lastName});

            if (cursor != null && cursor.moveToFirst()) {
                clientId = cursor.getInt(0);
                cursor.close();
            }
        }
        db.close();
        return clientId;
    }
    // В DatabaseHelper.java - метод с 5 параметрами
    public boolean addDeal(String carIdStr, int clientId, int employeeId, double amount,
                           String details, String paymentType) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int carId = Integer.parseInt(carIdStr);

            ContentValues values = new ContentValues();
            values.put("CarsID", carId);
            values.put("ClientsID", clientId);
            values.put("EmployeesID", employeeId);
            values.put("Price", amount);
            // Не добавляем Details, т.к. колонки нет
            // values.put("Details", details);
            values.put("DateSale", getCurrentDateTime());
            values.put("PaymentTypesID", getPaymentTypeId(paymentType));

            long result = db.insert("Sales", null, values);

            if (result != -1) {
                // Обновляем статус автомобиля
                ContentValues carValues = new ContentValues();
                carValues.put("Status", "Продан");
                db.update("Cars", carValues, "ID = ?", new String[]{String.valueOf(carId)});

                Log.d("DatabaseHelper", "Сделка оформлена. Сотрудник ID: " + employeeId);
                return true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Ошибка: " + e.getMessage());
        } finally {
            db.close();
        }

        return false;
    }
    public List<Car> getAllCarsInStock() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.ID, c.VIN, c.Color, c.SalePrice as Price, c.Year, c.Mileage, " +
                "c.Status, c.Photo as ImageUrl, cm.Name as ModelName, " +
                "cb.Name as BrandName FROM Cars c " +
                "LEFT JOIN CarModels cm ON c.ModelID = cm.ID " +
                "LEFT JOIN CarBrands cb ON cm.BrandID = cb.ID " +
                "WHERE c.Status = 'В продаже' OR c.Status = 'Available' " + // Оба варианта статуса
                "ORDER BY cb.Name, cm.Name";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Car car = new Car();
                car.setId(String.valueOf(cursor.getInt(0)));
                car.setVin(cursor.getString(1));
                car.setColor(cursor.getString(2));
                car.setPrice(cursor.getDouble(3));
                car.setYear(cursor.getInt(4));
                car.setMileage(cursor.getInt(5));
                car.setStatus(cursor.getString(6));
                car.setImageUrl(cursor.getString(7));
                car.setModel(cursor.getString(8));
                car.setBrand(cursor.getString(9));
                cars.add(car);
            }
            cursor.close();
        }
        db.close();
        return cars;
    }
    // Получение списка имен сотрудников
    public List<String> getAllEmployeeNames() {
        List<String> employees = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT FirstName || ' ' || LastName as FullName FROM Employees ORDER BY LastName";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                employees.add(cursor.getString(0));
            }
            cursor.close();
        }
        db.close();
        return employees;
    }

    // Получение ID сотрудника по имени
    public int getEmployeeIdByName(String employeeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int employeeId = -1;

        try {
            // Разделяем имя и фамилию (предполагаем формат "Имя Фамилия")
            String[] nameParts = employeeName.split(" ");
            if (nameParts.length >= 2) {
                String firstName = nameParts[0];
                String lastName = nameParts[1];

                String query = "SELECT ID FROM Employees WHERE FirstName = ? AND LastName = ?";
                Cursor cursor = db.rawQuery(query, new String[]{firstName, lastName});

                if (cursor != null && cursor.moveToFirst()) {
                    employeeId = cursor.getInt(0);
                    cursor.close();
                }
            } else {
                // Если формат неправильный, ищем по полному имени
                String query = "SELECT ID FROM Employees WHERE FirstName || ' ' || LastName = ?";
                Cursor cursor = db.rawQuery(query, new String[]{employeeName});

                if (cursor != null && cursor.moveToFirst()) {
                    employeeId = cursor.getInt(0);
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Ошибка поиска сотрудника: " + e.getMessage());
        } finally {
            db.close();
        }

        return employeeId;
    }
    // Получение имени сотрудника по ID
    public String getEmployeeNameById(int employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String employeeName = "Неизвестный сотрудник";

        String query = "SELECT FirstName || ' ' || LastName as FullName FROM Employees WHERE ID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId)});

        if (cursor != null && cursor.moveToFirst()) {
            employeeName = cursor.getString(0);
            cursor.close();
        }
        db.close();

        return employeeName;
    }

    // Вспомогательные методы (добавьте если нет)
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private List<Car> getCarsFromMainTable(SQLiteDatabase db) {
        List<Car> cars = new ArrayList<>();
        Cursor cursor = null;

        try {
            // Стандартный запрос с JOIN
            String query = "SELECT " +
                    "c.ID, c.VIN, c.Color, c.SalePrice, c.PurchasePrice, " +
                    "c.Year, c.Mileage, c.Status, c.Photo, " +
                    "c.Equipment, c.ArrivalDate, " +
                    "cm.Name as ModelName, cb.Name as BrandName " +
                    "FROM Cars c " +
                    "LEFT JOIN CarModels cm ON c.ModelID = cm.ID " +
                    "LEFT JOIN CarBrands cb ON cm.BrandID = cb.ID " +
                    "ORDER BY c.ID DESC";

            Log.d(TAG, "Запрос автомобилей из основной таблицы: " + query);

            cursor = db.rawQuery(query, null);

            int count = cursor != null ? cursor.getCount() : 0;
            Log.d(TAG, "Найдено записей в основной таблице: " + count);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Car car = createCarFromCursor(cursor);
                    if (car != null) {
                        cars.add(car);
                    }
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения автомобилей из основной таблицы", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return cars;
    }
    private List<Car> getCarsFromSimpleTable(SQLiteDatabase db) {
        List<Car> cars = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT ID, Brand, Model, Year, VIN, Color, " +
                    "Price, Mileage, Status, ImageUrl FROM CarsSimple " +
                    "ORDER BY ID DESC";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = String.valueOf(cursor.getInt(0));
                    String brand = cursor.getString(1);
                    String model = cursor.getString(2);
                    int year = cursor.getInt(3);
                    String vin = cursor.getString(4);
                    String color = cursor.getString(5);
                    double price = cursor.getDouble(6);
                    int mileage = cursor.getInt(7);
                    String status = cursor.getString(8);
                    String imageUrl = cursor.getString(9);

                    Car car = new Car(id, brand, model, year, vin, price, mileage, status, imageUrl);
                    car.setColor(color);
                    car.setEquipment("Стандарт");

                    cars.add(car);

                    Log.d(TAG, "Из упрощенной таблицы: " + brand + " " + model + ", цвет: " + color);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения из упрощенной таблицы", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return cars;
    }

    private Car createCarFromCursor(Cursor cursor) {
        try {
            String id = String.valueOf(cursor.getInt(cursor.getColumnIndex("ID")));
            String vin = cursor.getString(cursor.getColumnIndex("VIN"));
            String color = cursor.getString(cursor.getColumnIndex("Color"));
            double salePrice = cursor.getDouble(cursor.getColumnIndex("SalePrice"));
            double purchasePrice = cursor.getDouble(cursor.getColumnIndex("PurchasePrice"));
            int year = cursor.getInt(cursor.getColumnIndex("Year"));
            int mileage = cursor.getInt(cursor.getColumnIndex("Mileage"));
            String status = cursor.getString(cursor.getColumnIndex("Status"));
            String photo = cursor.getString(cursor.getColumnIndex("Photo"));
            String model = cursor.getString(cursor.getColumnIndex("ModelName"));
            String brand = cursor.getString(cursor.getColumnIndex("BrandName"));
            String equipment = cursor.getString(cursor.getColumnIndex("Equipment"));

            // Проверяем на null
            if (brand == null) brand = "Не указано";
            if (model == null) model = "Не указано";
            if (vin == null) vin = "";
            if (color == null) color = "";
            if (status == null) status = "В продаже";
            if (photo == null) photo = "";
            if (equipment == null) equipment = "";

            // Используем цену продажи (SalePrice) как цену для отображения
            double price = salePrice > 0 ? salePrice : purchasePrice;

            // Создаем объект Car
            Car car = new Car(id, brand, model, year, vin, price, mileage, status, photo);
            car.setColor(color);
            car.setEquipment(equipment);

            return car;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания автомобиля из курсора", e);
            return null;
        }
    }

    private Car createSimpleCarFromCursor(Cursor cursor) {
        try {
            String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            String brand = cursor.getString(cursor.getColumnIndex(COLUMN_BRAND));
            String model = cursor.getString(cursor.getColumnIndex(COLUMN_MODEL));
            String vin = cursor.getString(cursor.getColumnIndex(COLUMN_VIN));
            String color = cursor.getString(cursor.getColumnIndex(COLUMN_COLOR));
            double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
            int year = cursor.getInt(cursor.getColumnIndex(COLUMN_YEAR));
            int mileage = cursor.getInt(cursor.getColumnIndex(COLUMN_MILEAGE));
            String status = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
            String photo = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
            String equipment = cursor.getString(cursor.getColumnIndex(COLUMN_EQUIPMENT));

            // Проверяем на null
            if (brand == null) brand = "Не указано";
            if (model == null) model = "Не указано";
            if (vin == null) vin = "";
            if (color == null) color = "";
            if (status == null) status = "В продаже";
            if (photo == null) photo = "";
            if (equipment == null) equipment = "";

            // Создаем объект Car
            Car car = new Car(id, brand, model, year, vin, price, mileage, status, photo);
            car.setColor(color);
            car.setEquipment(equipment);

            return car;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания простого автомобиля из курсора", e);
            return null;
        }
    }

    public boolean deleteCar(String carId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Удаляем из таблицы Cars
            int rowsDeleted = db.delete(TABLE_CARS, COLUMN_ID + " = ?", new String[]{carId});

            if (rowsDeleted > 0) {
                Log.d(TAG, "Автомобиль удален. ID: " + carId);
                return true;
            } else {
                Log.w(TAG, "Автомобиль не найден для удаления. ID: " + carId);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении автомобиля: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean addCar(String brand, String model, int year, String vin,
                          String color, String description, double price,
                          int mileage, String status, String imageUrl, String equipment) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_VIN, vin);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_MILEAGE, mileage);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_IMAGE_URL, imageUrl);
        values.put(COLUMN_EQUIPMENT, equipment);

        try {
            long result = db.insert(TABLE_CARS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка добавления автомобиля: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateCar(String carId, String brand, String model, int year, String vin,
                             String color, double price, int mileage, String status,
                             String imageUrl, String equipment) {

        SQLiteDatabase db = super.getWritableDatabase();

        try {
            // 1. Находим или создаем бренд
            int brandId = findOrCreateBrand(brand, db);

            // 2. Находим или создаем модель
            int modelId = findOrCreateModel(model, brandId, db);

            // 3. Обновляем данные в таблице Cars
            ContentValues values = new ContentValues();
            values.put("ModelID", modelId);
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("Photo", imageUrl);
            values.put("SalePrice", price); // Используем SalePrice как основную цену
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", equipment);

            Log.d(TAG, "Обновляем автомобиль ID=" + carId +
                    ", ModelID=" + modelId + ", цена=" + price);

            int rowsAffected = db.update(TABLE_CARS, values, "ID = ?", new String[]{carId});

            if (rowsAffected > 0) {
                Log.d(TAG, "Успешно обновлено строк: " + rowsAffected);
                return true;
            } else {
                Log.d(TAG, "Ничего не обновлено. Возможно, ID не существует.");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления автомобиля: " + e.getMessage(), e);
            return false;
        }
    }

    // Метод для поиска или создания бренда
    private int findOrCreateBrand(String brandName, SQLiteDatabase db) {
        Cursor cursor = null;

        try {
            // Ищем существующий бренд
            cursor = db.rawQuery("SELECT ID FROM CarBrands WHERE Name = ?",
                    new String[]{brandName});

            if (cursor != null && cursor.moveToFirst()) {
                int brandId = cursor.getInt(0);
                Log.d(TAG, "Найден существующий бренд: " + brandName + " ID=" + brandId);
                return brandId;
            }

            // Создаем новый бренд
            ContentValues values = new ContentValues();
            values.put("Name", brandName);
            values.put("Country", "Не указано");

            long newBrandId = db.insert("CarBrands", null, values);
            Log.d(TAG, "Создан новый бренд: " + brandName + " ID=" + newBrandId);

            return (int) newBrandId;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка поиска/создания бренда: " + e.getMessage());
            return 1; // Возвращаем ID по умолчанию
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Метод для поиска или создания модели
    private int findOrCreateModel(String modelName, int brandId, SQLiteDatabase db) {
        Cursor cursor = null;

        try {
            // Ищем существующую модель
            cursor = db.rawQuery("SELECT ID FROM CarModels WHERE Name = ? AND BrandID = ?",
                    new String[]{modelName, String.valueOf(brandId)});

            if (cursor != null && cursor.moveToFirst()) {
                int modelId = cursor.getInt(0);
                Log.d(TAG, "Найдена существующая модель: " + modelName + " ID=" + modelId);
                return modelId;
            }

            // Создаем новую модель
            ContentValues values = new ContentValues();
            values.put("Name", modelName);
            values.put("BrandID", brandId);
            values.put("CarType", "Седан"); // Значение по умолчанию

            long newModelId = db.insert("CarModels", null, values);
            Log.d(TAG, "Создана новая модель: " + modelName + " ID=" + newModelId);

            return (int) newModelId;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка поиска/создания модели: " + e.getMessage());
            return 1; // Возвращаем ID по умолчанию
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Альтернативный метод обновления
    private boolean updateCarAlternative(String carId, String brand, String model, int year, String vin,
                                         String color, double price, int mileage, String status,
                                         String imageUrl, String equipment) {

        SQLiteDatabase db = super.getWritableDatabase();

        try {
            // Пробуем использовать прямой SQL
            String sql = "UPDATE " + TABLE_CARS + " SET " +
                    "Brand = ?, Model = ?, Year = ?, VIN = ?, Color = ?, " +
                    "Price = ?, Mileage = ?, Status = ?, Photo = ?, Equipment = ? " +
                    "WHERE " + COLUMN_ID + " = ?";

            db.execSQL(sql, new Object[]{brand, model, year, vin, color, price,
                    mileage, status, imageUrl, equipment, carId});

            Log.d(TAG, "Альтернативное обновление выполнено для ID: " + carId);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка альтернативного обновления: " + e.getMessage(), e);
            return false;
        }
    }

    // ============== МЕТОДЫ ДЛЯ РАБОТЫ С КЛИЕНТАМИ ==============

    public Client getClientById(int clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CLIENTS + " WHERE ID = ?",
                    new String[]{String.valueOf(clientId)});

            if (cursor != null && cursor.moveToFirst()) {
                return createClientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения клиента по ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    public Client getClientByPhoneOrEmail(String phone, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query;
            String[] args;

            if (phone != null && !phone.isEmpty() && email != null && !email.isEmpty()) {
                query = "SELECT * FROM " + TABLE_CLIENTS + " WHERE Phone = ? OR Email = ? LIMIT 1";
                args = new String[]{phone, email};
            } else if (phone != null && !phone.isEmpty()) {
                query = "SELECT * FROM " + TABLE_CLIENTS + " WHERE Phone = ? LIMIT 1";
                args = new String[]{phone};
            } else if (email != null && !email.isEmpty()) {
                query = "SELECT * FROM " + TABLE_CLIENTS + " WHERE Email = ? LIMIT 1";
                args = new String[]{email};
            } else {
                return null;
            }

            cursor = db.rawQuery(query, args);

            if (cursor != null && cursor.moveToFirst()) {
                return createClientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения клиента по телефону/email: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    private Client createClientFromCursor(Cursor cursor) {
        try {
            int id = cursor.getInt(cursor.getColumnIndex("ID"));
            String firstName = cursor.getString(cursor.getColumnIndex("FirstName"));
            String lastName = cursor.getString(cursor.getColumnIndex("LastName"));
            String phone = cursor.getString(cursor.getColumnIndex("Phone"));
            String email = cursor.getString(cursor.getColumnIndex("Email"));
            String address = cursor.getString(cursor.getColumnIndex("Address"));
            String passportSeries = cursor.getString(cursor.getColumnIndex("PassportSeries"));
            String passportNumber = cursor.getString(cursor.getColumnIndex("PassportNumber"));
            String issueDate = cursor.getString(cursor.getColumnIndex("IssueDate"));
            String issuedBy = cursor.getString(cursor.getColumnIndex("IssuedBy"));
            String registrationDate = cursor.getString(cursor.getColumnIndex("RegistrationDate"));

            return new Client(
                    id,
                    firstName != null ? firstName : "",
                    lastName != null ? lastName : "",
                    phone != null ? phone : "",
                    email != null ? email : "",
                    address != null ? address : "",
                    passportSeries != null ? passportSeries : "",
                    passportNumber != null ? passportNumber : "",
                    issueDate != null ? issueDate : "",
                    issuedBy != null ? issuedBy : "",
                    registrationDate != null ? registrationDate : ""
            );
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания клиента из курсора", e);
            return null;
        }
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CLIENTS + " ORDER BY FirstName", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Client client = createClientFromCursor(cursor);
                    if (client != null) {
                        clients.add(client);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения всех клиентов", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return clients;
    }

    // ============== МЕТОДЫ ДЛЯ РАБОТЫ С СОТРУДНИКАМИ ==============

    public Employee getEmployeeByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + " WHERE Email = ? LIMIT 1",
                    new String[]{email});

            if (cursor != null && cursor.moveToFirst()) {
                return createEmployeeFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения сотрудника по email: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEES + " ORDER BY FirstName", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Employee employee = createEmployeeFromCursor(cursor);
                    if (employee != null) {
                        employees.add(employee);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения всех сотрудников", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return employees;
    }
    private void createWorkingCarsTable() {
        SQLiteDatabase db = super.getWritableDatabase();

        // Создаем рабочую таблицу если её нет
        String CREATE_WORKING_CARS_TABLE = "CREATE TABLE IF NOT EXISTS WorkingCars (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_BRAND + " TEXT," +
                COLUMN_MODEL + " TEXT," +
                COLUMN_YEAR + " INTEGER," +
                COLUMN_VIN + " TEXT UNIQUE," +
                COLUMN_COLOR + " TEXT," +
                COLUMN_DESCRIPTION + " TEXT," +
                COLUMN_PRICE + " REAL," +
                COLUMN_MILEAGE + " INTEGER," +
                COLUMN_STATUS + " TEXT DEFAULT 'В продаже'," +
                COLUMN_PHOTO + " TEXT," +
                COLUMN_EQUIPMENT + " TEXT" +
                ")";
        db.execSQL(CREATE_WORKING_CARS_TABLE);

        Log.d(TAG, "Создана рабочая таблица WorkingCars");
    }
    private Employee createEmployeeFromCursor(Cursor cursor) {
        try {
            return new Employee(
                    cursor.getInt(cursor.getColumnIndex("ID")),
                    cursor.getString(cursor.getColumnIndex("FirstName")),
                    cursor.getString(cursor.getColumnIndex("LastName")),
                    cursor.getString(cursor.getColumnIndex("Phone")),
                    cursor.getString(cursor.getColumnIndex("Email")),
                    cursor.getString(cursor.getColumnIndex("Position")),
                    cursor.getString(cursor.getColumnIndex("BirthDate")),
                    cursor.getString(cursor.getColumnIndex("HireDate")),
                    cursor.getDouble(cursor.getColumnIndex("Salary")),
                    cursor.getString(cursor.getColumnIndex("Department"))
            );
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания сотрудника из курсора", e);
            return null;
        }
    }

    public int getEmployeeSalesCount(int employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_SALES + " WHERE EmployeesID = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId)});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения количества продаж сотрудника: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public double getEmployeeTotalSales(int employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT SUM(Price) FROM " + TABLE_SALES + " WHERE EmployeesID = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId)});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения общей суммы продаж сотрудника: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0.0;
    }

    public boolean updateEmployeePosition(int employeeId, String newPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Position", newPosition);

        try {
            int rowsAffected = db.update(TABLE_EMPLOYEES, values, "ID = ?", new String[]{String.valueOf(employeeId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления должности сотрудника", e);
            return false;
        }
    }

    // ============== МЕТОДЫ ДЛЯ РАБОТЫ С ПРОДАЖАМИ ==============

    public List<Deal> getAllDeals() {
        List<Deal> deals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT " +
                    "s.ID, s.DateSale, s.Price, " +
                    "c.VIN, " +
                    "cl.FirstName || ' ' || cl.LastName as ClientName, " +
                    "e.FirstName || ' ' || e.LastName as EmployeeName, " +
                    "pt.Name as PaymentType " +
                    "FROM " + TABLE_SALES + " s " +
                    "LEFT JOIN " + TABLE_CARS + " c ON s.CarsID = c.ID " +
                    "LEFT JOIN " + TABLE_CLIENTS + " cl ON s.ClientsID = cl.ID " +
                    "LEFT JOIN " + TABLE_EMPLOYEES + " e ON s.EmployeesID = e.ID " +
                    "LEFT JOIN " + TABLE_PAYMENT_TYPES + " pt ON s.PaymentTypesID = pt.ID " +
                    "ORDER BY s.DateSale DESC";

            Log.d(TAG, "Выполняем запрос сделок: " + query);

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = String.valueOf(cursor.getInt(0));
                    String saleDateStr = cursor.getString(1);
                    double price = cursor.getDouble(2);
                    String carVin = cursor.getString(3);
                    String clientName = cursor.getString(4);
                    String employeeName = cursor.getString(5);
                    String paymentType = cursor.getString(6);

                    // Парсим дату
                    Date saleDate = parseDate(saleDateStr);
                    String dealName = "Сделка #" + id + (carVin != null ? " (VIN: " + carVin + ")" : "");

                    // Создаем объект Deal
                    Deal deal = new Deal(id, dealName, saleDate, price, clientName);
                    deals.add(deal);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения всех сделок", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return deals;
    }

    // ============== МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ СПРАВОЧНИКАМИ ==============

    public static class CatalogItemDetail {
        private int id;
        private String name;
        private String description;

        public CatalogItemDetail(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public List<CatalogItemDetail> getCatalogItems(String tableName) {
        List<CatalogItemDetail> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query;

            switch (tableName) {
                case "CarBrands":
                    query = "SELECT ID, Name, Country FROM CarBrands ORDER BY Name";
                    break;
                case "CarModels":
                    query = "SELECT ID, Name, CarType FROM CarModels ORDER BY Name";
                    break;
                case "DriveTypes":
                    query = "SELECT ID, Name FROM DriveTypes ORDER BY Name";
                    break;
                case "EngineTypes":
                    query = "SELECT ID, Name FROM EngineTypes ORDER BY Name";
                    break;
                case "TransmissionTypes":
                    query = "SELECT ID, Name FROM TransmissionTypes ORDER BY Name";
                    break;
                case "PaymentTypes":
                    query = "SELECT ID, Name FROM PaymentTypes ORDER BY Name";
                    break;
                case "Employees":
                    query = "SELECT ID, FirstName, Position FROM Employees ORDER BY FirstName";
                    break;
                default:
                    return items;
            }

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    String description = "";

                    if (tableName.equals("CarBrands")) {
                        description = cursor.getString(2);
                    } else if (tableName.equals("CarModels")) {
                        description = cursor.getString(2);
                    } else if (tableName.equals("Employees")) {
                        description = cursor.getString(2);
                    }

                    items.add(new CatalogItemDetail(id, name, description));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения элементов каталога из " + tableName, e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return items;
    }

    public boolean addCatalogItem(String tableName, String itemName, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            switch (tableName) {
                case "CarBrands":
                    values.put("Name", itemName);
                    if (description != null && !description.isEmpty()) {
                        values.put("Country", description);
                    }
                    break;
                case "CarModels":
                    values.put("Name", itemName);
                    if (description != null && !description.isEmpty()) {
                        values.put("CarType", description);
                    }
                    Cursor brandCursor = db.rawQuery("SELECT ID FROM CarBrands LIMIT 1", null);
                    if (brandCursor != null && brandCursor.moveToFirst()) {
                        values.put("BrandID", brandCursor.getInt(0));
                    } else {
                        ContentValues brandValues = new ContentValues();
                        brandValues.put("Name", "Default Brand");
                        long brandId = db.insert("CarBrands", null, brandValues);
                        values.put("BrandID", brandId);
                    }
                    if (brandCursor != null) brandCursor.close();
                    break;
                case "DriveTypes":
                case "EngineTypes":
                case "TransmissionTypes":
                case "PaymentTypes":
                    values.put("Name", itemName);
                    break;
                case "Employees":
                    values.put("FirstName", itemName);
                    values.put("LastName", "Новый");
                    if (description != null && !description.isEmpty()) {
                        values.put("Position", description);
                    } else {
                        values.put("Position", "Сотрудник");
                    }
                    values.put("Phone", "+7");
                    values.put("Email", "new@example.com");
                    values.put("Salary", 0);
                    values.put("Department", "Не указан");
                    break;
                default:
                    return false;
            }

            long result = db.insert(tableName, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка добавления элемента в " + tableName + ": " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateCatalogItem(String tableName, int itemId, String newName, String newDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            switch (tableName) {
                case "CarBrands":
                    values.put("Name", newName);
                    values.put("Country", newDescription);
                    break;
                case "CarModels":
                    values.put("Name", newName);
                    values.put("CarType", newDescription);
                    break;
                case "DriveTypes":
                case "EngineTypes":
                case "TransmissionTypes":
                case "PaymentTypes":
                    values.put("Name", newName);
                    break;
                case "Employees":
                    values.put("FirstName", newName);
                    values.put("Position", newDescription);
                    break;
                default:
                    return false;
            }

            int rowsAffected = db.update(tableName, values, "ID = ?", new String[]{String.valueOf(itemId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления элемента в " + tableName, e);
            return false;
        }
    }

    public boolean deleteCatalogItem(String tableName, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            boolean canDelete = true;

            if (tableName.equals("CarBrands")) {
                Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM CarModels WHERE BrandID = ?",
                        new String[]{String.valueOf(itemId)});
                if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
                    canDelete = false;
                }
                if (cursor != null) cursor.close();
            } else if (tableName.equals("Employees")) {
                Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Sales WHERE EmployeesID = ?",
                        new String[]{String.valueOf(itemId)});
                if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
                    canDelete = false;
                }
                if (cursor != null) cursor.close();
            }

            if (!canDelete) {
                return false;
            }

            int rowsAffected = db.delete(tableName, "ID = ?", new String[]{String.valueOf(itemId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка удаления элемента из " + tableName, e);
            return false;
        }
    }

    // ============== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==============

    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки существования таблицы: " + tableName, e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        if (!isTableExists(db, tableName)) return true;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) == 0;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки пустоты таблицы: " + tableName, e);
            return true;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }

        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // Пробуем следующий формат
            }
        }

        Log.w(TAG, "Не удалось распарсить дату: " + dateStr + ", используется текущая дата");
        return new Date();
    }

    // ============== МЕТОДЫ ОТЛАДКИ ==============

    public void debugAllTables() {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Log.d(TAG, "=== СТРУКТУРА ВСЕХ ТАБЛИЦ ===");

            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String tableName = cursor.getString(0);
                    Log.d(TAG, "\nТаблица: " + tableName);

                    // Получаем структуру таблицы
                    Cursor columnCursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
                    if (columnCursor != null) {
                        while (columnCursor.moveToNext()) {
                            String colName = columnCursor.getString(1);
                            String colType = columnCursor.getString(2);
                            Log.d(TAG, "  - " + colName + " (" + colType + ")");
                        }
                        columnCursor.close();
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка отладки таблиц", e);
        }
    }


    public void debugDatabaseInfo() {
        SQLiteDatabase db = getReadableDatabase();

        try {
            Log.d(TAG, "=== ИНФОРМАЦИЯ О БАЗЕ ДАННЫХ ===");
            Log.d(TAG, "Путь к БД: " + DATABASE_PATH);
            Log.d(TAG, "БД существует: " + checkDatabaseExists());

            // 1. Проверяем таблицы
            try (Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null)) {

                Log.d(TAG, "=== ТАБЛИЦЫ В БАЗЕ ===");
                while (cursor != null && cursor.moveToNext()) {
                    String tableName = cursor.getString(0);
                    Log.d(TAG, "- " + tableName);

                    // Количество записей в таблице
                    try (Cursor countCursor = db.rawQuery(
                            "SELECT COUNT(*) FROM " + tableName, null)) {
                        if (countCursor != null && countCursor.moveToFirst()) {
                            Log.d(TAG, "  Записей: " + countCursor.getInt(0));
                        }
                    }
                }
            }

            // 2. Проверяем структуру таблицы Sales
            try (Cursor cursor = db.rawQuery("PRAGMA table_info(Sales)", null)) {
                Log.d(TAG, "=== СТРУКТУРА ТАБЛИЦЫ Sales ===");
                while (cursor != null && cursor.moveToNext()) {
                    String colName = cursor.getString(1);
                    String colType = cursor.getString(2);
                    Log.d(TAG, "  - " + colName + " (" + colType + ")");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отладке БД", e);
        }
    }
    // В класс DatabaseHelper добавьте эти методы:

    // Метод для отладки структуры таблицы
    public void debugTableStructure() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Получаем структуру таблицы Cars
            cursor = db.rawQuery("PRAGMA table_info(Cars)", null);

            Log.d(TAG, "=== СТРУКТУРА ТАБЛИЦЫ CARS ===");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String columnName = cursor.getString(1);
                    String columnType = cursor.getString(2);
                    Log.d(TAG, "Колонка: " + columnName + " (" + columnType + ")");
                } while (cursor.moveToNext());
            }

            // Показываем несколько записей для примера
            if (cursor != null) cursor.close();
            cursor = db.rawQuery("SELECT ID, VIN, Year, Color FROM Cars LIMIT 5", null);

            Log.d(TAG, "=== ПЕРВЫЕ 5 ЗАПИСЕЙ CARS ===");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String vin = cursor.getString(1);
                    int year = cursor.getInt(2);
                    String color = cursor.getString(3);
                    Log.d(TAG, "ID=" + id + ", VIN=" + vin + ", Year=" + year + ", Color=" + color);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка отладки структуры таблиц: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Метод updateCarSimple (исправьте сигнатуру)
    public boolean updateCarSimple(String carId, int year, String vin, String color,
                                   double price, int mileage, String status,
                                   String imageUrl, String equipment) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("Photo", imageUrl); // Используем Photo
            values.put("SalePrice", price);
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", equipment);

            Log.d(TAG, "Простое обновление автомобиля ID=" + carId);

            int rowsAffected = db.update(TABLE_CARS, values, "ID = ?", new String[]{carId});

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка простого обновления автомобиля: " + e.getMessage(), e);
            return false;
        }
    }

    // Метод для обновления только фото
    public boolean updateCarPhoto(String carId, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("Photo", imageUrl);

            Log.d(TAG, "Обновление фото автомобиля ID=" + carId);

            int rowsAffected = db.update(TABLE_CARS, values, "ID = ?", new String[]{carId});

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления фото автомобиля: " + e.getMessage(), e);
            return false;
        }
    }

    public void checkDatabaseTables() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String[] tables = {"CarBrands", "CarModels", "Cars", "Employees", "Clients", "Sales"};

            for (String table : tables) {
                if (!isTableExists(db, table)) {
                    Log.w(TAG, "Таблица " + table + " не существует");
                } else {
                    Log.d(TAG, "Таблица " + table + " существует");
                }
            }

            Log.d(TAG, "Проверка таблиц завершена успешно");

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке таблиц: " + e.getMessage(), e);
        }
    }
    // В класс DatabaseHelper добавьте этот метод
    private void fixDatabasePermissions() {
        try {
            File dbFile = new File(DATABASE_PATH);
            if (dbFile.exists()) {
                // Проверяем и устанавливаем права на запись
                boolean writable = dbFile.setWritable(true);
                boolean readable = dbFile.setReadable(true);
                Log.d(TAG, "Права базы данных: writable=" + writable + ", readable=" + readable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка установки прав базы данных", e);
        }
    }

    // Обновите метод getWritableDatabase()
    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (database != null && database.isOpen() && !database.isReadOnly()) {
            return database;
        }

        try {
            copyDatabaseWithPermissions();
            return database;
        } catch (Exception e) {
            Log.e(TAG, "Не удалось получить базу для записи: " + e.getMessage(), e);
            return super.getWritableDatabase();
        }
    }
    // ============== МЕТОДЫ ДЛЯ ДОБАВЛЕНИЯ КЛИЕНТА ==============

    public boolean addClient(String firstName, String lastName, String phone,
                             String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Проверяем, существует ли клиент с таким телефоном или email
            if (phone != null && !phone.isEmpty()) {
                Client existing = getClientByPhoneOrEmail(phone, null);
                if (existing != null) {
                    Log.w(TAG, "Клиент с таким телефоном уже существует: " + phone);
                    return false;
                }
            }

            if (email != null && !email.isEmpty()) {
                Client existing = getClientByPhoneOrEmail(null, email);
                if (existing != null) {
                    Log.w(TAG, "Клиент с таким email уже существует: " + email);
                    return false;
                }
            }

            values.put("FirstName", firstName != null ? firstName : "");
            values.put("LastName", lastName != null ? lastName : "");
            values.put("Phone", phone != null ? phone : "");
            values.put("Email", email != null ? email : "");
            values.put("Address", address != null ? address : "");
            values.put("PassportSeries", "");
            values.put("PassportNumber", "");
            values.put("IssueDate", "");
            values.put("IssuedBy", "");
            values.put("RegistrationDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            long result = db.insert(TABLE_CLIENTS, null, values);

            if (result != -1) {
                Log.d(TAG, "Клиент успешно добавлен: " + firstName + " " + lastName + ", ID: " + result);
                return true;
            } else {
                Log.e(TAG, "Ошибка при добавлении клиента в базу");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в методе addClient: " + e.getMessage(), e);
            return false;
        }
    }
    public boolean addClientFull(String firstName, String lastName, String phone,
                                 String email, String address, String passportSeries,
                                 String passportNumber, String issueDate, String issuedBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("FirstName", firstName);
            values.put("LastName", lastName);
            values.put("Phone", phone);
            values.put("Email", email);
            values.put("Address", address);
            values.put("PassportSeries", passportSeries);
            values.put("PassportNumber", passportNumber);
            values.put("IssueDate", issueDate);
            values.put("IssuedBy", issuedBy);
            values.put("RegistrationDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            long result = db.insert(TABLE_CLIENTS, null, values);

            return result != -1;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении клиента: " + e.getMessage(), e);
            return false;
        }
    }

// ============== МЕТОДЫ ДЛЯ ДОБАВЛЕНИЯ СДЕЛКИ ==============

    public boolean addDeal(int carId, int clientId, int employeeId,
                           double amount, String paymentType, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Получаем ID типа оплаты
            int paymentTypeId = getPaymentTypeId(paymentType);

            values.put("CarsID", carId);
            values.put("ClientsID", clientId);
            values.put("EmployeesID", employeeId);
            values.put("Price", amount);
            values.put("PaymentTypesID", paymentTypeId);
            values.put("DateSale", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            if (details != null && !details.isEmpty()) {
                values.put("Description", details);
            }

            long result = db.insert(TABLE_SALES, null, values);

            if (result != -1) {
                // Обновляем статус автомобиля на "Продан"
                updateCarStatus(carId, "Продан");
                Log.d(TAG, "Сделка успешно добавлена: ID=" + result + ", сумма=" + amount);
                return true;
            } else {
                Log.e(TAG, "Ошибка при добавлении сделки в базу");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в методе addDeal: " + e.getMessage(), e);
            return false;
        }
    }
    // Метод для получения ID первого сотрудника
    private int getFirstEmployeeId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Сначала проверяем таблицу Employees
            cursor = db.rawQuery("SELECT ID FROM " + TABLE_EMPLOYEES + " LIMIT 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                Log.d(TAG, "Найден сотрудник ID: " + id);
                return id;
            }

            // Если сотрудников нет, создаем одного по умолчанию
            Log.d(TAG, "Сотрудников нет, создаем по умолчанию...");

            ContentValues values = new ContentValues();
            values.put("FirstName", "Администратор");
            values.put("LastName", "Системы");
            values.put("Phone", "+79990000000");
            values.put("Email", "admin@avto.ru");
            values.put("Position", "Менеджер");
            values.put("BirthDate", "1990-01-01");
            values.put("HireDate", "2020-01-01");
            values.put("Salary", 50000.0);
            values.put("Department", "Продажи");

            long newEmployeeId = db.insert(TABLE_EMPLOYEES, null, values);

            if (newEmployeeId != -1) {
                Log.d(TAG, "Создан сотрудник по умолчанию ID: " + newEmployeeId);
                return (int) newEmployeeId;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения ID сотрудника: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        // Возвращаем 1 по умолчанию
        Log.w(TAG, "Не удалось получить/создать сотрудника, используем ID=1");
        return 1;
    }
    public boolean addDealSimple(String carName, String clientName, double amount, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Для упрощенной версии, находим ID автомобиля и клиента
            int carId = findCarIdByName(carName);
            int clientId = findClientIdByName(clientName);

            Log.d(TAG, "Создание сделки: carId=" + carId + ", clientId=" + clientId + ", amount=" + amount);

            if (carId == -1) {
                Log.w(TAG, "Автомобиль не найден: " + carName);
                return false;
            }

            if (clientId == -1) {
                Log.w(TAG, "Клиент не найден: " + clientName);
                return false;
            }

            // Получаем первого сотрудника по умолчанию
            int employeeId = getFirstEmployeeId();
            int paymentTypeId = 1; // Наличные по умолчанию

            // Заполняем значения согласно структуре таблицы Sales
            values.put("EmployeesID", employeeId);
            values.put("ClientsID", clientId);
            values.put("CarsID", carId);
            values.put("PaymentTypesID", paymentTypeId);
            values.put("Price", amount);
            values.put("Commission", 0.0); // Комиссия по умолчанию 0
            values.put("Discount", 0.0);   // Скидка по умолчанию 0
            values.put("DateSale", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            if (details != null && !details.isEmpty()) {
                values.put("Description", details);
            }

            long result = db.insert(TABLE_SALES, null, values);

            if (result != -1) {
                // Обновляем статус автомобиля
                updateCarStatus(String.valueOf(carId), "Продан");
                Log.d(TAG, "Упрощенная сделка добавлена: ID=" + result +
                        ", CarID=" + carId + ", ClientID=" + clientId + ", Amount=" + amount);
                return true;
            } else {
                Log.e(TAG, "Ошибка вставки в таблицу Sales");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении упрощенной сделки: " + e.getMessage(), e);
            return false;
        }
    }

// ============== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==============

    private int getPaymentTypeId(String paymentTypeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT ID FROM PaymentTypes WHERE Name = ? LIMIT 1",
                    new String[]{paymentTypeName});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // Создаем новый тип оплаты, если не найден
                ContentValues values = new ContentValues();
                values.put("Name", paymentTypeName);
                long newId = db.insert("PaymentTypes", null, values);
                return (int) newId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения ID типа оплаты: " + e.getMessage(), e);
            return 1; // По умолчанию "Наличные"
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void updateCarStatus(int carId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Status", status);

        try {
            db.update(TABLE_CARS, values, "ID = ?", new String[]{String.valueOf(carId)});
            Log.d(TAG, "Статус автомобиля ID=" + carId + " обновлен на: " + status);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления статуса автомобиля: " + e.getMessage(), e);
        }
    }

    public void updateCarStatus(String carId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Status", status);

        try {
            int rows = db.update(TABLE_CARS, values, "ID = ?", new String[]{carId});
            Log.d(TAG, "Обновлен статус автомобиля. ID=" + carId + ", новый статус=" + status + ", затронуто строк=" + rows);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления статуса автомобиля: " + e.getMessage(), e);
        }
    }
    // Добавь этот метод в DatabaseHelper.java
    public boolean addCarWithLocalImage(String brand, String model, int year, String vin,
                                        String color, String imagePath, double price,
                                        int mileage, String status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Проверяем, существует ли уже автомобиль с таким VIN
            Cursor cursor = db.rawQuery("SELECT ID FROM Cars WHERE VIN = ?", new String[]{vin});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                Log.w(TAG, "Автомобиль с таким VIN уже существует: " + vin);
                return false;
            }
            if (cursor != null) cursor.close();

            // Для простоты - используем фиксированные ID для бренда и модели
            int brandId = 1; // По умолчанию
            int modelId = 1; // По умолчанию

            // Сохраняем путь к файлу
            String photoUri = "file://" + imagePath;

            values.put("ModelID", modelId);
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("Photo", photoUri);
            values.put("SalePrice", price);
            values.put("PurchasePrice", price * 0.8);
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", "Стандарт");
            values.put("ArrivalDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            long result = db.insert(TABLE_CARS, null, values);

            if (result != -1) {
                Log.d(TAG, "Автомобиль успешно добавлен: " + brand + " " + model + ", VIN: " + vin);
                return true;
            } else {
                Log.e(TAG, "Ошибка при добавлении автомобиля в базу");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в методе addCarWithLocalImage: " + e.getMessage(), e);
            return false;
        }
    }
    public boolean addCarWithUri(String brand, String model, int year, String vin,
                                 String color, String imageUri, double price,
                                 int mileage, String status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            Log.d(TAG, "=== ДОБАВЛЕНИЕ АВТОМОБИЛЯ ===");
            Log.d(TAG, "Полученная марка: " + brand);
            Log.d(TAG, "Полученная модель: " + model);
            Log.d(TAG, "Полученный цвет: " + color);

            // Проверяем VIN
            Cursor cursor = db.rawQuery("SELECT ID FROM Cars WHERE VIN = ?", new String[]{vin});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                Log.w(TAG, "Автомобиль с таким VIN уже существует: " + vin);
                return false;
            }
            if (cursor != null) cursor.close();

            // СПОСОБ 1: Используем упрощенную таблицу если она есть
            if (isTableExists(db, "CarsSimple")) {
                // Сохраняем в упрощенную таблицу
                return addToSimpleCarsTable(db, brand, model, year, vin, color,
                        imageUri, price, mileage, status);
            }

            // СПОСОБ 2: Используем существующую структуру с поиском/созданием бренда и модели
            int brandId = findOrCreateBrand(brand, db);
            int modelId = findOrCreateModel(model, brandId, db);

            Log.d(TAG, "Создан бренд ID: " + brandId + " для: " + brand);
            Log.d(TAG, "Создана модель ID: " + modelId + " для: " + model);

            values.put("ModelID", modelId);
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("Photo", imageUri);
            values.put("SalePrice", price);
            values.put("PurchasePrice", price * 0.8);
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", "Стандарт");
            values.put("ArrivalDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            long result = db.insert(TABLE_CARS, null, values);

            Log.d(TAG, "Результат вставки: " + result);

            // Проверяем что сохранилось
            if (result != -1) {
                checkLastAddedCar();
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в методе addCarWithUri: " + e.getMessage(), e);
            return false;
        }
    }
    private boolean addToSimpleCarsTable(SQLiteDatabase db, String brand, String model,
                                         int year, String vin, String color,
                                         String imageUri, double price, int mileage,
                                         String status) {

        try {
            ContentValues values = new ContentValues();

            values.put("Brand", brand);
            values.put("Model", model);
            values.put("Year", year);
            values.put("VIN", vin);
            values.put("Color", color);
            values.put("ImageUrl", imageUri);
            values.put("Price", price);
            values.put("Mileage", mileage);
            values.put("Status", status);
            values.put("Equipment", "Стандарт");
            values.put("CreatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long result = db.insert("CarsSimple", null, values);

            Log.d(TAG, "Сохранено в упрощенную таблицу. Результат: " + result);
            return result != -1;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка сохранения в упрощенную таблицу", e);
            return false;
        }
    }
    public void checkLastAddedCar() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Проверяем последнюю добавленную запись
            String query = "SELECT " +
                    "c.ID, c.VIN, c.Color, " +
                    "cb.Name as BrandName, cm.Name as ModelName " +
                    "FROM Cars c " +
                    "LEFT JOIN CarModels cm ON c.ModelID = cm.ID " +
                    "LEFT JOIN CarBrands cb ON cm.BrandID = cb.ID " +
                    "ORDER BY c.ID DESC LIMIT 1";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(0);
                String vin = cursor.getString(1);
                String color = cursor.getString(2);
                String brand = cursor.getString(3);
                String model = cursor.getString(4);

                Log.d(TAG, "=== ПОСЛЕДНИЙ ДОБАВЛЕННЫЙ АВТОМОБИЛЬ ===");
                Log.d(TAG, "ID: " + id);
                Log.d(TAG, "VIN: " + vin);
                Log.d(TAG, "Цвет: " + color);
                Log.d(TAG, "Марка (из таблицы): " + brand);
                Log.d(TAG, "Модель (из таблицы): " + model);
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки автомобиля", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    private int findCarIdByName(String carName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Ищем по VIN или модели
            cursor = db.rawQuery("SELECT ID FROM Cars WHERE VIN LIKE ? OR ID IN " +
                            "(SELECT c.ID FROM Cars c JOIN CarModels cm ON c.ModelID = cm.ID " +
                            "WHERE cm.Name LIKE ?) LIMIT 1",
                    new String[]{"%" + carName + "%", "%" + carName + "%"});

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка поиска ID автомобиля: " + e.getMessage(), e);
            return -1;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Упрощенный метод для поиска клиента
    private int findClientIdByName(String clientName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            Log.d(TAG, "Поиск клиента по имени: " + clientName);

            // Разбираем имя на части
            String[] nameParts = clientName.split(" ");
            String firstName = "";
            String lastName = "";

            if (nameParts.length >= 1) {
                firstName = nameParts[0];
                if (nameParts.length >= 2) {
                    lastName = nameParts[1];
                }
            }

            String query;
            String[] args;

            if (!lastName.isEmpty()) {
                // Ищем по имени и фамилии
                query = "SELECT ID FROM Clients WHERE FirstName LIKE ? AND LastName LIKE ? LIMIT 1";
                args = new String[]{firstName, lastName};
            } else {
                // Ищем только по имени
                query = "SELECT ID FROM Clients WHERE FirstName LIKE ? LIMIT 1";
                args = new String[]{firstName};
            }

            cursor = db.rawQuery(query, args);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                Log.d(TAG, "Найден клиент ID: " + id);
                return id;
            } else {
                Log.d(TAG, "Клиент не найден: " + clientName);
                // Пробуем найти любого клиента
                cursor = db.rawQuery("SELECT ID FROM Clients LIMIT 1", null);
                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(0);
                    Log.d(TAG, "Используем первого клиента ID: " + id);
                    return id;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка поиска ID клиента: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return -1;
    }

    // Метод для получения всех клиентов в виде списка для Spinner
    public List<String> getAllClientNames() {
        List<String> clientNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT FirstName, LastName FROM " + TABLE_CLIENTS + " ORDER BY FirstName";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String firstName = cursor.getString(0);
                    String lastName = cursor.getString(1);

                    String fullName = (firstName != null ? firstName : "") +
                            " " +
                            (lastName != null ? lastName : "");

                    clientNames.add(fullName.trim());
                } while (cursor.moveToNext());
            }

            // Если список пуст, добавляем заглушку
            if (clientNames.isEmpty()) {
                clientNames.add("Нет клиентов");
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения списка клиентов: " + e.getMessage(), e);
            clientNames.add("Нет клиентов");
        } finally {
            if (cursor != null) cursor.close();
        }

        Log.d(TAG, "Получено клиентов: " + clientNames.size());
        return clientNames;
    }
// ============== МЕТОДЫ ДЛЯ РАБОТЫ С МАРКАМИ И МОДЕЛЯМИ ==============

    public List<String> getAllCarBrands() {
        List<String> brands = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT DISTINCT Name FROM CarBrands ORDER BY Name";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String brandName = cursor.getString(0);
                    if (brandName != null && !brandName.isEmpty()) {
                        brands.add(brandName);
                    }
                } while (cursor.moveToNext());
            }

            // Если таблицы нет или она пуста, возвращаем тестовые данные
            if (brands.isEmpty()) {
                brands.add("Toyota");
                brands.add("BMW");
                brands.add("Mercedes");
                brands.add("Audi");
                brands.add("Ford");
                Log.d(TAG, "Возвращаем тестовые марки автомобилей");
            }

            Log.d(TAG, "Получено марок автомобилей: " + brands.size());

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения марок автомобилей: " + e.getMessage(), e);
            // Возвращаем тестовые данные при ошибке
            if (brands.isEmpty()) {
                brands.add("Toyota");
                brands.add("BMW");
                brands.add("Mercedes");
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return brands;
    }

    public List<String> getAllCarModels() {
        List<String> models = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT DISTINCT Name FROM CarModels ORDER BY Name";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String modelName = cursor.getString(0);
                    if (modelName != null && !modelName.isEmpty()) {
                        models.add(modelName);
                    }
                } while (cursor.moveToNext());
            }

            // Если таблицы нет или она пуста, возвращаем тестовые данные
            if (models.isEmpty()) {
                models.add("Camry");
                models.add("X5");
                models.add("C-Class");
                models.add("A4");
                models.add("Focus");
                Log.d(TAG, "Возвращаем тестовые модели автомобилей");
            }

            Log.d(TAG, "Получено моделей автомобилей: " + models.size());

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения моделей автомобилей: " + e.getMessage(), e);
            // Возвращаем тестовые данные при ошибке
            if (models.isEmpty()) {
                models.add("Camry");
                models.add("X5");
                models.add("C-Class");
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return models;
    }

    // Альтернативный метод для получения марок с проверкой таблицы
    public List<String> getAllCarBrandsWithCheck() {
        List<String> brands = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Проверяем, существует ли таблица CarBrands
            if (isTableExists(db, "CarBrands")) {
                Cursor cursor = db.rawQuery("SELECT DISTINCT Name FROM CarBrands ORDER BY Name", null);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        brands.add(cursor.getString(0));
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Log.d(TAG, "Марки загружены из таблицы CarBrands: " + brands.size() + " шт.");
            } else {
                Log.w(TAG, "Таблица CarBrands не существует!");
                // Создаем тестовые данные
                createTestBrandsAndModels(db);
                brands.add("Toyota");
                brands.add("BMW");
                brands.add("Mercedes");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getAllCarBrandsWithCheck: " + e.getMessage(), e);
        }

        return brands;
    }

    // Вспомогательный метод для создания тестовых данных
    private void createTestBrandsAndModels(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Создание тестовых данных марок и моделей...");

            // Тестовые марки
            String[] testBrands = {"Toyota", "BMW", "Mercedes", "Audi", "Ford", "Hyundai", "Kia"};
            // Тестовые модели для каждой марки
            Map<String, String[]> testModels = new HashMap<>();
            testModels.put("Toyota", new String[]{"Camry", "Corolla", "RAV4", "Land Cruiser"});
            testModels.put("BMW", new String[]{"X5", "X3", "3 Series", "5 Series"});
            testModels.put("Mercedes", new String[]{"C-Class", "E-Class", "S-Class", "GLE"});
            testModels.put("Audi", new String[]{"A4", "A6", "Q5", "Q7"});
            testModels.put("Ford", new String[]{"Focus", "Mondeo", "Explorer", "Mustang"});

            // Добавляем марки
            for (String brand : testBrands) {
                ContentValues brandValues = new ContentValues();
                brandValues.put("Name", brand);
                brandValues.put("Country", getCountryForBrand(brand));
                db.insert("CarBrands", null, brandValues);
            }

            // Добавляем модели
            for (Map.Entry<String, String[]> entry : testModels.entrySet()) {
                String brand = entry.getKey();
                String[] models = entry.getValue();

                // Находим ID бренда
                Cursor cursor = db.rawQuery("SELECT ID FROM CarBrands WHERE Name = ?", new String[]{brand});
                int brandId = -1;
                if (cursor != null && cursor.moveToFirst()) {
                    brandId = cursor.getInt(0);
                    cursor.close();
                }

                if (brandId != -1) {
                    for (String model : models) {
                        ContentValues modelValues = new ContentValues();
                        modelValues.put("Name", model);
                        modelValues.put("BrandID", brandId);
                        modelValues.put("CarType", "Седан"); // По умолчанию
                        db.insert("CarModels", null, modelValues);
                    }
                }
            }

            Log.d(TAG, "Тестовые данные марок и моделей созданы успешно");

        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания тестовых данных: " + e.getMessage(), e);
        }
    }

    private String getCountryForBrand(String brand) {
        switch (brand) {
            case "Toyota": return "Япония";
            case "BMW": return "Германия";
            case "Mercedes": return "Германия";
            case "Audi": return "Германия";
            case "Ford": return "США";
            case "Hyundai": return "Корея";
            case "Kia": return "Корея";
            default: return "Неизвестно";
        }
    }
    // В DatabaseHelper.java добавьте:
    public boolean addEmployeeDirect(String fullName, String position, String email,
                                     String phone, double salary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        values.put("FirstName", firstName);
        values.put("LastName", lastName);
        values.put("Position", position);
        values.put("Email", email);
        values.put("Phone", phone);
        values.put("Salary", salary);
        values.put("Department", "Продажи");
        values.put("HireDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        try {
            long result = db.insert(TABLE_EMPLOYEES, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка добавления сотрудника", e);
            return false;
        }
    }
    // В классе DatabaseHelper добавьте этот метод (после других методов для работы с сотрудниками):

    public boolean updateEmployee(Employee employee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put("FirstName", employee.getFirstName());
            values.put("LastName", employee.getLastName());
            values.put("Phone", employee.getPhone());
            values.put("Email", employee.getEmail());
            values.put("Position", employee.getPosition());
            values.put("BirthDate", employee.getBirthDate());
            values.put("HireDate", employee.getHireDate());
            values.put("Salary", employee.getSalary());
            values.put("Department", employee.getDepartment());

            int rowsAffected = db.update(TABLE_EMPLOYEES, values,
                    "ID = ?",
                    new String[]{String.valueOf(employee.getId())});

            Log.d(TAG, "Обновлен сотрудник ID=" + employee.getId() +
                    ", затронуто строк: " + rowsAffected);

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления сотрудника: " + e.getMessage(), e);
            return false;
        }
    }

    // Альтернативная упрощенная версия (только основные поля):
    public boolean updateEmployeeSimple(Employee employee) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            String sql = "UPDATE " + TABLE_EMPLOYEES + " SET " +
                    "FirstName = ?, LastName = ?, Phone = ?, " +
                    "Email = ?, Position = ?, Salary = ? " +
                    "WHERE ID = ?";

            db.execSQL(sql, new Object[]{
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getPhone(),
                    employee.getEmail(),
                    employee.getPosition(),
                    employee.getSalary(),
                    employee.getId()
            });

            Log.d(TAG, "Сотрудник обновлен: " + employee.getFullName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка обновления сотрудника: " + e.getMessage(), e);
            return false;
        }
    }
    // В классе DatabaseHelper добавьте этот метод (в разделе методов для работы с сотрудниками):

    public boolean deleteEmployee(int employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Сначала проверяем, есть ли у сотрудника связанные продажи
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SALES +
                            " WHERE EmployeesID = ?",
                    new String[]{String.valueOf(employeeId)});

            boolean hasSales = false;
            if (cursor != null && cursor.moveToFirst()) {
                hasSales = cursor.getInt(0) > 0;
                cursor.close();
            }

            if (hasSales) {
                Log.w(TAG, "Нельзя удалить сотрудника с продажами. ID: " + employeeId);
                return false; // Не удаляем сотрудника с продажами
            }

            // Удаляем сотрудника
            int rowsDeleted = db.delete(TABLE_EMPLOYEES, "ID = ?",
                    new String[]{String.valueOf(employeeId)});

            if (rowsDeleted > 0) {
                Log.d(TAG, "Сотрудник удален. ID: " + employeeId);
                return true;
            } else {
                Log.w(TAG, "Сотрудник не найден для удаления. ID: " + employeeId);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении сотрудника: " + e.getMessage(), e);
            return false;
        }
    }

    // Альтернативная упрощенная версия (без проверки продаж):
    public boolean deleteEmployeeSimple(int employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsDeleted = db.delete(TABLE_EMPLOYEES, "ID = ?",
                    new String[]{String.valueOf(employeeId)});

            Log.d(TAG, "Удален сотрудник. ID: " + employeeId + ", удалено строк: " + rowsDeleted);
            return rowsDeleted > 0;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка удаления сотрудника: " + e.getMessage(), e);
            return false;
        }
    }
    public boolean deleteDeal(int dealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Удаляем сделку по ID
            int rowsAffected = db.delete("Sales", "ID = ?", new String[]{String.valueOf(dealId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting deal: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Или если ID хранится как String:
    public boolean deleteDeal(String dealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsAffected = db.delete("Sales", "ID = ?", new String[]{dealId});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting deal: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    // Для int ID:
    public boolean deleteClient(int clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsAffected = db.delete("Clients", "ID = ?",
                    new String[]{String.valueOf(clientId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting client: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Или для String ID:
    public boolean deleteClient(String clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsAffected = db.delete("Clients", "ID = ?",
                    new String[]{clientId});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting client: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
    public boolean backupDatabase() {
        try {
            // Простая временная реализация
            Log.d(TAG, "Backup database function called");

            // Здесь можно добавить реальную логику бэкапа
            // Например, копирование файла базы данных

            return true; // Временно возвращаем true
        } catch (Exception e) {
            Log.e(TAG, "Error in backupDatabase: " + e.getMessage());
            return false;
        }
    }
    // Обновление марки автомобиля
    public boolean updateCarBrand(String oldBrand, String newBrand) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("Name", newBrand);

            int rowsAffected = db.update("CarBrands", values, "Name = ?",
                    new String[]{oldBrand});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating car brand: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Удаление марки автомобиля
    public boolean deleteCarBrand(String brand) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsAffected = db.delete("CarBrands", "Name = ?",
                    new String[]{brand});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting car brand: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Метод для получения всех автомобилей в виде списка для Spinner
    public List<String> getAllCarNames() {
        List<String> carNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT VIN, Status FROM Cars ORDER BY VIN";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String vin = cursor.getString(0);
                    String status = cursor.getString(1);

                    // Только непроданные автомобили
                    if (!"Продан".equals(status)) {
                        carNames.add(vin); // Показываем только VIN для простоты
                    }
                } while (cursor.moveToNext());
            }

            // Если список пуст, добавляем заглушку
            if (carNames.isEmpty()) {
                carNames.add("Нет доступных автомобилей");
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения списка автомобилей: " + e.getMessage(), e);
            carNames.add("Нет доступных автомобилей");
        } finally {
            if (cursor != null) cursor.close();
        }

        Log.d(TAG, "Получено автомобилей для продажи: " + carNames.size());
        return carNames;
    }
    // ============== ЗАКРЫТИЕ СОЕДИНЕНИЯ ==============

    @Override
    public synchronized void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.close();
    }
}