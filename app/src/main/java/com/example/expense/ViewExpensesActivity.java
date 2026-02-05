package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.adapter.ExpenseAdapter;
import com.example.expense.data.Expense;
import com.example.expense.utils.FirebaseUtils;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ViewExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewExpenses;
    private SearchView searchView;
    private ImageButton buttonFilter;
    private ImageButton buttonSort;
    private ImageButton buttonBack;
    
    private ExpenseAdapter expenseAdapter;
    private List<Expense> allExpenses = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        initViews();
        setupRecyclerView();
        setupSearchAndFilter();
        setupClickListeners();
        loadExpenses();
    }

    private void initViews() {
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        searchView = findViewById(R.id.searchView);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonSort = findViewById(R.id.buttonSort);
        buttonBack = findViewById(R.id.buttonBack);
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), expense -> {
            Intent intent = new Intent(this, ExpenseDetailsActivity.class);
            intent.putExtra("expenseId", expense.getId());
            startActivity(intent);
        });
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void setupSearchAndFilter() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAndSortExpenses(query != null ? query : "", null);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndSortExpenses(newText != null ? newText : "", null);
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
                sortExpenses(selectedOption);
            });
        
        builder.show();
    }

    private void showFilterOptions() {
        // Create options for filtering
        String[] options = {"All Categories", "Food", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare", "Education", "Other"};
        
        boolean[] selectedOptions = new boolean[options.length];
        
        // Get unique categories from all expenses
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        for (Expense expense : allExpenses) {
            if (!categories.contains(expense.getCategory())) {
                categories.add(expense.getCategory());
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Filter By Category")
            .setItems(categories.toArray(new String[0]), (dialog, which) -> {
                String selectedCategory = categories.get(which);
                filterAndSortExpenses("", selectedCategory.equals("All Categories") ? null : selectedCategory);
            });
        
        builder.show();
    }

    private void sortExpenses(String sortOption) {
        List<Expense> sortedExpenses = new ArrayList<>(allExpenses);
        
        switch (sortOption) {
            case "Date (Newest First)":
                sortedExpenses.sort((e1, e2) -> Long.compare(e2.getDate(), e1.getDate()));
                break;
            case "Date (Oldest First)":
                sortedExpenses.sort((e1, e2) -> Long.compare(e1.getDate(), e2.getDate()));
                break;
            case "Amount (Highest First)":
                sortedExpenses.sort((e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
                break;
            case "Amount (Lowest First)":
                sortedExpenses.sort((e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
                break;
            case "Title (A-Z)":
                sortedExpenses.sort((e1, e2) -> e1.getTitle().compareToIgnoreCase(e2.getTitle()));
                break;
            case "Title (Z-A)":
                sortedExpenses.sort((e1, e2) -> e2.getTitle().compareToIgnoreCase(e1.getTitle()));
                break;
        }
        
        expenseAdapter = new ExpenseAdapter(sortedExpenses, expense -> {
            Intent intent = new Intent(ViewExpensesActivity.this, ExpenseDetailsActivity.class);
            intent.putExtra("expenseId", expense.getId());
            startActivity(intent);
        });
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void filterAndSortExpenses(String query, String category) {
        List<Expense> filteredExpenses;
        
        if (category == null || category.equals("")) {
            // Apply only search query
            if (query.isEmpty()) {
                filteredExpenses = new ArrayList<>(allExpenses);
            } else {
                filteredExpenses = new ArrayList<>();
                for (Expense expense : allExpenses) {
                    if (expense.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        expense.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        expense.getNotes().toLowerCase().contains(query.toLowerCase()) ||
                        String.valueOf(expense.getAmount()).contains(query)) {
                        filteredExpenses.add(expense);
                    }
                }
            }
        } else {
            // Apply both category filter and search query
            if (query.isEmpty()) {
                filteredExpenses = new ArrayList<>();
                for (Expense expense : allExpenses) {
                    if (expense.getCategory().equals(category)) {
                        filteredExpenses.add(expense);
                    }
                }
            } else {
                filteredExpenses = new ArrayList<>();
                for (Expense expense : allExpenses) {
                    if (expense.getCategory().equals(category) && 
                        (expense.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                         expense.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                         expense.getNotes().toLowerCase().contains(query.toLowerCase()) ||
                         String.valueOf(expense.getAmount()).contains(query))) {
                        filteredExpenses.add(expense);
                    }
                }
            }
        }

        // Apply default sorting (by date, newest first)
        filteredExpenses.sort((e1, e2) -> Long.compare(e2.getDate(), e1.getDate()));

        expenseAdapter = new ExpenseAdapter(filteredExpenses, expense -> {
            Intent intent = new Intent(ViewExpensesActivity.this, ExpenseDetailsActivity.class);
            intent.putExtra("expenseId", expense.getId());
            startActivity(intent);
        });
        recyclerViewExpenses.setAdapter(expenseAdapter);
    }

    private void loadExpenses() {
        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, exception) -> {
                if (exception != null) {
                    // Handle error
                    return;
                }

                if (snapshot != null) {
                    allExpenses.clear();
                    allExpenses.addAll(snapshot.toObjects(Expense.class));
                    expenseAdapter = new ExpenseAdapter(new ArrayList<>(allExpenses), expense -> {
                        Intent intent = new Intent(ViewExpensesActivity.this, ExpenseDetailsActivity.class);
                        intent.putExtra("expenseId", expense.getId());
                        startActivity(intent);
                    });
                    recyclerViewExpenses.setAdapter(expenseAdapter);
                }
            });
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Reload expenses when returning to this activity to ensure fresh data
        loadExpenses();
    }
}