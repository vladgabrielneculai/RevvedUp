package com.vgn.revvedup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class EventFragment extends Fragment {

    // TODO: If the user is "admin": display the events from the database
    // TODO: If the user is "organizator": display his events
    // TODO: If the user is "participant": display the events based on the recommandation algorithm

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }
}