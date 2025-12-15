package com.example.avto.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avto.R;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Car;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> implements Filterable {

    private List<Car> carList;
    private List<Car> carListFull; // Для фильтрации
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Car car);
        void onEditClick(Car car);
        void onDeleteClick(Car car);
    }

    public CarAdapter(Context context, List<Car> carList, OnItemClickListener listener) {
        this.context = context;
        this.carList = new ArrayList<>(carList);
        this.carListFull = new ArrayList<>(carList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car_manage, parent, false); // Используем новый layout с кнопками
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        // Устанавливаем данные
        holder.tvBrandModel.setText(car.getBrand() + " " + car.getModel());
        holder.tvYear.setText("Год: " + car.getYear());
        holder.tvMileage.setText("Пробег: " + String.format(Locale.getDefault(), "%,d км", car.getMileage()));
        holder.tvPrice.setText("Цена: " + String.format(Locale.getDefault(), "%,.0f ₽", car.getPrice()));
        holder.tvStatus.setText("Статус: " + car.getStatus());

        if (car.getColor() != null && !car.getColor().isEmpty()) {
            holder.tvColor.setText("Цвет: " + car.getColor());
        } else {
            holder.tvColor.setText("Цвет: не указан");
        }

        // Загрузка фото
        String imageUrl = car.getImageUrl();
        Log.d("CarAdapter", "Загрузка фото для " + car.getBrand() + " " + car.getModel() +
                ": " + (imageUrl != null ? imageUrl : "нет фото"));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Используем Picasso для загрузки изображения
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .resize(400, 300)
                        .centerCrop()
                        .into(holder.ivCarImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("CarAdapter", "Фото успешно загружено: " + imageUrl);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("CarAdapter", "Ошибка загрузки фото: " + imageUrl, e);
                                holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
                            }
                        });
            } catch (Exception e) {
                Log.e("CarAdapter", "Ошибка Picasso: " + e.getMessage());
                holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        } else {
            holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        // Устанавливаем обработчики для кнопок
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(car);
            }
        });

        holder.btnEditCar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(car);
            }
        });

        holder.btnDeleteCar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(car);
            }
        });

        // Цвет статуса
        String status = car.getStatus();
        if ("В продаже".equals(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Продан".equals(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateData(List<Car> newCars) {
        this.carList.clear();
        this.carList.addAll(newCars);

        this.carListFull.clear();
        this.carListFull.addAll(newCars);

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return carFilter;
    }

    private Filter carFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Car> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(carListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Car car : carListFull) {
                    if (car.getBrand() != null && car.getBrand().toLowerCase().contains(filterPattern) ||
                            car.getModel() != null && car.getModel().toLowerCase().contains(filterPattern) ||
                            car.getVin() != null && car.getVin().toLowerCase().contains(filterPattern)) {
                        filteredList.add(car);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            carList.clear();
            carList.addAll((List<Car>) results.values);
            notifyDataSetChanged();
        }
    };

    // Вспомогательные методы
    private int findCarPositionById(String carId) {
        for (int i = 0; i < carList.size(); i++) {
            if (carList.get(i).getId().equals(carId)) {
                return i;
            }
        }
        return -1;
    }

    private void removeFromFullListById(String carId) {
        for (int i = 0; i < carListFull.size(); i++) {
            if (carListFull.get(i).getId().equals(carId)) {
                carListFull.remove(i);
                break;
            }
        }
    }

    public class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvBrandModel, tvYear, tvMileage, tvPrice, tvStatus, tvColor;
        Button btnViewDetails, btnEditCar, btnDeleteCar;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            // Инициализация ImageView
            ivCarImage = itemView.findViewById(R.id.ivCarImage);

            // Инициализация TextView
            tvBrandModel = itemView.findViewById(R.id.tvCarBrandModel);
            tvYear = itemView.findViewById(R.id.tvCarYear);
            tvMileage = itemView.findViewById(R.id.tvCarMileage);
            tvPrice = itemView.findViewById(R.id.tvCarPrice);
            tvStatus = itemView.findViewById(R.id.tvCarStatus);
            tvColor = itemView.findViewById(R.id.tvCarColor);

            // Инициализация кнопок - ДОБАВЬТЕ ЭТИ СТРОКИ!
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEditCar = itemView.findViewById(R.id.btnEditCar);
            btnDeleteCar = itemView.findViewById(R.id.btnDeleteCar);

            // Проверка (для отладки)
            if (btnViewDetails == null) {
                Log.e("CarViewHolder", "btnViewDetails не найден!");
            }
            if (btnEditCar == null) {
                Log.e("CarViewHolder", "btnEditCar не найден!");
            }
            if (btnDeleteCar == null) {
                Log.e("CarViewHolder", "btnDeleteCar не найден!");
            }
        }
    }
}