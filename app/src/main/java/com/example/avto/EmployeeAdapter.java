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
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> implements Filterable {

    private List<Employee> employeeList;
    private List<Employee> employeeListFiltered;
    private DatabaseHelper databaseHelper;

    public EmployeeAdapter(List<Employee> employeeList, DatabaseHelper databaseHelper) {
        this.employeeList = employeeList;
        this.employeeListFiltered = employeeList;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employeeListFiltered.get(position);

        int salesCount = databaseHelper.getEmployeeSalesCount(employee.getId());
        double totalSales = databaseHelper.getEmployeeTotalSales(employee.getId());
        double averageSale = salesCount > 0 ? totalSales / salesCount : 0;

        holder.tvEmployeeName.setText(employee.getFullName());
        holder.tvPosition.setText(employee.getPosition());
        holder.tvDepartment.setText("Отдел: " + employee.getDepartment());
        holder.tvSalesCount.setText(String.format(Locale.getDefault(),
                "%d продаж", salesCount));
        holder.tvTotalSales.setText(String.format(Locale.getDefault(),
                "%.0f ₽", totalSales));
        holder.tvAverageSale.setText(String.format(Locale.getDefault(),
                "Средний чек: %.0f ₽", averageSale));
        holder.tvContact.setText(employee.getPhone() + " | " + employee.getEmail());

        // Цветовая индикация эффективности
        if (salesCount > 10) {
            holder.cardView.setCardBackgroundColor(0xFFE8F5E8); // Зеленый для успешных
        } else if (salesCount > 5) {
            holder.cardView.setCardBackgroundColor(0xFFFFF9C4); // Желтый для средних
        } else {
            holder.cardView.setCardBackgroundColor(0xFFFFEBEE); // Красный для новичков
        }
    }

    @Override
    public int getItemCount() {
        return employeeListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    employeeListFiltered = employeeList;
                } else {
                    List<Employee> filteredList = new ArrayList<>();
                    for (Employee employee : employeeList) {
                        if (employee.getFullName().toLowerCase().contains(charString.toLowerCase()) ||
                                employee.getPosition().toLowerCase().contains(charString.toLowerCase()) ||
                                employee.getDepartment().toLowerCase().contains(charString.toLowerCase()) ||
                                employee.getEmail().toLowerCase().contains(charString.toLowerCase()) ||
                                employee.getPhone().contains(charString)) {
                            filteredList.add(employee);
                        }
                    }
                    employeeListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = employeeListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                employeeListFiltered = (List<Employee>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Метод для обновления данных
    public void updateData(List<Employee> newEmployeeList) {
        this.employeeList = newEmployeeList;
        this.employeeListFiltered = newEmployeeList;
        notifyDataSetChanged();
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvPosition, tvDepartment, tvSalesCount, tvTotalSales, tvAverageSale, tvContact;
        CardView cardView;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvSalesCount = itemView.findViewById(R.id.tvSalesCount);
            tvTotalSales = itemView.findViewById(R.id.tvTotalSales);
            tvAverageSale = itemView.findViewById(R.id.tvAverageSale);
            tvContact = itemView.findViewById(R.id.tvContact);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}