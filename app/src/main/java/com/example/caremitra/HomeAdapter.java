package com.example.caremitra;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Define constants for each new view type
    private static final int ITEM_TYPE_WELCOME = 0;
    private static final int ITEM_TYPE_VITALS_GRID = 1;
    private static final int ITEM_TYPE_BANNER = 2;
    private static final int ITEM_TYPE_HEALTH_ANALYTICS = 3;
    private static final int ITEM_TYPE_SECTION_HEADER = 4; // ADDED
    private static final int ITEM_TYPE_APPOINTMENT_CARD = 5; // ADDED
    private static final int ITEM_TYPE_ALERTS = 6;           // Renumbered
    private final Context context;
    private List<Object> items;
    private int lastPosition = -1;

    // --- Click Listener Code ---
    public interface OnItemClickListener {
        void onItemClick(Object itemData);
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // --- End of Click Listener Code ---

    public HomeAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    // ADDED: New DataModel for the section header
    public static class SectionHeaderData {
        String title;
        public SectionHeaderData(String title) { this.title = title; }

        // ADD THIS METHOD
        public String getTitle() {
            return title;
        }
    }

    // ADDED: New DataModel for the appointment card
    public static class AppointmentCardData {
        AppointmentDetails appointmentDetails;
        public AppointmentCardData(AppointmentDetails details) { this.appointmentDetails = details; }
    }
    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof WelcomeData) return ITEM_TYPE_WELCOME;
        if (items.get(position) instanceof VitalsGridData) return ITEM_TYPE_VITALS_GRID;
        if (items.get(position) instanceof BannerData) return ITEM_TYPE_BANNER;
        if (items.get(position) instanceof HealthAnalyticsData) return ITEM_TYPE_HEALTH_ANALYTICS;
        if (items.get(position) instanceof SectionHeaderData) return ITEM_TYPE_SECTION_HEADER; // ADDED
        if (items.get(position) instanceof AppointmentCardData) return ITEM_TYPE_APPOINTMENT_CARD; // ADDED
        if (items.get(position) instanceof AlertsData) return ITEM_TYPE_ALERTS;
        return -1;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM_TYPE_WELCOME:
                return new WelcomeViewHolder(inflater.inflate(R.layout.item_welcome, parent, false));
            case ITEM_TYPE_VITALS_GRID:
                return new VitalsGridViewHolder(inflater.inflate(R.layout.item_vitals_grid, parent, false));
            case ITEM_TYPE_BANNER:
                return new BannerViewHolder(inflater.inflate(R.layout.item_banner, parent, false));
            case ITEM_TYPE_HEALTH_ANALYTICS:
                return new HealthAnalyticsViewHolder(inflater.inflate(R.layout.item_health_analytics, parent, false));
            case ITEM_TYPE_SECTION_HEADER: // ADDED
                return new SectionHeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
            case ITEM_TYPE_APPOINTMENT_CARD: // ADDED
                return new AppointmentViewHolder(inflater.inflate(R.layout.item_appointment, parent, false));
            case ITEM_TYPE_ALERTS:
                return new AlertsViewHolder(inflater.inflate(R.layout.item_alerts_card, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        setAnimation(holder.itemView, position);
        switch (holder.getItemViewType()) {
            case ITEM_TYPE_WELCOME:
                ((WelcomeViewHolder) holder).bind((WelcomeData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_VITALS_GRID:
                ((VitalsGridViewHolder) holder).bind((VitalsGridData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_BANNER:
                ((BannerViewHolder) holder).bind((BannerData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_HEALTH_ANALYTICS:
                ((HealthAnalyticsViewHolder) holder).bind((HealthAnalyticsData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_SECTION_HEADER:
                ((SectionHeaderViewHolder) holder).bind((SectionHeaderData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_APPOINTMENT_CARD:
                ((AppointmentViewHolder) holder).bind((AppointmentCardData) items.get(position));
                break; // FIXED
            case ITEM_TYPE_ALERTS:
                ((AlertsViewHolder) holder).bind((AlertsData) items.get(position));
                break; // FIXED
        }
    }
    @Override
    public int getItemCount() { return items.size(); }

    private String getGreetingMessage() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 5 && timeOfDay < 12) return "Good Morning,";
        if (timeOfDay >= 12 && timeOfDay < 17) return "Good Afternoon,";
        if (timeOfDay >= 17 && timeOfDay < 21) return "Good Evening,";
        return "Good Night,";
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // --- ViewHolder Classes ---

    class WelcomeViewHolder extends RecyclerView.ViewHolder {
        TextView greetingText, welcomeText;
        WelcomeViewHolder(View itemView) {
            super(itemView);
            greetingText = itemView.findViewById(R.id.greetingText);
            welcomeText = itemView.findViewById(R.id.welcomeText);
        }
        void bind(WelcomeData data) {
            greetingText.setText(getGreetingMessage());
            welcomeText.setText(data.userName);
        }
    }

    class HealthAnalyticsViewHolder extends RecyclerView.ViewHolder {
        TextView sugarLevels;
        ImageView graphImage;
        HealthAnalyticsViewHolder(View itemView) {
            super(itemView);
            sugarLevels = itemView.findViewById(R.id.sugarLevels);
            graphImage = itemView.findViewById(R.id.graphImage);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onItemClick(items.get(pos));
                }
            });
        }
        void bind(HealthAnalyticsData data) {
            sugarLevels.setText("Sugar Levels: " + data.sugarLevelText);
            graphImage.setImageResource(data.graphDrawableRes);
        }
    }

    class VitalsGridViewHolder extends RecyclerView.ViewHolder {
        TextView heartRate, oxygenLevel, bodyTemp;
        VitalsGridViewHolder(View itemView) {
            super(itemView);
            heartRate = itemView.findViewById(R.id.heartRate);
            oxygenLevel = itemView.findViewById(R.id.oxygenLevel);
            bodyTemp = itemView.findViewById(R.id.bodyTemp);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) listener.onItemClick(items.get(pos));
                }
            });
        }
        void bind(VitalsGridData data) {
            heartRate.setText(data.heartRate);
            oxygenLevel.setText(data.oxygenLevel);
            bodyTemp.setText(data.bodyTemp);
        }
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        private final Handler bannerHandler = new Handler(Looper.getMainLooper());
        private Runnable bannerRunnable;
        private int currentBannerIndex = 0;
        BannerViewHolder(View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
        }
        void bind(BannerData data) {
            if (data.bannerImages == null || data.bannerImages.length == 0) return;
            bannerHandler.removeCallbacks(bannerRunnable);
            bannerRunnable = () -> {
                bannerImage.setImageResource(data.bannerImages[currentBannerIndex]);
                currentBannerIndex = (currentBannerIndex + 1) % data.bannerImages.length;
                bannerHandler.postDelayed(bannerRunnable, 3000);
            };
            bannerHandler.post(bannerRunnable);
        }
    }

    class AlertsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout alertsContainer;
        AlertsViewHolder(View itemView) {
            super(itemView);
            alertsContainer = itemView.findViewById(R.id.alertsContainer);
        }
        void bind(AlertsData data) {
            alertsContainer.removeAllViews();
            if (data.alerts == null || data.alerts.length == 0) {
                TextView tv = new TextView(context);
                tv.setText("No alerts at this time");
                alertsContainer.addView(tv);
            } else {
                for (String alert : data.alerts) {
                    TextView tv = new TextView(context);
                    tv.setText("• " + alert);
                    tv.setPadding(0, 4, 0, 4);
                    alertsContainer.addView(tv);
                }
            }
        }
    }
    class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        TextView sectionViewAll;
        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
            sectionViewAll = itemView.findViewById(R.id.section_view_all);

            // ADD THIS CLICK LISTENER
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(items.get(pos));
                    }
                }
            });
        }
        void bind(SectionHeaderData data) {
            sectionTitle.setText(data.title);
        }

    }


    // ADD THIS CLASS
    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospital, tvDate, tvStatus, tvNote;
        AppointmentViewHolder(View itemView) {
            super(itemView);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
        void bind(AppointmentCardData data) {
            AppointmentDetails details = data.appointmentDetails;
            tvHospital.setText(details.getHospital_name());
            tvDate.setText(details.getScheduled_at());
            tvStatus.setText(details.getStatus());
            tvNote.setText(details.getNotes());
        }
    }

    // --- Data Model Classes ---
    public static class WelcomeData {
        String userName;
        public WelcomeData(String name) { this.userName = name; }
    }
    public static class VitalsGridData {
        String heartRate, oxygenLevel, bodyTemp;
        public VitalsGridData(String hr, String oxy, String temp) { this.heartRate = hr; this.oxygenLevel = oxy; this.bodyTemp = temp; }
    }
    public static class BannerData {
        int[] bannerImages;
        public BannerData(int[] images) { this.bannerImages = images; }
    }
    public static class HealthAnalyticsData {
        String sugarLevelText;
        int graphDrawableRes;
        public HealthAnalyticsData(String s, int d) { this.sugarLevelText = s; this.graphDrawableRes = d; }
    }
    public static class AlertsData {
        String[] alerts;
        public AlertsData(String[] a) { this.alerts = a; }
    }

}