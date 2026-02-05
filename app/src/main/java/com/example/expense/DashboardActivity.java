package com.example.expense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.adapter.CategoryAdapter;
import com.example.expense.adapter.ExpenseAdapter;
import com.example.expense.data.CategorySummary;
import com.example.expense.data.Expense;
import com.example.expense.data.Income;
import com.example.expense.utils.FirebaseUtils;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView textViewTotalExpenses;
    private TextView textViewTotalIncomes;
    private TextView textViewNetAmount;
    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewRecentExpenses;
    private TextView textViewViewAll;
    private Button buttonAddExpense;
    private Button buttonAddIncome;
    private ImageButton buttonProfile;

    private CategoryAdapter categoryAdapter;
    private ExpenseAdapter expenseAdapter;

    private double totalExpenses = 0.0;
    private double totalIncomes = 0.0;
    private List<Expense> expenses = new ArrayList<>();
    private List<Income> incomes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        loadDashboardData();
    }

    private void initViews() {
        textViewTotalExpenses = findViewById(R.id.textViewTotalExpenses);
        textViewTotalIncomes = findViewById(R.id.textViewTotalIncomes);
        textViewNetAmount = findViewById(R.id.textViewNetAmount);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewRecentExpenses = findViewById(R.id.recyclerViewRecentExpenses);
        textViewViewAll = findViewById(R.id.textViewViewAll);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddIncome = findViewById(R.id.buttonAddIncome);
        buttonProfile = findViewById(R.id.buttonProfile);
    }

    private void setupRecyclerViews() {
        // Setup categories recycler view
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Setup recent expenses recycler view
        recyclerViewRecentExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), expense -> {
            // Handle expense item click
            Intent intent = new Intent(this, ExpenseDetailsActivity.class);
            intent.putExtra("expenseId", expense.getId());
            startActivity(intent);
        });
        recyclerViewRecentExpenses.setAdapter(expenseAdapter);
    }

    private void setupClickListeners() {
        buttonAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        buttonAddIncome.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddIncomeActivity.class);
            startActivity(intent);
        });

        textViewViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewExpensesActivity.class);
            startActivity(intent);
        });

        buttonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Add click listener for analytics access - changed to net amount to make more sense
        textViewNetAmount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalyticsActivity.class);
            startActivity(intent);
        });
    }

    private void loadDashboardData() {
        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ?
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Load recent expenses (just for display, not for calculations)
        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener((expenseSnapshot, expenseException) -> {
                if (expenseException != null) {
                    // Handle error
                    return;
                }

                if (expenseSnapshot != null) {
                    expenses.clear();
                    expenses.addAll(expenseSnapshot.toObjects(Expense.class));
                    updateDashboard();
                }
            });

        // Load recent incomes (just for display, not for calculations)
        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener((incomeSnapshot, incomeException) -> {
                if (incomeException != null) {
                    // Handle error
                    return;
                }

                if (incomeSnapshot != null) {
                    incomes.clear();
                    incomes.addAll(incomeSnapshot.toObjects(Income.class));
                    updateDashboard();
                }
            });

        // Load ALL expenses for total calculation
        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener((expenseSnapshot, expenseException) -> {
                if (expenseException != null) {
                    // Handle error
                    return;
                }

                if (expenseSnapshot != null) {
                    List<Expense> allExpenses = expenseSnapshot.toObjects(Expense.class);
                    totalExpenses = 0.0;
                    for (Expense expense : allExpenses) {
                        totalExpenses += expense.getAmount();
                    }

                    updateDashboard();
                }
            });

        // Load ALL incomes for total calculation
        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener((incomeSnapshot, incomeException) -> {
                if (incomeException != null) {
                    // Handle error
                    return;
                }

                if (incomeSnapshot != null) {
                    List<Income> allIncomes = incomeSnapshot.toObjects(Income.class);
                    totalIncomes = 0.0;
                    for (Income income : allIncomes) {
                        totalIncomes += income.getAmount();
                    }

                    updateDashboard();
                }
            });
    }

    private void updateDashboard() {
        // Update total incomes display
        textViewTotalIncomes.setText("$" + String.format("%.2f", totalIncomes));
        
        // Update total expenses display
        textViewTotalExpenses.setText("$" + String.format("%.2f", totalExpenses));
        
        // Update net amount display
        double netAmount = totalIncomes - totalExpenses;
        String netText;
        int netColor;
        
        if (netAmount >= 0) {
            netText = "+$" + String.format("%.2f", netAmount);
            netColor = ContextCompat.getColor(this, R.color.teal_200); // Positive in teal/green
        } else {
            netText = "-$" + String.format("%.2f", Math.abs(netAmount));
            netColor = ContextCompat.getColor(this, R.color.purple_500); // Negative in purple/red
        }
        
        textViewNetAmount.setText(netText);
        textViewNetAmount.setTextColor(netColor);

        // Update the recycler view with recent expenses
        if (expenseAdapter != null) {
            expenseAdapter.updateExpenses(expenses);
        } else {
            expenseAdapter = new ExpenseAdapter(expenses, expense -> {
                Intent intent = new Intent(this, ExpenseDetailsActivity.class);
                intent.putExtra("expenseId", expense.getId());
                startActivity(intent);
            });
            recyclerViewRecentExpenses.setAdapter(expenseAdapter);
        }

        // Load category summaries based on recent expenses
        loadCategorySummaries(expenses);
    }

    private void loadCategorySummaries(List<Expense> expenses) {
        Map<String, Double> categoryMap = new HashMap<>();

        for (Expense expense : expenses) {
            Double currentAmount = categoryMap.get(expense.getCategory());
            if (currentAmount == null) {
                currentAmount = 0.0;
            }
            categoryMap.put(expense.getCategory(), currentAmount + expense.getAmount());
        }

        List<CategorySummary> categories = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            categories.add(new CategorySummary(entry.getKey(), entry.getValue()));
        }

        if (categoryAdapter != null) {
            categoryAdapter.updateCategories(categories);
        } else {
            categoryAdapter = new CategoryAdapter(categories);
            recyclerViewCategories.setAdapter(categoryAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload dashboard data when returning to this activity to ensure fresh data
        loadDashboardData();
    }
}