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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private static List<User> users = null;
    private static OnItemClickListener itemClickListener;

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    public interface OnItemClickListener {
        void onDeleteClick(User user);

        void onViewDetailsUser(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
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
        private final Button deleteUser, viewDetailsUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            userImageView = itemView.findViewById(R.id.img);
            deleteUser = itemView.findViewById(R.id.deleteUser);
            viewDetailsUser = itemView.findViewById(R.id.viewDetails);

            deleteUser.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onDeleteClick(users.get(position));
                }
            });

            viewDetailsUser.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onViewDetailsUser(users.get(position));
                }
            });
        }
    }
}

