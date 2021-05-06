package com.dragontwister.yondekiku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.dragontwister.yondekiku.interfaces.RecognitionCallback;
import com.dragontwister.yondekiku.managers.ContinuousRecognitionManager;
import com.dragontwister.yondekiku.managers.MessageAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionCallback {
//    Speech Recognition variables
    private final String[] activationWords = new String[] {"hello"};
    private final String[] deactivationWords = new String[] {"thanks"};
    private static final int RECORD_AUDIO_REQUEST_CODE = 101;
    private ContinuousRecognitionManager manager;

//    Text View variables
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Text view Initialization
        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }

        manager = new ContinuousRecognitionManager(this, activationWords, deactivationWords, true, this);
        manager.startRecognition();
    }

//    Text view functions
    private void setTextView(String string){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Me");
            jsonObject.put("message", string);
            jsonObject.put("isSent", true);
            messageAdapter.addItem(jsonObject);
            if(messageAdapter.getItemCount() > 0)
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void clearMessages(View view){
        messageAdapter.clearItems();
    }

//    Speech recognition functions
    @Override
    public void onKeywordDetected(String type){

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int)rmsdB);
    }

    @Override
    public void onResults(@NotNull List<String> results) {
        StringBuilder text = new StringBuilder();
        for(int i=0; i<results.size(); i++){
            text.append(results.get(i)).append(". ");
        }

        setTextView(text.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.stopRecognition();
    }
}