package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingScreenActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(LoadingScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 2500);
    }


}