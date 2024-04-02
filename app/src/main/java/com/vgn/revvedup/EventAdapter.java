package com.vgn.revvedup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.UserViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_event, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventTextView.setText(event.getName());
        holder.eventDateTextView.setText(String.format("%s - %s", event.getStartDate(), event.getEndDate()));

        // Load and display the image using Glide
        Glide.with(holder.itemView.getContext())
                .load(event.getEventImage()) // Load image URL
                .placeholder(R.drawable.empty_image) // Placeholder image while loading
                .error(R.drawable.error_image) // Error image if loading fails
                .into(holder.eventImageView); // ImageView to display the image
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventTextView;
        private final TextView eventDateTextView;
        private final ImageView eventImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.eventTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventImageView = itemView.findViewById(R.id.img);
        }
    }
}
