package com.example.avto.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageManager {
    private static final String TAG = "ImageManager";

    // Конвертация Bitmap в Base64 (для хранения в SQLite)
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Конвертация Base64 в Bitmap (для отображения)
    public static Bitmap base64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error converting base64 to bitmap: " + e.getMessage());
            return null;
        }
    }

    // Загрузка изображения в ImageView из URL или Base64
    public static void loadImage(Context context, String imageSource, ImageView imageView, int placeholderResId) {
        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        try {
            // Если это URL (начинается с http)
            if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
                Picasso.get()
                        .load(imageSource)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                Log.d(TAG, "Loading image from URL: " + imageSource);
            }
            // Если это Base64 строка
            else if (imageSource.startsWith("/9j/") || imageSource.length() > 1000) {
                Bitmap bitmap = base64ToBitmap(imageSource);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    Log.d(TAG, "Loading image from Base64 string");
                } else {
                    imageView.setImageResource(placeholderResId);
                }
            }
            // Если это путь к файлу
            else if (imageSource.startsWith("/storage") || imageSource.startsWith("file://")) {
                Picasso.get()
                        .load(new File(imageSource))
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                Log.d(TAG, "Loading image from file: " + imageSource);
            }
            // Если это URI
            else if (imageSource.startsWith("content://")) {
                Picasso.get()
                        .load(imageSource)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
                Log.d(TAG, "Loading image from URI: " + imageSource);
            }
            else {
                imageView.setImageResource(placeholderResId);
                Log.d(TAG, "Unknown image source format: " + imageSource);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            imageView.setImageResource(placeholderResId);
        }
    }

    // Создание уникального имени файла
    public static String generateUniqueFileName(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timeStamp + ".jpg";
    }

    // Сохранение Bitmap в файл
    public static String saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        File storageDir = context.getExternalFilesDir(null);
        if (storageDir == null) {
            return null;
        }

        File imageFile = new File(storageDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file: " + e.getMessage());
            return null;
        }
    }

    // Получение размера файла изображения
    public static String getImageFileSize(String filePath) {
        if (filePath == null) return "0 KB";
        File file = new File(filePath);
        if (file.exists()) {
            long sizeInBytes = file.length();
            long sizeInKB = sizeInBytes / 1024;
            long sizeInMB = sizeInKB / 1024;

            if (sizeInMB > 0) {
                return sizeInMB + " MB";
            } else {
                return sizeInKB + " KB";
            }
        }
        return "0 KB";
    }

    // Оптимизация Bitmap
    public static Bitmap optimizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return originalBitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }
}