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
    private static String userRole;

    public CarsAdapter(List<Car> cars, String userRole) {
        CarsAdapter.cars = cars;
        CarsAdapter.userRole = userRole;
    }

    public interface OnItemClickListener {
        void onDetailsClick(Car car);

        void onDeleteClick(Car car);

        void onAcceptClick(Car car);

        void onDenyCar(Car car);

        void onModifyClick(Car car);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public CarsAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (userRole) {
            case "Admin":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_car_admin, parent, false);
                break;
            case "Organizator":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_car_event_admin, parent, false);
                break;
            case "Participant":
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_car_participant, parent, false);
                break;

        }
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
        private final Button viewDetails, deleteCar, acceptCar, denyCar, modifyCar;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            carRegistrationTextView = itemView.findViewById(R.id.carRegistration);
            carBrandTextView = itemView.findViewById(R.id.carBrand);
            carModelTextView = itemView.findViewById(R.id.carModel);
            carImageView = itemView.findViewById(R.id.img);
            viewDetails = itemView.findViewById(R.id.viewDetails);
            deleteCar = itemView.findViewById(R.id.deleteCar);
            acceptCar = itemView.findViewById(R.id.acceptCar);
            denyCar = itemView.findViewById(R.id.denyCar);
            modifyCar = itemView.findViewById(R.id.modifyCar);

            if (viewDetails != null) {
                viewDetails.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDetailsClick(cars.get(position));
                    }
                });
            }

            if (deleteCar != null) {
                deleteCar.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDeleteClick(cars.get(position));
                    }
                });
            }

            if (acceptCar != null) {
                acceptCar.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onAcceptClick(cars.get(position));
                    }
                });
            }

            if (denyCar != null) {
                denyCar.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDenyCar(cars.get(position));
                    }
                });
            }

            if (modifyCar != null) {
                modifyCar.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onModifyClick(cars.get(position));
                    }
                });
            }
        }
    }
}
