package com.dragontwister.yondekiku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.dragontwister.yondekiku.interfaces.RecognitionCallback;
import com.dragontwister.yondekiku.managers.ContinuousRecognitionManager;
import com.dragontwister.yondekiku.managers.MessageAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private String name;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

//    Socket connection variables
    private WebSocket webSocket;
    private String SERVER_PATH = "ws://SERVER-IP-HERE:PORT-NUMBER-HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        Socket connection initialization
//        initiateSocketConnection();

//        Textview Initialization
        EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Enter name")
                .setView(editText)
                .setPositiveButton("confirm", (dialog, which) -> {
                    name = editText.getText().toString();
                })
                .create()
                .show();
        recyclerView = findViewById(R.id.recyclerView);
        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//          Speech Recognition Initialization
        manager = new ContinuousRecognitionManager(this, activationWords, deactivationWords, false, this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
        }

        manager.startRecognition();
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
//            webSocket.send(jsonObject.toString());
            jsonObject.put("isSent", true);
            messageAdapter.addItem(jsonObject);
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

//    Speech recognition functions
    @Override
    public void onKeywordDetected() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        progressBar.setProgress((int)rmsdB);
    }

    @Override
    public void onResults(@NotNull List<String> results, @Nullable float[] scores) {
        setTextView(String.join(". ", results) + ".");
        manager.startRecognition();
    }

    @Override
    public void setText(String txt) {
        setTextView("Activated");
        manager.startRecognition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.stopRecognition();
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this,
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
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