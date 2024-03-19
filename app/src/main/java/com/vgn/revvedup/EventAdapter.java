package com.vgn.revvedup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.eventTextView.setText(String.format("%s", event.getName()));
        holder.eventDateTextView.setText(String.format("%s - %s", event.getStartDate(), event.getEndDate()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventTextView;
        private final TextView eventDateTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.eventTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
        }
    }
}

