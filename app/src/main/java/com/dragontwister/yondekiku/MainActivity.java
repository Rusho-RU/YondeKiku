package com.dragontwister.yondekiku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dragontwister.yondekiku.interfaces.RecognitionCallback;
import com.dragontwister.yondekiku.managers.ContinuousRecognitionManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionCallback {
    private final String[] activationWords = new String[] {"hello"};
    private final String[] deactivationWords = new String[] {"thanks"};

    private static final int RECORD_AUDIO_REQUEST_CODE = 101;

    private ProgressBar progressBar;
    private TextView textView;

    private ContinuousRecognitionManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = new ContinuousRecognitionManager(this, activationWords, deactivationWords, false, this);

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setMax(10);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }

        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);

        manager.startRecognition();
    }

    @Override
    public void onKeywordDetected() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int)rmsdB);
    }

    @Override
    public void onResults(@NotNull List<String> results, @Nullable float[] scores) {
        textView.setText(String.join(". ", results) + ".");
        manager.startRecognition();
    }

    @Override
    public void setText(String txt) {
        textView.setText(txt);
        manager.startRecognition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.stopRecognition();
    }
}