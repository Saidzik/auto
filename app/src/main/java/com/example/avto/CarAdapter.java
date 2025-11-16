package com.example.avto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Car;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> implements Filterable {

    private List<Car> carList;
    private List<Car> carListFiltered;

    public CarAdapter(List<Car> carList) {
        this.carList = carList;
        this.carListFiltered = carList;
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
        Car car = carListFiltered.get(position);

        holder.tvCarName.setText(car.getFullName());
        holder.tvCarDetails.setText(String.format(Locale.getDefault(),
                "%d год, %d км", car.getYear(), car.getMileage()));
        holder.tvPrice.setText(String.format(Locale.getDefault(),
                "%.0f ₽", car.getPrice()));
        holder.tvStatus.setText(car.getStatus());
        holder.tvVin.setText("VIN: " + car.getVin());

        // Цвет статуса
        if ("В продаже".equals(car.getStatus())) {
            holder.tvStatus.setBackgroundColor(0xFF4CAF50); // Зеленый
            holder.tvStatus.setTextColor(0xFFFFFFFF);
        } else if ("Ожидание".equals(car.getStatus())) {
            holder.tvStatus.setBackgroundColor(0xFFFF9800); // Оранжевый
            holder.tvStatus.setTextColor(0xFFFFFFFF);
        } else {
            holder.tvStatus.setBackgroundColor(0xFFF44336); // Красный
            holder.tvStatus.setTextColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return carListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    carListFiltered = carList;
                } else {
                    List<Car> filteredList = new ArrayList<>();
                    for (Car car : carList) {
                        // Поиск по названию автомобиля, VIN, статусу
                        if (car.getFullName().toLowerCase().contains(charString.toLowerCase()) ||
                                car.getVin().toLowerCase().contains(charString.toLowerCase()) ||
                                car.getStatus().toLowerCase().contains(charString.toLowerCase()) ||
                                String.valueOf(car.getYear()).contains(charString) ||
                                String.format(Locale.getDefault(), "%.0f", car.getPrice()).contains(charString)) {
                            filteredList.add(car);
                        }
                    }
                    carListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = carListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                carListFiltered = (List<Car>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Метод для обновления данных
    public void updateData(List<Car> newCarList) {
        this.carList = newCarList;
        this.carListFiltered = newCarList;
        notifyDataSetChanged();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView tvCarName, tvCarDetails, tvPrice, tvStatus, tvVin;
        CardView cardView;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvCarDetails = itemView.findViewById(R.id.tvCarDetails);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvVin = itemView.findViewById(R.id.tvVin);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}