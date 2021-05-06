package com.dragontwister.yondekiku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dragontwister.yondekiku.interfaces.RecognitionCallback;
import com.dragontwister.yondekiku.managers.ContinuousRecognitionManager;
import com.dragontwister.yondekiku.managers.MessageAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity implements RecognitionCallback {
//    Speech Recognition variables
    private final String[] activationWords = new String[] {"hello"};
    private final String[] deactivationWords = new String[] {"thanks"};
    private static final int RECORD_AUDIO_REQUEST_CODE = 101;
    private ContinuousRecognitionManager manager;

//    Text View variables
    private ProgressBar progressBar;
    private String name = "Anonymous";
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

//    Socket connection variables
    private WebSocket webSocket;
    private String SERVER_PATH = "https://websockt-asr.herokuapp.com/";
//    private String SERVER_PATH = "ws://192.168.0.102:3000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Socket connection initialization
        initiateSocketConnection();

//        Text view Initialization
        recyclerView = findViewById(R.id.recyclerView);
        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }

        EditText editText = new EditText(this);
        editText.setAllCaps(true);
        new AlertDialog.Builder(this)
                .setTitle("Enter name")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("confirm", (dialog, which) -> {
                    name = editText.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.textView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnClear).setVisibility(View.VISIBLE);
//                    Speech Recognition Initialization
                    manager = new ContinuousRecognitionManager(this, activationWords, deactivationWords, true, this);
                    manager.startRecognition();
                })
                .create()
                .show();
    }

//    Socket connection functions
    private void initiateSocketConnection() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new MainActivity.SocketListener());
    }

//    Text view functions
    private void setTextView(String string){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("message", string);
            webSocket.send(jsonObject.toString());
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
        switch (type){
            case "active": setTextView(name + " is speaking now...");
            break;
            case "deactivate" : setTextView(name + " has finished speech");
            break;
        }
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

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Socket Connection Successful!",
                    Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent", false);

                    messageAdapter.addItem(jsonObject);

                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}