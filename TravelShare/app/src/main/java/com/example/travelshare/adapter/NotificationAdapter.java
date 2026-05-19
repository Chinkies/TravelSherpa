package com.example.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView senderImage;
        private final TextView message;
        private final TextView time;
        private final View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.notification_sender_image);
            message = itemView.findViewById(R.id.notification_message);
            time = itemView.findViewById(R.id.notification_time);
            unreadIndicator = itemView.findViewById(R.id.notification_unread_indicator);
        }

        public void bind(Notification notification, OnNotificationClickListener listener, SimpleDateFormat dateFormat) {
            message.setText(notification.getMessage());
            
            if (notification.getTimestamp() != null) {
                time.setText(dateFormat.format(notification.getTimestamp()));
            }

            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            Glide.with(itemView.getContext())
                    .load(notification.getSenderProfilePictureUrl())
                    .placeholder(R.drawable.default_user)
                    .circleCrop()
                    .into(senderImage);

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }
    }
}
