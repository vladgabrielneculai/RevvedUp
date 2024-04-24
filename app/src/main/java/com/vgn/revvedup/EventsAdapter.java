package com.vgn.revvedup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.UserViewHolder> {

    private List<Event> events;
    private OnItemClickListener itemClickListener;
    private final String userRole;

    public EventsAdapter(List<Event> events, String userRole) {
        this.events = events;
        this.userRole = userRole;
    }

    public interface OnItemClickListener {
        void onDetailsClick(Event event);

        void onDeleteClick(Event event);

        void onLikeEvent(Event event);

        void onAddCarToEvent(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (userRole) {
            case "Admin":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_event_admin, parent, false);
                break;
            case "Organizator":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_event_event_admin, parent, false);
                break;
            case "Participant":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_event_participant, parent, false);
                break;
        }
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventTextView.setText(event.getName());
        holder.eventDateTextView.setText(String.format("%s - %s", event.getStartDate(), event.getEndDate()));

        // Load and display the image using Glide
        Glide.with(holder.itemView.getContext()).load(event.getEventImage()) // Load image URL
                .placeholder(R.drawable.empty_image) // Placeholder image while loading
                .error(R.drawable.error_image) // Error image if loading fails
                .into(holder.eventImageView); // ImageView to display the image
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventTextView;
        private final TextView eventDateTextView;
        private final ImageView eventImageView;
        private final Button viewDetails, deleteEvent, likeEvent, addCarToEvent;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.eventTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventImageView = itemView.findViewById(R.id.img);
            viewDetails = itemView.findViewById(R.id.viewDetails);
            deleteEvent = itemView.findViewById(R.id.deleteEvent);
            likeEvent = itemView.findViewById(R.id.likeEvent);
            addCarToEvent = itemView.findViewById(R.id.addCarToEvent);

            if (viewDetails != null) {
                viewDetails.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDetailsClick(events.get(position));
                    }
                });
            }


            if (deleteEvent != null) {
                deleteEvent.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDeleteClick(events.get(position));
                    }
                });
            }

            if (likeEvent != null) {
                likeEvent.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onLikeEvent(events.get(position));
                    }
                });
            }

            if (addCarToEvent != null) {
                addCarToEvent.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onAddCarToEvent(events.get(position));
                    }
                });
            }

        }
    }
}
