package com.example.avto;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avto.adapters.DealAdapter;
import com.example.avto.database.DatabaseHelper;
import com.example.avto.models.Deal;
import java.util.ArrayList;
import java.util.List;

public class DealsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDeals;
    private DealAdapter dealAdapter;
    private DatabaseHelper databaseHelper;
    private List<Deal> dealList;

    private static final String TAG = "DealsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        loadDealsFromDatabase();

        Log.d(TAG, "DealsActivity created");
    }

    private void initViews() {
        recyclerViewDeals = findViewById(R.id.recyclerViewDeals);
        Log.d(TAG, "Views initialized");
    }

    private void setupRecyclerView() {
        dealList = new ArrayList<>();

        // Создаем слушатель для DealAdapter
        DealAdapter.OnItemClickListener listener = new DealAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Deal deal) {
                // Обработка клика по сделке
                openDealDetails(deal);
            }
        };

        dealAdapter = new DealAdapter(dealList, listener);
        recyclerViewDeals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDeals.setAdapter(dealAdapter);
        Log.d(TAG, "RecyclerView setup completed");
    }

    private void openDealDetails(Deal deal) {
        // Открываем детали сделки
        Toast.makeText(this, "Сделка #" + deal.getId(), Toast.LENGTH_SHORT).show();
        // В будущем: Intent intent = new Intent(this, DealDetailActivity.class);
    }

    private void loadDealsFromDatabase() {
        Log.d(TAG, "Loading deals from database...");

        new Thread(() -> {
            try {
                List<Deal> deals = databaseHelper.getAllDeals();

                runOnUiThread(() -> {
                    Log.d(TAG, "Received " + deals.size() + " deals from database");
                    dealAdapter.updateData(deals);
                    Log.d(TAG, "Displaying " + deals.size() + " deals");
                    Log.d(TAG, "Deals list updated successfully");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading deals: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка загрузки сделок", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deals, menu);

        // Настройка поиска
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Реализация поиска сделок
                    filterDeals(newText);
                    return true;
                }
            });
        }

        Log.d(TAG, "SearchView setup completed");
        return true;
    }

    private void filterDeals(String query) {
        List<Deal> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(dealList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Deal deal : dealList) {
                if (deal.getClientName().toLowerCase().contains(lowerCaseQuery) ||
                        deal.getCarName().toLowerCase().contains(lowerCaseQuery) ||
                        String.valueOf(deal.getId()).contains(query)) {
                    filteredList.add(deal);
                }
            }
        }

        dealAdapter.updateData(filteredList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_add_deal) {
            // Добавление новой сделки
            Intent intent = new Intent(this, AddDealActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - refreshing data");
        loadDealsFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        Log.d(TAG, "DealsActivity destroyed");
    }
}