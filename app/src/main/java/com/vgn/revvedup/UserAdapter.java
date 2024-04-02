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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameTextView.setText(String.format("%s %s", user.getFname(), user.getLname()));
        holder.roleTextView.setText(user.getRole());

        // Load and display the image using Glide
        Glide.with(holder.itemView.getContext())
                .load(user.getImagePath()) // Load image URL
                .placeholder(R.drawable.empty_image) // Placeholder image while loading
                .error(R.drawable.error_image) // Error image if loading fails
                .into(holder.userImageView); // ImageView to display the image
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView usernameTextView;
        private final TextView roleTextView;
        private final ImageView userImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            userImageView = itemView.findViewById(R.id.img);
        }
    }
}

