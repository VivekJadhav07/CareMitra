package com.example.caremitra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentDetails> appointmentList;

    public AppointmentAdapter(List<AppointmentDetails> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {
        AppointmentDetails appt = appointmentList.get(position);
        holder.tvHospital.setText(appt.getHospital_name() != null ? appt.getHospital_name() : "Unknown Hospital");
        holder.tvDate.setText(formatDate(appt.getScheduled_at()));
        holder.tvNote.setText(appt.getNotes() != null ? appt.getNotes() : "");
        holder.tvStatus.setText(appt.getStatus() != null ? appt.getStatus() : "");
    }

    @Override
    public int getItemCount() {
        return appointmentList != null ? appointmentList.size() : 0;
    }

    public void updateData(List<AppointmentDetails> newList) {
        appointmentList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospital, tvDate, tvNote, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "No Date";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
            Date d = input.parse(isoDate.replace("Z", ""));
            return output.format(d);
        } catch (Exception e) {
            return isoDate.split("T")[0];
        }
    }
}
