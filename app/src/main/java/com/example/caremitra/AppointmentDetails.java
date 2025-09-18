package com.example.caremitra;

public class AppointmentDetails {
    private String appointment_id;
    private String scheduled_at;
    private String status;
    private String notes;
    private String hospital_id;
    private String hospital_name;

    public String getAppointment_id() { return appointment_id; }
    public void setAppointment_id(String appointment_id) { this.appointment_id = appointment_id; }

    public String getScheduled_at() { return scheduled_at; }
    public void setScheduled_at(String scheduled_at) { this.scheduled_at = scheduled_at; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getHospital_id() { return hospital_id; }
    public void setHospital_id(String hospital_id) { this.hospital_id = hospital_id; }

    public String getHospital_name() { return hospital_name; }
    public void setHospital_name(String hospital_name) { this.hospital_name = hospital_name; }
}
