package com.example.expense;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.data.Expense;
import com.example.expense.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private TextInputEditText expenseTitleEditText;
    private TextInputEditText expenseAmountEditText;
    private TextView expenseDateTextView;
    private TextView expenseCategoryTextView;
    private TextInputEditText expenseNotesEditText;
    private Button editButton;
    private Button deleteButton;
    private ImageButton buttonBack;
    
    private String expenseId = "";
    private Expense expense;
    private boolean isEditing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        expenseId = getIntent().getStringExtra("expenseId");
        if (expenseId == null || expenseId.isEmpty()) {
            Toast.makeText(this, "Invalid expense ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadExpenseData();
    }

    private void initViews() {
        expenseTitleEditText = findViewById(R.id.editTextExpenseTitle);
        expenseAmountEditText = findViewById(R.id.editTextExpenseAmount);
        expenseDateTextView = findViewById(R.id.textViewExpenseDate);
        expenseCategoryTextView = findViewById(R.id.textViewExpenseCategory);
        expenseNotesEditText = findViewById(R.id.editTextExpenseNotes);
        editButton = findViewById(R.id.buttonEdit);
        deleteButton = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBack);
    }
    
    private void loadExpenseData() {
        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .document(expenseId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    expense = document.toObject(Expense.class);
                    if (expense != null) {
                        expenseTitleEditText.setText(expense.getTitle());
                        expenseAmountEditText.setText(String.valueOf(expense.getAmount()));
                        expenseNotesEditText.setText(expense.getNotes());
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        expenseDateTextView.setText(dateFormat.format(new Date(expense.getDate())));
                        
                        expenseCategoryTextView.setText(expense.getCategory());
                    }
                }
            })
            .addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to load expense: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
        
        editButton.setOnClickListener(v -> {
            if (isEditing) {
                // Save changes
                updateExpense();
            } else {
                // Switch to edit mode
                enableEditMode();
            }
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void enableEditMode() {
        isEditing = true;
        expenseTitleEditText.setEnabled(true);
        expenseAmountEditText.setEnabled(true);
        expenseNotesEditText.setEnabled(true);
        editButton.setText("Save");
        deleteButton.setText("Cancel");
    }

    private void disableEditMode() {
        isEditing = false;
        expenseTitleEditText.setEnabled(false);
        expenseAmountEditText.setEnabled(false);
        expenseNotesEditText.setEnabled(false);
        editButton.setText("Edit");
        deleteButton.setText("Delete");
    }

    private void updateExpense() {
        if (expense == null) return;
        
        String title = expenseTitleEditText.getText().toString().trim();
        String amountStr = expenseAmountEditText.getText().toString().trim();
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

        Expense updatedExpense = new Expense(
            expense.getId(),
            expense.getUserId(),
            title,
            amount,
            expense.getDate(), // Keep original date
            expense.getCategory(), // Keep original category for now
            notes,
            expense.getCreatedAt() // Keep original creation time
        );

        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .document(expenseId)
            .set(updatedExpense)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                expense = updatedExpense; // Update the local expense object
                disableEditMode();
            })
            .addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to update expense: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }

    private void showDeleteConfirmationDialog() {
        if (isEditing) {
            // If in edit mode, cancel editing instead
            if (expense != null) {
                expenseTitleEditText.setText(expense.getTitle());
                expenseAmountEditText.setText(String.valueOf(expense.getAmount()));
                expenseNotesEditText.setText(expense.getNotes());
            }
            disableEditMode();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes", (dialog, which) -> deleteExpense())
            .setNegativeButton("No", null)
            .show();
    }

    private void deleteExpense() {
        FirebaseUtils.getFirestore().collection(FirebaseUtils.EXPENSES_COLLECTION)
            .document(expenseId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to delete expense: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
}