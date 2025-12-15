package com.example.avto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Car;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientCarAdapter extends RecyclerView.Adapter<ClientCarAdapter.ClientCarViewHolder> implements Filterable {

    private Context context;
    private List<Car> carList;
    private List<Car> carListFull; // Для фильтрации
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Car car);
    }

    public ClientCarAdapter(Context context, List<Car> carList, OnItemClickListener listener) {
        this.context = context;
        this.carList = new ArrayList<>(carList);
        this.carListFull = new ArrayList<>(carList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car_client, parent, false);
        return new ClientCarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientCarViewHolder holder, int position) {
        Car car = carList.get(position);

        // Устанавливаем данные
        holder.tvBrandModel.setText(car.getBrand() + " " + car.getModel());
        holder.tvYear.setText("Год: " + car.getYear());
        holder.tvMileage.setText("Пробег: " + String.format(Locale.getDefault(), "%,d км", car.getMileage()));
        holder.tvPrice.setText("Цена: " + String.format(Locale.getDefault(), "%,.0f ₽", car.getPrice()));

        if (car.getColor() != null && !car.getColor().isEmpty()) {
            holder.tvColor.setText("Цвет: " + car.getColor());
        } else {
            holder.tvColor.setText("Цвет: не указан");
        }

        // Загрузка фото
        String imageUrl = car.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .resize(300, 200)
                    .centerCrop()
                    .into(holder.ivCarImage);
        } else {
            holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        // Клик на всю карточку
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
                            car.getColor() != null && car.getColor().toLowerCase().contains(filterPattern)) {
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

    public static class ClientCarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvBrandModel, tvYear, tvMileage, tvPrice, tvColor;

        public ClientCarViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvBrandModel = itemView.findViewById(R.id.tvCarBrandModel);
            tvYear = itemView.findViewById(R.id.tvCarYear);
            tvMileage = itemView.findViewById(R.id.tvCarMileage);
            tvPrice = itemView.findViewById(R.id.tvCarPrice);
            tvColor = itemView.findViewById(R.id.tvCarColor);
        }
    }
}