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
import com.example.expense.data.Expense;
import com.example.expense.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText expenseTitleEditText;
    private TextInputEditText expenseAmountEditText;
    private TextView expenseDateTextView;
    private Spinner categorySpinner;
    private TextInputEditText expenseNotesEditText;
    private Button saveExpenseButton;
    private Button cancelButton;
    private ImageButton buttonBack;
    
    private long selectedDate = System.currentTimeMillis();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initViews();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initViews() {
        expenseTitleEditText = findViewById(R.id.editTextExpenseTitle);
        expenseAmountEditText = findViewById(R.id.editTextExpenseAmount);
        expenseDateTextView = findViewById(R.id.textViewExpenseDate);
        categorySpinner = findViewById(R.id.spinnerCategory);
        expenseNotesEditText = findViewById(R.id.editTextExpenseNotes);
        saveExpenseButton = findViewById(R.id.buttonSaveExpense);
        cancelButton = findViewById(R.id.buttonCancel);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void setupCategorySpinner() {
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare", "Education", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        expenseDateTextView.setOnClickListener(v -> showDatePicker());

        saveExpenseButton.setOnClickListener(v -> saveExpense());

        cancelButton.setOnClickListener(v -> finish());
        
        buttonBack.setOnClickListener(v -> finish());
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
                expenseDateTextView.setText(dateFormat.format(calendar.getTime()));
            },
            year, month, day
        );

        datePickerDialog.show();
    }

    private void saveExpense() {
        String title = expenseTitleEditText.getText().toString().trim();
        String amountStr = expenseAmountEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String notes = expenseNotesEditText.getText().toString().trim();

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

        Expense expense = new Expense(
            "", // Auto-generated ID
            userId,
            title,
            amount,
            selectedDate,
            category,
            notes,
            System.currentTimeMillis()
        );

        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .add(expense)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to save expense: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
}