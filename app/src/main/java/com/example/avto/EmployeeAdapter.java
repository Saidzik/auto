package com.example.avto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private List<Employee> employeeList;
    private DatabaseHelper databaseHelper;
    private OnEmployeeClickListener listener;

    public EmployeeAdapter(List<Employee> employeeList, DatabaseHelper databaseHelper) {
        this.employeeList = employeeList != null ? employeeList : new ArrayList<>();
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
        Employee employee = employeeList.get(position);

        // Рассчитываем статистику продаж
        int salesCount = databaseHelper.getEmployeeSalesCount(employee.getId());
        double totalSales = databaseHelper.getEmployeeTotalSales(employee.getId());
        double averageSale = salesCount > 0 ? totalSales / salesCount : 0;

        // Устанавливаем данные
        holder.tvEmployeeName.setText(employee.getFullName());
        holder.tvPosition.setText("Должность: " + employee.getPosition());

        // Проверяем существование полей перед установкой
        if (holder.tvDepartment != null) {
            holder.tvDepartment.setText("Отдел: " +
                    (employee.getDepartment() != null ? employee.getDepartment() : "Не указан"));
        }

        if (holder.tvSalesCount != null) {
            holder.tvSalesCount.setText("Продаж: " + salesCount);
        }

        if (holder.tvTotalSales != null) {
            holder.tvTotalSales.setText("Выручка: " + String.format(Locale.getDefault(), "%.0f ₽", totalSales));
        }

        if (holder.tvAverageSale != null) {
            holder.tvAverageSale.setText("Средний чек: " + String.format(Locale.getDefault(), "%.0f ₽", averageSale));
        }

        if (holder.tvContact != null) {
            String contact = "";
            if (employee.getPhone() != null && !employee.getPhone().isEmpty()) {
                contact += employee.getPhone();
            }
            if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
                if (!contact.isEmpty()) contact += " | ";
                contact += employee.getEmail();
            }
            holder.tvContact.setText(contact);
        }

        // Устанавливаем обработчики для кнопок
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(employee);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(employee);
            }
        });

        holder.btnChangePosition.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangePositionClick(employee);
            }
        });

        // Цветовая индикация эффективности
        if (holder.cardView != null) {
            if (salesCount > 10) {
                holder.cardView.setCardBackgroundColor(0xFFE8F5E8); // Зеленый
            } else if (salesCount > 5) {
                holder.cardView.setCardBackgroundColor(0xFFFFF9C4); // Желтый
            } else {
                holder.cardView.setCardBackgroundColor(0xFFFFEBEE); // Красный
            }
        }
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public void updateData(List<Employee> newEmployeeList) {
        this.employeeList = newEmployeeList != null ? newEmployeeList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnEmployeeClickListener {
        void onEditClick(Employee employee);
        void onDeleteClick(Employee employee);
        void onChangePositionClick(Employee employee);
    }

    public void setOnEmployeeClickListener(OnEmployeeClickListener listener) {
        this.listener = listener;
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvPosition, tvDepartment, tvSalesCount, tvTotalSales, tvAverageSale, tvContact;
        Button btnEdit, btnDelete, btnChangePosition;
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

            // Инициализируем кнопки
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnChangePosition = itemView.findViewById(R.id.btnChangePosition);

            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}