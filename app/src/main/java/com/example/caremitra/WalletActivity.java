package com.example.caremitra;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private Spinner hospitalSpinner;
    private TextView walletBalance;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        toolbar = findViewById(R.id.toolbar_wallet);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.transactions_recycler_view);
        hospitalSpinner = findViewById(R.id.hospital_spinner);
        walletBalance = findViewById(R.id.wallet_balance);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDummyData(); // Load sample data

        // Initialize adapter with the (initially empty) filtered list
        adapter = new TransactionAdapter(this, filteredTransactions);
        recyclerView.setAdapter(adapter);

        setupSpinner();
        filterTransactions("All Hospitals"); // Initially show all
    }

    private void setupSpinner() {
        // In a real app, you would get this list from your database
        List<String> hospitalNames = new ArrayList<>();
        hospitalNames.add("All Hospitals");
        hospitalNames.add("Ruby Hall Clinic");
        hospitalNames.add("District Hospital");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hospitalNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospitalSpinner.setAdapter(spinnerAdapter);

        hospitalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHospital = parent.getItemAtPosition(position).toString();
                filterTransactions(selectedHospital);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterTransactions(String hospitalName) {
        filteredTransactions.clear();
        if ("All Hospitals".equals(hospitalName)) {
            filteredTransactions.addAll(allTransactions);
        } else {
            for (Transaction transaction : allTransactions) {
                if (transaction.getHospitalName().equals(hospitalName)) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadDummyData() {
        allTransactions.add(new Transaction("Consultation Fee", "30 Sep 2025", 500.00, Transaction.Type.PAYMENT, "Ruby Hall Clinic"));
        allTransactions.add(new Transaction("Wallet Deposit", "29 Sep 2025", 6000.00, Transaction.Type.DEPOSIT, "Ruby Hall Clinic"));
        allTransactions.add(new Transaction("Pharmacy Bill", "28 Sep 2025", 850.00, Transaction.Type.PAYMENT, "District Hospital"));
        allTransactions.add(new Transaction("Overpayment Refund", "27 Sep 2025", 150.00, Transaction.Type.REFUND, "District Hospital"));
        allTransactions.add(new Transaction("Lab Tests", "26 Sep 2025", 1200.00, Transaction.Type.PAYMENT, "Ruby Hall Clinic"));

        // Calculate and set the balance
        double balance = 0;
        for (Transaction t : allTransactions) {
            if (t.getTransactionType() == Transaction.Type.DEPOSIT || t.getTransactionType() == Transaction.Type.REFUND) {
                balance += t.getAmount();
            } else {
                balance -= t.getAmount();
            }
        }
        walletBalance.setText(String.format(Locale.getDefault(), "₹ %.2f", balance));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous screen
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}