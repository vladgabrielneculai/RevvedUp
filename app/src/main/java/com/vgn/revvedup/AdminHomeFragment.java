package com.vgn.revvedup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHomeFragment extends Fragment {

    private DatabaseReference databaseReference;
    private PieChart modsPieChart;
    private PieDataSet modsDataSet;
    private List<PieEntry> modsPC;

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        modsPieChart = view.findViewById(R.id.pieChartMods);

        modsPC = new ArrayList<>();

        modsDataSet = new PieDataSet(modsPC, "Modificări auto");


        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("cars");

        // Retrieve data for Mods PieChart
        retrieveModsData();

        return view;
    }

    private void retrieveModsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the previous data
                modsPC.clear();

                // Count the number of each modification
                Map<String, Integer> modsCount = new HashMap<>();
                for (DataSnapshot carSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot modsSnapshot = carSnapshot.child("modsApplied");
                    for (DataSnapshot modSnapshot : modsSnapshot.getChildren()) {
                        String modName = modSnapshot.getValue(String.class);
                        if (modsCount.containsKey(modName)) {
                            modsCount.put(modName, modsCount.get(modName) + 1);
                        } else {
                            modsCount.put(modName, 1);
                        }
                    }
                }

                // Add data to modsPC list
                for (Map.Entry<String, Integer> entry : modsCount.entrySet()) {
                    modsPC.add(new PieEntry(entry.getValue(), entry.getKey()));
                }

                // Set a different color for each entry
                List<Integer> colors = new ArrayList<>();
                for (int color : ColorTemplate.COLORFUL_COLORS) {
                    colors.add(color);
                }

                // Create a data set for Mods PieChart
                modsDataSet = new PieDataSet(modsPC, "Modificări auto");
                modsDataSet.setColors(colors);
                modsDataSet.setValueTextSize(18);
                modsDataSet.setDrawValues(true);

                // Update Mods PieChart
                updateModsPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }


    private void updateModsPieChart() {
        // Set options for modsDataSet
        // For example, colors, text size, etc.

        // Assemble data
        PieData modsData = new PieData(modsDataSet);

        // Set data for Mods PieChart
        modsPieChart.setData(modsData);

        // Refresh Mods PieChart
        modsPieChart.invalidate();

        //Get rid of the Description Label
        modsPieChart.getDescription().setEnabled(false);

        //Set center text
        modsPieChart.setCenterText("Modificări auto");
        modsPieChart.setDrawEntryLabels(true);
        modsPieChart.setHoleRadius(50);
        modsPieChart.setCenterTextSize(16);

        Legend legend = modsPieChart.getLegend();
        legend.setEnabled(false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
