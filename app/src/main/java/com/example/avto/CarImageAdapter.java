package com.example.avto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Car;
import com.example.avto.utils.ImageManager;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Locale;

public class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.CarViewHolder> {

    private List<Car> carList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Car car);
    }

    public CarImageAdapter(List<Car> carList, Context context, OnItemClickListener listener) {
        this.carList = carList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        // Установка данных
        holder.tvCarName.setText(car.getBrand() + " " + car.getModel());
        holder.tvCarDetails.setText(String.format(Locale.getDefault(),
                "%d год, %,d км", car.getYear(), car.getMileage()));
        holder.tvPrice.setText(String.format(Locale.getDefault(),
                "%,.0f ₽", car.getPrice()));
        holder.tvStatus.setText(car.getStatus());
        holder.tvVin.setText("VIN: " + car.getVin());

        // Загрузка изображения
        if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
            ImageManager.loadImage(context, car.getImageUrl(), holder.ivCarImage,
                    R.drawable.ic_car_placeholder);

            // Или через Picasso напрямую
            /*
            Picasso.get()
                .load(car.getImageUrl())
                .placeholder(R.drawable.ic_car_placeholder)
                .error(R.drawable.ic_car_placeholder)
                .into(holder.ivCarImage);
            */
        } else {
            holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        // Обработчик клика
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(car);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateData(List<Car> newCarList) {
        this.carList = newCarList;
        notifyDataSetChanged();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvCarName, tvCarDetails, tvPrice, tvStatus, tvVin;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvCarName = itemView.findViewById(R.id.tvCarName);

            tvPrice = itemView.findViewById(R.id.tvCarPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);

        }
    }
}