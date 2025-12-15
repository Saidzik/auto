package com.example.avto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.R;
import com.example.avto.models.Client;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clientList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Client client);
    }

    public ClientAdapter(List<Client> clientList, OnItemClickListener listener) {
        this.clientList = clientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clientList.get(position);

        // Исправьте здесь:
        holder.tvClientName.setText(client.getFirstName() + " " + client.getLastName());
        holder.tvClientEmail.setText(client.getEmail());
        holder.tvClientPhone.setText(client.getPhone());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvClientEmail, tvClientPhone;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientEmail = itemView.findViewById(R.id.tvClientEmail);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
        }
    }
}