package com.example.avto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Deal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private List<Deal> dealList;
    private SimpleDateFormat dateFormat;

    public DealAdapter(List<Deal> dealList) {
        this.dealList = dealList;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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

        holder.tvCarName.setText(deal.getCarName());
        holder.tvDate.setText(dateFormat.format(deal.getDate()));
        holder.tvAmount.setText(String.format(Locale.getDefault(),
                "%.0f ₽", deal.getAmount()));
        holder.tvClient.setText("Клиент: " + deal.getClientName());
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    public static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView tvCarName, tvDate, tvAmount, tvClient;
        CardView cardView;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvClient = itemView.findViewById(R.id.tvClient);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
