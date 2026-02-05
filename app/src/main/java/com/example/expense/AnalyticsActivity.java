package com.example.expense;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.Expense;
import com.example.expense.data.Income;
import com.example.expense.utils.FirebaseUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private PieChart pieChartCategories;
    private BarChart barChartMonthly;
    private BarChart barChartIncomeExpenses;
    private HorizontalBarChart horizontalBarChart;
    private TextView textViewTotalExpenses;
    private TextView textViewTotalIncome;
    private TextView textViewNetIncome;
    private TextView textViewAvgMonthly;
    private TextView textViewTopCategory;
    private ImageButton buttonBack;
    private Button buttonViewAllIncomes;
    private Button buttonViewAllExpenses;
    
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Income> allIncomes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        initViews();
        setupClickListeners();
        loadAndDisplayAnalytics();
    }

    private void initViews() {
        pieChartCategories = findViewById(R.id.pieChartCategories);
        barChartMonthly = findViewById(R.id.barChartMonthly);
        barChartIncomeExpenses = findViewById(R.id.barChartIncomeExpenses);
        horizontalBarChart = findViewById(R.id.horizontalBarChart);
        textViewTotalExpenses = findViewById(R.id.textViewTotalExpenses);
        textViewTotalIncome = findViewById(R.id.textViewTotalIncome);
        textViewNetIncome = findViewById(R.id.textViewNetIncome);
        textViewAvgMonthly = findViewById(R.id.textViewAvgMonthly);
        buttonBack = findViewById(R.id.buttonBack);
        buttonViewAllIncomes = findViewById(R.id.buttonViewAllIncomes);
        buttonViewAllExpenses = findViewById(R.id.buttonViewAllExpenses);
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
        
        buttonViewAllIncomes.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewIncomesActivity.class);
            startActivity(intent);
        });
        
        buttonViewAllExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewExpensesActivity.class);
            startActivity(intent);
        });
    }

    private void loadAndDisplayAnalytics() {
        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Load expenses
        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener((expenseSnapshot, expenseException) -> {
                if (expenseException != null) {
                    // Handle error
                    return;
                }

                if (expenseSnapshot != null) {
                    allExpenses.clear();
                    allExpenses.addAll(expenseSnapshot.toObjects(Expense.class));
                    displayAnalytics();
                }
            });
            
        // Load incomes
        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener((incomeSnapshot, incomeException) -> {
                if (incomeException != null) {
                    // Handle error
                    return;
                }

                if (incomeSnapshot != null) {
                    allIncomes.clear();
                    allIncomes.addAll(incomeSnapshot.toObjects(Income.class));
                    displayAnalytics();
                }
            });
    }

    private void displayAnalytics() {
        // Calculate total expenses
        double totalExpenses = 0.0;
        for (Expense expense : allExpenses) {
            totalExpenses += expense.getAmount();
        }
        textViewTotalExpenses.setText("$" + String.format("%.2f", totalExpenses));
        
        // Calculate total income
        double totalIncome = 0.0;
        for (Income income : allIncomes) {
            totalIncome += income.getAmount();
        }
        textViewTotalIncome.setText("$" + String.format("%.2f", totalIncome));
        
        // Calculate net income
        double netIncome = totalIncome - totalExpenses;
        textViewNetIncome.setText("$" + String.format("%.2f", netIncome));
        
        // Update text color based on positive/negative net income
        if (netIncome >= 0) {
            textViewNetIncome.setTextColor(Color.GREEN);
        } else {
            textViewNetIncome.setTextColor(Color.RED);
        }
        
        // Calculate average monthly expenses
        if (!allExpenses.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            long firstDate = allExpenses.get(0).getDate(); // Get last item as it's ordered DESC
            long lastDate = allExpenses.get(0).getDate();
            
            for (Expense expense : allExpenses) {
                if (expense.getDate() < firstDate) firstDate = expense.getDate();
                if (expense.getDate() > lastDate) lastDate = expense.getDate();
            }
            
            calendar.setTimeInMillis(firstDate);
            int startMonth = calendar.get(Calendar.MONTH) + calendar.get(Calendar.YEAR) * 12;
            
            calendar.setTimeInMillis(lastDate);
            int endMonth = calendar.get(Calendar.MONTH) + calendar.get(Calendar.YEAR) * 12;
            
            double months = (endMonth - startMonth + 1);
            double avgMonthly = (months > 0) ? totalExpenses / months : 0.0;
            textViewAvgMonthly.setText("$" + String.format("%.2f", avgMonthly));
        } else {
            textViewAvgMonthly.setText("$0.00");
        }

        // Prepare data for pie chart (categories)
        Map<String, Double> categoryMap = new HashMap<>();
        for (Expense expense : allExpenses) {
            Double currentAmount = categoryMap.get(expense.getCategory());
            if (currentAmount == null) {
                currentAmount = 0.0;
            }
            categoryMap.put(expense.getCategory(), currentAmount + expense.getAmount());
        }

        // Set up pie chart
        setupPieChart(categoryMap);

        // Prepare data for bar chart (monthly)
        Map<String, double[]> monthlyData = groupMonthlyData(allExpenses, allIncomes);
        setupBarChart(monthlyData);

        // Set up Income vs Expenses bar chart
        setupIncomeExpensesBarChart(totalIncome, totalExpenses);

        // Set up horizontal bar chart for top categories
        setupHorizontalBarChart(categoryMap);
    }

    private void setupPieChart(Map<String, Double> categoryMap) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(ColorTemplate.COLORFUL_COLORS[i % ColorTemplate.COLORFUL_COLORS.length]);
            i++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChartCategories.setData(data);
        pieChartCategories.getDescription().setText("");
        pieChartCategories.setUsePercentValues(true);
        pieChartCategories.getLegend().setEnabled(true);
        pieChartCategories.animateY(1000);
        pieChartCategories.invalidate();
    }

    private void setupBarChart(Map<String, double[]> monthlyData) {
        List<BarEntry> entries = new ArrayList<>();
        
        int index = 0;
        for (Map.Entry<String, double[]> entry : monthlyData.entrySet()) {
            // entry.getValue()[0] = income, entry.getValue()[1] = expense
            double netAmount = entry.getValue()[0] - entry.getValue()[1]; // income - expense
            entries.add(new BarEntry(index++, (float) netAmount));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Net Income");
        // Color based on whether it's positive or negative
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : entries) {
            if (entry.getY() >= 0) {
                colors.add(Color.GREEN);
            } else {
                colors.add(Color.RED);
            }
        }
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        
        barChartMonthly.setData(data);
        barChartMonthly.getDescription().setText("");
        barChartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthlyData.keySet().toArray(new String[0])) {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                String[] keys = monthlyData.keySet().toArray(new String[0]);
                if (index >= 0 && index < keys.length) {
                    return keys[index];
                }
                return "";
            }
        });
        barChartMonthly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartMonthly.animateY(1000);
        barChartMonthly.invalidate();
    }

    private Map<String, double[]> groupMonthlyData(List<Expense> expenses, List<Income> incomes) {
        Map<String, double[]> monthlyMap = new HashMap<>(); // Key: month-year, Value: [income, expense]
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        // Group expenses by month
        for (Expense expense : expenses) {
            String monthKey = dateFormat.format(new Date(expense.getDate()));
            double[] currentData = monthlyMap.get(monthKey);
            if (currentData == null) {
                currentData = new double[]{0.0, 0.0}; // [income, expense]
            }
            currentData[1] += expense.getAmount(); // Add to expense
            monthlyMap.put(monthKey, currentData);
        }

        // Group incomes by month
        for (Income income : incomes) {
            String monthKey = dateFormat.format(new Date(income.getDate()));
            double[] currentData = monthlyMap.get(monthKey);
            if (currentData == null) {
                currentData = new double[]{0.0, 0.0}; // [income, expense]
            }
            currentData[0] += income.getAmount(); // Add to income
            monthlyMap.put(monthKey, currentData);
        }

        return monthlyMap;
    }

    private void setupIncomeExpensesBarChart(double totalIncome, double totalExpenses) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalIncome));  // Income
        entries.add(new BarEntry(1f, (float) totalExpenses)); // Expenses

        BarDataSet dataSet = new BarDataSet(entries, "Income vs Expenses");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChartIncomeExpenses.setData(data);
        barChartIncomeExpenses.getDescription().setText("Total Income vs Total Expenses");
        barChartIncomeExpenses.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "Income";
                else if (value == 1) return "Expenses";
                else return "";
            }
        });
        barChartIncomeExpenses.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartIncomeExpenses.animateY(1000);
        barChartIncomeExpenses.invalidate();
    }

    private void setupHorizontalBarChart(Map<String, Double> categoryMap) {
        // Sort categories by amount to show top categories
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(categoryMap.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take only top 5 categories
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < Math.min(5, sortedEntries.size()); i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Top Categories");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        horizontalBarChart.setData(data);
        horizontalBarChart.getDescription().setText("Top 5 Spending Categories");
        horizontalBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        horizontalBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        horizontalBarChart.animateY(1000);
        horizontalBarChart.invalidate();

        // Make it horizontal
        horizontalBarChart.setFitBars(true);
    }
}