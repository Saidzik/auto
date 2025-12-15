package com.example.avto.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;

public class FirebaseImageUploader {
    private static final String TAG = "FirebaseUploader";
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public FirebaseImageUploader() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    // Загрузка файла в Firebase Storage
    public void uploadFile(Uri fileUri, String folderName, UploadCallback callback) {
        try {
            String fileName = new File(fileUri.getPath()).getName();
            StorageReference fileRef = storageRef.child(folderName + "/" + fileName);

            UploadTask uploadTask = fileRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "File uploaded successfully: " + downloadUrl);
                    callback.onSuccess(downloadUrl);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Upload failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                callback.onProgress(progress);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            callback.onError(e.getMessage());
        }
    }

    // Загрузка Bitmap в Firebase Storage
    public void uploadBitmap(byte[] imageData, String fileName, String folderName, UploadCallback callback) {
        try {
            StorageReference fileRef = storageRef.child(folderName + "/" + fileName);

            UploadTask uploadTask = fileRef.putBytes(imageData);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Bitmap uploaded successfully: " + downloadUrl);
                    callback.onSuccess(downloadUrl);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Upload failed: " + e.getMessage());
                callback.onError(e.getMessage());
            });

        } catch (Exception e) {
            Log.e(TAG, "Error uploading bitmap: " + e.getMessage());
            callback.onError(e.getMessage());
        }
    }

    // Интерфейс обратного вызова
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onProgress(double progress);
        void onError(String errorMessage);
    }

    // Генерация ссылки на изображение
    public static String generateImageUrl(String imageId) {
        return "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/cars%2F" +
                imageId + ".jpg?alt=media";
    }
}