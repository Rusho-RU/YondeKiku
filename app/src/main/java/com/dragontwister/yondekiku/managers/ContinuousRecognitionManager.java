package com.dragontwister.yondekiku.managers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.dragontwister.yondekiku.interfaces.RecognitionCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContinuousRecognitionManager implements RecognitionListener {
    private static final int SILENCE_TIME_in_MILLIS = 300;
    private Context context;
    private RecognitionCallback callback;
    private String[] activationWords;
    private String[] deactivationWords;
    private boolean shouldMute;

    private AudioManager audioManager;

    private SpeechRecognizer speech;
    private Intent recognizerIntent;

    public boolean isSpeaking = false;

    private List<String> matches;

    public ContinuousRecognitionManager(Context context, String[] activationWords, String[] deactivationWords, boolean shouldMute, RecognitionCallback callback){
        this.context = context;
        this.callback = callback;
        this.activationWords = activationWords;
        this.deactivationWords = deactivationWords;
        this.shouldMute = shouldMute;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothA2dpOn()) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
        }

        speech = SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIME_in_MILLIS);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        matches = new ArrayList<>();
    }

    public void startRecognition(){
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
    }

    public void stopRecognition() {
        speech.stopListening();
//        audioManager.setMode(AudioManager.MODE_NORMAL);
//        audioManager.stopBluetoothSco();
//        audioManager.setBluetoothScoOn(false);
    }

    public void cancelRecognition() {
        speech.cancel();
    }

    public void destroyRecognizer() {
        muteRecognition(false);
        speech.destroy();
    }

    @SuppressWarnings( "deprecation" )
    private void muteRecognition(boolean mute){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flag = mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE;
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, flag, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute);
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, mute);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
            audioManager.setStreamMute(AudioManager.STREAM_RING, mute);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        muteRecognition(shouldMute || !isSpeaking);
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        callback.onRmsChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY :{
                cancelRecognition();
            }
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT : {
                destroyRecognizer();
            }
        }

        startRecognition();
    }

    @Override
    public void onResults(Bundle results) {
        List<String> hold = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if(hold != null){
            String text = String.join(". ", hold);

            if(isSpeaking){
                if(Arrays.stream(deactivationWords).anyMatch(text::contains)){
                    isSpeaking = false;
                    matches.add(text);
                    callback.onResults(matches, scores);
                    startRecognition();
                } else{
                    matches.add(text);
                    startRecognition();
                }
            } else if(Arrays.stream(activationWords).anyMatch(text::contains)){
                isSpeaking = true;
                matches.clear();
                matches.add(text);
                callback.setText("Activation word detected");
                startRecognition();
            }

            else
                startRecognition();
        }
        else
            startRecognition();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        callback.setText("Partial");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
