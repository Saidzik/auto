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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleImagePicker {

    private static final String TAG = "SimpleImagePicker";
    private Activity activity;
    private String currentPhotoPath;
    private OnImagePickedListener listener;

    public static final int REQUEST_TAKE_PHOTO = 1001;
    public static final int REQUEST_PICK_PHOTO = 1002;
    public static final int REQUEST_CAMERA_PERMISSION = 1003;
    public static final int REQUEST_STORAGE_PERMISSION = 1004;

    public interface OnImagePickedListener {
        void onImagePicked(Uri imageUri);
        void onImagePickFailed(String error);
    }

    public SimpleImagePicker(Activity activity) {
        this.activity = activity;
    }

    public void setOnImagePickedListener(OnImagePickedListener listener) {
        this.listener = listener;
    }

    // Простой диалог выбора
    public void showSimpleImagePicker() {
        String[] options = {"Камера", "Галерея", "Отмена"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle("Выберите фото");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Камера
                    takePhotoWithCamera();
                    break;
                case 1: // Галерея
                    pickPhotoFromGallery();
                    break;
                case 2: // Отмена
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    // Сделать фото
    private void takePhotoWithCamera() {
        // Проверяем разрешение
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Убедимся, что есть приложение для обработки
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Создаем файл для сохранения фото
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            activity.getPackageName() + ".provider",
                            photoFile);

                    currentPhotoPath = photoFile.getAbsolutePath();

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка камеры: " + e.getMessage(), e);
            Toast.makeText(activity, "Ошибка при запуске камеры", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onImagePickFailed("Ошибка камеры: " + e.getMessage());
            }
        }
    }

    // Выбрать из галереи
    private void pickPhotoFromGallery() {
        // Проверяем разрешение для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_STORAGE_PERMISSION);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                return;
            }
        }

        try {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPhotoIntent.setType("image/*");
            activity.startActivityForResult(pickPhotoIntent, REQUEST_PICK_PHOTO);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка галереи: " + e.getMessage(), e);
            Toast.makeText(activity, "Ошибка при выборе фото", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onImagePickFailed("Ошибка галереи: " + e.getMessage());
            }
        }
    }

    // Создание файла для фото
    private File createImageFile() throws IOException {
        // Создаем уникальное имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Получаем директорию для сохранения
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = activity.getFilesDir();
        }

        // Создаем директорию если её нет
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Создаем файл
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    // Обработка результата
    public Uri handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO && currentPhotoPath != null) {
                // Фото с камеры
                File file = new File(currentPhotoPath);
                if (file.exists()) {
                    return FileProvider.getUriForFile(activity,
                            activity.getPackageName() + ".provider",
                            file);
                }
            } else if (requestCode == REQUEST_PICK_PHOTO && data != null) {
                // Фото из галереи
                return data.getData();
            }
        }
        return null;
    }

    // Обработка разрешений
    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    takePhotoWithCamera();
                    break;
                case REQUEST_STORAGE_PERMISSION:
                    pickPhotoFromGallery();
                    break;
            }
        } else {
            Toast.makeText(activity, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
}