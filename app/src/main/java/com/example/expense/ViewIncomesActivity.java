package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.adapter.IncomeAdapter;
import com.example.expense.data.Income;
import com.example.expense.utils.FirebaseUtils;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ViewIncomesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewIncomes;
    private SearchView searchView;
    private ImageButton buttonFilter;
    private ImageButton buttonSort;
    private ImageButton buttonBack;
    private IncomeAdapter incomeAdapter;
    private List<Income> allIncomes = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_incomes);

        initViews();
        setupRecyclerView();
        setupSearchAndFilter();
        setupClickListeners();
        loadIncomes();
    }

    private void initViews() {
        recyclerViewIncomes = findViewById(R.id.recyclerViewIncomes);
        searchView = findViewById(R.id.searchView);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonSort = findViewById(R.id.buttonSort);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupRecyclerView () {
        recyclerViewIncomes.setLayoutManager(new LinearLayoutManager(this));
        incomeAdapter = new IncomeAdapter(new ArrayList<>(), income -> {
            // Handle income item click
            Intent intent = new Intent(this, IncomeDetailsActivity.class);
            intent.putExtra("incomeId", income.getId());
            startActivity(intent);
        });
        recyclerViewIncomes.setAdapter(incomeAdapter);
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void setupSearchAndFilter() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAndSortIncomes(query != null ? query : "", null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndSortIncomes(newText != null ? newText : "", null);
                return true;
            }
        });

        buttonSort.setOnClickListener(v -> showSortOptions());

        buttonFilter.setOnClickListener(v -> showFilterOptions());
    }

    private void showSortOptions() {
        // Create options for sorting
        String[] options = {"Date (Newest First)", "Date (Oldest First)", "Amount (Highest First)", "Amount (Lowest First)", "Title (A-Z)", "Title (Z-A)"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Sort By")
            .setItems(options, (dialog, which) -> {
                String selectedOption = options[which];
                sortIncomes(selectedOption);
            });
        
        builder.show();
    }

    private void showFilterOptions() {
        // Create options for filtering based on income categories
        String[] options = {"All Categories", "Salary", "Freelance", "Business", "Investment", "Rental Income", "Consulting", "Part-time Job", "Other"};
        
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        for (Income income : allIncomes) {
            if (!categories.contains(income.getCategory())) {
                categories.add(income.getCategory());
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Filter By Category")
            .setItems(categories.toArray(new String[0]), (dialog, which) -> {
                String selectedCategory = categories.get(which);
                filterAndSortIncomes("", selectedCategory.equals("All Categories") ? null : selectedCategory);
            });
        
        builder.show();
    }

    private void sortIncomes(String sortOption) {
        List<Income> sortedIncomes = new ArrayList<>(allIncomes);
        
        switch (sortOption) {
            case "Date (Newest First)":
                sortedIncomes.sort((i1, i2) -> Long.compare(i2.getDate(), i1.getDate()));
                break;
            case "Date (Oldest First)":
                sortedIncomes.sort((i1, i2) -> Long.compare(i1.getDate(), i2.getDate()));
                break;
            case "Amount (Highest First)":
                sortedIncomes.sort((i1, i2) -> Double.compare(i2.getAmount(), i1.getAmount()));
                break;
            case "Amount (Lowest First)":
                sortedIncomes.sort((i1, i2) -> Double.compare(i1.getAmount(), i2.getAmount()));
                break;
            case "Title (A-Z)":
                sortedIncomes.sort((i1, i2) -> i1.getTitle().compareToIgnoreCase(i2.getTitle()));
                break;
            case "Title (Z-A)":
                sortedIncomes.sort((i1, i2) -> i2.getTitle().compareToIgnoreCase(i1.getTitle()));
                break;
        }
        
        incomeAdapter = new IncomeAdapter(sortedIncomes, income -> {
            Intent intent = new Intent(ViewIncomesActivity.this, IncomeDetailsActivity.class);
            intent.putExtra("incomeId", income.getId());
            startActivity(intent);
        });
        recyclerViewIncomes.setAdapter(incomeAdapter);
    }

    private void filterAndSortIncomes(String query, String category) {
        List<Income> filteredIncomes;
        
        if (category == null || category.equals("")) {
            // Apply only search query
            if (query.isEmpty()) {
                filteredIncomes = new ArrayList<>(allIncomes);
            } else {
                filteredIncomes = new ArrayList<>();
                for (Income income : allIncomes) {
                    if (income.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        income.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        income.getNotes().toLowerCase().contains(query.toLowerCase()) ||
                        String.valueOf(income.getAmount()).contains(query)) {
                        filteredIncomes.add(income);
                    }
                }
            }
        } else {
            // Apply both category filter and search query
            if (query.isEmpty()) {
                filteredIncomes = new ArrayList<>();
                for (Income income : allIncomes) {
                    if (income.getCategory().equals(category)) {
                        filteredIncomes.add(income);
                    }
                }
            } else {
                filteredIncomes = new ArrayList<>();
                for (Income income : allIncomes) {
                    if (income.getCategory().equals(category) && 
                        (income.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                         income.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                         income.getNotes().toLowerCase().contains(query.toLowerCase()) ||
                         String.valueOf(income.getAmount()).contains(query))) {
                        filteredIncomes.add(income);
                    }
                }
            }
        }

        // Apply default sorting (by date, newest first)
        filteredIncomes.sort((i1, i2) -> Long.compare(i2.getDate(), i1.getDate()));

        incomeAdapter = new IncomeAdapter(filteredIncomes, income -> {
            Intent intent = new Intent(ViewIncomesActivity.this, IncomeDetailsActivity.class);
            intent.putExtra("incomeId", income.getId());
            startActivity(intent);
        });
        recyclerViewIncomes.setAdapter(incomeAdapter);
    }

    private void loadIncomes() {
        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, exception) -> {
                if (exception != null) {
                    // Handle error
                    return;
                }

                if (snapshot != null) {
                    allIncomes.clear();
                    allIncomes.addAll(snapshot.toObjects(Income.class));
                    incomeAdapter = new IncomeAdapter(new ArrayList<>(allIncomes), income -> {
                        Intent intent = new Intent(ViewIncomesActivity.this, IncomeDetailsActivity.class);
                        intent.putExtra("incomeId", income.getId());
                        startActivity(intent);
                    });
                    recyclerViewIncomes.setAdapter(incomeAdapter);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload incomes when returning to this activity to ensure fresh data
        loadIncomes();
    }
}