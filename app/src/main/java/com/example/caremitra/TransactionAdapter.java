package com.example.caremitra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.description.setText(transaction.getDescription());
        holder.date.setText(transaction.getDate());

        switch (transaction.getTransactionType()) {
            case DEPOSIT:
            case REFUND:
                holder.amount.setText(String.format(Locale.getDefault(), "+ ₹%.2f", transaction.getAmount()));
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green)); // You'll need a green color in colors.xml
                holder.icon.setImageResource(R.drawable.ic_arrow_up);
                break;
            case PAYMENT:
                holder.amount.setText(String.format(Locale.getDefault(), "- ₹%.2f", transaction.getAmount()));
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red)); // You'll need a red color in colors.xml
                holder.icon.setImageResource(R.drawable.ic_arrow_down);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView description, date, amount;
        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.transaction_icon);
            description = view.findViewById(R.id.transaction_description);
            date = view.findViewById(R.id.transaction_date);
            amount = view.findViewById(R.id.transaction_amount);
        }
    }
}