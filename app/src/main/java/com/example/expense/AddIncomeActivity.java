package com.example.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.Income;
import com.example.expense.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {

    private TextInputEditText incomeTitleEditText;
    private TextInputEditText incomeAmountEditText;
    private TextView incomeDateTextView;
    private Spinner categorySpinner;
    private TextInputEditText incomeNotesEditText;
    private Button saveIncomeButton;
    private Button cancelButton;
    private ImageButton backButton;
    
    private long selectedDate = System.currentTimeMillis();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        initViews();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initViews() {
        incomeTitleEditText = findViewById(R.id.editTextIncomeTitle);
        incomeAmountEditText = findViewById(R.id.editTextIncomeAmount);
        incomeDateTextView = findViewById(R.id.textViewIncomeDate);
        categorySpinner = findViewById(R.id.spinnerIncomeCategory);
        incomeNotesEditText = findViewById(R.id.editTextIncomeNotes);
        saveIncomeButton = findViewById(R.id.buttonSaveIncome);
        cancelButton = findViewById(R.id.buttonCancel);
        backButton = findViewById(R.id.buttonBack);
    }

    private void setupCategorySpinner() {
        String[] categories = {
            "Salary", "Freelance", "Business", "Investment", 
            "Rental Income", "Consulting", "Part-time Job", "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        incomeDateTextView.setOnClickListener(v -> showDatePicker());

        saveIncomeButton.setOnClickListener(v -> saveIncome());

        cancelButton.setOnClickListener(v -> finish());
        
        backButton.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                calendar.set(selectedYear, selectedMonth, selectedDay);
                selectedDate = calendar.getTimeInMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                incomeDateTextView.setText(dateFormat.format(calendar.getTime()));
            },
            year, month, day
        );

        datePickerDialog.show();
    }

    private void saveIncome() {
        String title = incomeTitleEditText.getText().toString().trim();
        String amountStr = incomeAmountEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String notes = incomeNotesEditText.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill title and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseUtils.getAuth().getCurrentUser() != null ? 
            FirebaseUtils.getAuth().getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Get the month and year from selected date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based, so add 1
        int year = calendar.get(Calendar.YEAR);

        Income income = new Income(
            "", // Auto-generated ID
            userId,
            title,
            amount,
            selectedDate,
            category,
            month,
            year,
            notes,
            System.currentTimeMillis()
        );

        FirebaseUtils.getFirestore().collection(FirebaseUtils.INCOMES_COLLECTION)
            .add(income)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Income saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to save income: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
}