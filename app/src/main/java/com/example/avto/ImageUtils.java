package com.example.avto.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.avto.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    private static final String TAG = "ImageUtils";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
    public static final int REQUEST_PERMISSION_CAMERA = 3;
    public static final int REQUEST_PERMISSION_STORAGE = 4;

    private Activity activity;
    private String currentPhotoPath;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    // Проверка разрешений для камеры
    public boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Проверка разрешений для хранилища
    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Запрос разрешений для камеры
    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CAMERA);
    }

    // Запрос разрешений для хранилища
    public void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_PERMISSION_STORAGE);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_STORAGE);
        }
    }

    // Создание файла для фото
    private File createImageFile() throws IOException {
        // Создаем имя файла с timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "car_photo_" + timeStamp;

        // Создаем папку для хранения фото, если её нет
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Создаем файл
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Сохраняем путь для использования позже
        currentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    // Запуск камеры
    public boolean takePhoto() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Убедимся, что есть Activity для обработки intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Создаем файл для сохранения фото
                File photoFile = createImageFile();

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            activity.getPackageName() + ".provider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при запуске камеры: " + e.getMessage(), e);
            Toast.makeText(activity, "Ошибка при запуске камеры", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Выбор фото из галереи
    public boolean pickPhotoFromGallery() {
        try {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            pickPhotoIntent.setType("image/*");
            activity.startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выборе фото: " + e.getMessage(), e);
            Toast.makeText(activity, "Ошибка при выборе фото", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Получение пути к текущему фото
    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    // Обработка результата выбора фото
    public String handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Фото сделано камерой
                if (currentPhotoPath != null) {
                    return "file://" + currentPhotoPath;
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Фото выбрано из галереи
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    return selectedImage.toString();
                }
            }
        }
        return null;
    }

    // Показать диалог выбора источника фото
    public void showImageSourceDialog(OnImageSourceSelectedListener listener) {
        String[] options = {"Сделать фото", "Выбрать из галереи", "Отмена"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("Выберите источник фото");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (listener != null) listener.onCameraSelected();
                    break;
                case 1:
                    if (listener != null) listener.onGallerySelected();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    public interface OnImageSourceSelectedListener {
        void onCameraSelected();
        void onGallerySelected();
    }
}