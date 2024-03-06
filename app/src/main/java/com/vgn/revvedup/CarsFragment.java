package com.vgn.revvedup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class CarsFragment extends Fragment {

    // TODO: If the user is "admin": display the cars from the database
    // TODO: If the user is "participant": display his cars (he/she might have more than one car)
    // TODO: If the user is "organizator": display all the cars that registered in his/hers event & create functionality so that the event admin can accept or refuse the car in his event

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cars, container, false);
    }
}