package com.dragontwister.yondekiku;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 2000);
    }
}
