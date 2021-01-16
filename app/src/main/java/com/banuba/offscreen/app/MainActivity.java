package com.banuba.offscreen.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.banuba.offscreen.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnPortrait = findViewById(R.id.activity_main_btn_portrait);
        btnPortrait.setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, CameraActivityPortrait.class);
            startActivity(intent);
        });

        final Button btnSensor = findViewById(R.id.activity_main_btn_full_sensor);
        btnSensor.setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, CameraActivityFullSensor.class);
            startActivity(intent);
        });

        final Button btnFullSensor = findViewById(R.id.activity_main_btn_default);
        btnFullSensor.setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, CameraActivityDefault.class);
            startActivity(intent);
        });

    }
}