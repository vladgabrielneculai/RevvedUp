package com.vgn.revvedup;

import android.net.Uri;
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

public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.UserViewHolder> {

    private static List<Car> cars = null;
    private static OnItemClickListener itemClickListener;

    public CarsAdapter(List<Car> cars) {
        CarsAdapter.cars = cars;
    }

    public interface OnItemClickListener {
        void onDetailsClick(Car car);

        void onDeleteClick(Car car);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public CarsAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_car_admin, parent, false);
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CarsAdapter.UserViewHolder holder, int position) {
        Car car = cars.get(position);
        holder.carBrandTextView.setText(car.getCarBrand());
        holder.carModelTextView.setText(car.getCarModel());
        holder.carRegistrationTextView.setText(car.getCarRegistration());

        // Load and display the first image using Glide
        if (car.getCarPicturesUri() != null && !car.getCarPicturesUri().isEmpty()) {
            // Get the first URI from the list
            Uri firstPictureUri = Uri.parse(car.getCarPicturesUri().get(0));

            // Load image using Glide
            Glide.with(holder.itemView.getContext())
                    .load(firstPictureUri) // Load image URI
                    .placeholder(R.drawable.empty_image) // Placeholder image while loading
                    .error(R.drawable.error_image) // Error image if loading fails
                    .into(holder.carImageView); // ImageView to display the image
        } else {
            //Handle case where no picture URI is available
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.empty_image) // Load placeholder image
                    .into(holder.carImageView); // ImageView to display the image
        }
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView carRegistrationTextView;
        private final TextView carBrandTextView;
        private final TextView carModelTextView;
        private final ImageView carImageView;
        private final Button viewDetails, deleteCar;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            carRegistrationTextView = itemView.findViewById(R.id.carRegistration);
            carBrandTextView = itemView.findViewById(R.id.carBrand);
            carModelTextView = itemView.findViewById(R.id.carModel);
            carImageView = itemView.findViewById(R.id.img);
            viewDetails = itemView.findViewById(R.id.viewDetails);
            deleteCar = itemView.findViewById(R.id.deleteCar);

            viewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onDetailsClick(cars.get(position));
                }
            });

            deleteCar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onDeleteClick(cars.get(position));
                }
            });


        }
    }
}
