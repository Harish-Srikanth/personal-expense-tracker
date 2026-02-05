package com.example.expense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.R;
import com.example.expense.data.Income;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private List<Income> incomes;
    private OnIncomeClickListener onIncomeClickListener;

    public interface OnIncomeClickListener {
        void onIncomeClick(Income income);
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        TextView category;
        TextView amount;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewIncomeTitle);
            date = itemView.findViewById(R.id.textViewIncomeDate);
            category = itemView.findViewById(R.id.textViewIncomeCategory);
            amount = itemView.findViewById(R.id.textViewIncomeAmount);
        }
    }

    public IncomeAdapter(List<Income> incomes, OnIncomeClickListener onIncomeClickListener) {
        this.incomes = incomes;
        this.onIncomeClickListener = onIncomeClickListener;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_income, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomes.get(position);
        holder.title.setText(income.getTitle());
        holder.category.setText(income.getCategory());
        holder.amount.setText("+$" + String.format("%.2f", income.getAmount()));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.date.setText(dateFormat.format(new Date(income.getDate())));
        
        holder.itemView.setOnClickListener(v -> {
            if (onIncomeClickListener != null) {
                onIncomeClickListener.onIncomeClick(income);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incomes.size();
    }
}