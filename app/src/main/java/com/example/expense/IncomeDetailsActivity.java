package com.example.expense;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.Income;
import com.example.expense.utils.FirebaseUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IncomeDetailsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private TextView textViewIncomeTitle;
    private TextView textViewIncomeAmount;
    private TextView textViewIncomeDate;
    private TextView textViewIncomeCategory;
    private TextView textViewIncomeNotes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_details);

        initViews();
        setupClickListeners();
        loadIncomeDetails();
    }

    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        textViewIncomeTitle = findViewById(R.id.textViewIncomeTitle);
        textViewIncomeAmount = findViewById(R.id.textViewIncomeAmount);
        textViewIncomeDate = findViewById(R.id.textViewIncomeDate);
        textViewIncomeCategory = findViewById(R.id.textViewIncomeCategory);
        textViewIncomeNotes = findViewById(R.id.textViewIncomeNotes);
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private void loadIncomeDetails() {
        String incomeId = getIntent().getStringExtra("incomeId");
        if (incomeId == null) return;
        
        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .document(incomeId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    Income income = document.toObject(Income.class);
                    if (income != null && userId.equals(income.getUserId())) {
                        displayIncomeDetails(income);
                    }
                }
            })
            .addOnFailureListener(exception -> {
                // Handle error
            });
    }

    private void displayIncomeDetails(Income income) {
        textViewIncomeTitle.setText(income.getTitle());
        textViewIncomeAmount.setText("+$" + String.format("%.2f", income.getAmount()));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        textViewIncomeDate.setText(dateFormat.format(new Date(income.getDate())));
        
        textViewIncomeCategory.setText(income.getCategory());
        textViewIncomeNotes.setText(income.getNotes().isEmpty() ? "No notes" : income.getNotes());
    }
}