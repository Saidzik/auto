package com.example.avto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Deal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private List<Deal> dealList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Deal deal);
    }

    public DealAdapter(List<Deal> dealList, OnItemClickListener listener) {
        this.dealList = dealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deal, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        Deal deal = dealList.get(position);

        holder.tvDealId.setText("Сделка #" + deal.getId());
        holder.tvClientName.setText("Клиент: " + deal.getClientName());
        holder.tvCarInfo.setText("Авто: " + deal.getCarName());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%,.0f ₽", deal.getAmount()));

        // Форматируем дату
        if (deal.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            holder.tvDate.setText("Дата: " + sdf.format(deal.getDate()));
        } else {
            holder.tvDate.setText("Дата не указана");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(deal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    public void updateData(List<Deal> newDealList) {
        this.dealList = newDealList;
        notifyDataSetChanged();
    }

    static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView tvDealId, tvClientName, tvCarInfo, tvAmount, tvDate;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDealId = itemView.findViewById(R.id.tvDealId);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvCarInfo = itemView.findViewById(R.id.tvCarInfo);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}