package com.example.expense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.R;
import com.example.expense.data.CategorySummary;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategorySummary> categories;

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView categoryAmount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textViewCategoryName);
            categoryAmount = itemView.findViewById(R.id.textViewCategoryAmount);
        }
    }

    public CategoryAdapter(List<CategorySummary> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategorySummary category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryAmount.setText("$" + String.format("%.2f", category.getAmount()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<CategorySummary> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }
}