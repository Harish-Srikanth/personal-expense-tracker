package com.example.expense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.R;
import com.example.expense.data.Expense;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private OnExpenseClickListener onExpenseClickListener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        TextView category;
        TextView amount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewExpenseTitle);
            date = itemView.findViewById(R.id.textViewExpenseDate);
            category = itemView.findViewById(R.id.textViewExpenseCategory);
            amount = itemView.findViewById(R.id.textViewExpenseAmount);
        }
    }

    public ExpenseAdapter(List<Expense> expenses, OnExpenseClickListener onExpenseClickListener) {
        this.expenses = expenses;
        this.onExpenseClickListener = onExpenseClickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.title.setText(expense.getTitle());
        holder.category.setText(expense.getCategory());
        holder.amount.setText("-$" + String.format("%.2f", expense.getAmount()));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.date.setText(dateFormat.format(new Date(expense.getDate())));
        
        holder.itemView.setOnClickListener(v -> {
            if (onExpenseClickListener != null) {
                onExpenseClickListener.onExpenseClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }
}