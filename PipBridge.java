package com.pip.roadbuddy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/** JavaScript bridge: text-to-speech, speech recognition and vibration for the web UI. */
public final class PipBridge extends UtteranceProgressListener implements RecognitionListener {
    private final MainActivity act;
    private SpeechRecognizer rec;

    public PipBridge(MainActivity act) { this.act = act; }

    void js(final String code) {
        act.runOnUiThread(() -> { if (act.web != null) act.web.evaluateJavascript(code, null); });
    }

    /* ---- speaking ---- */
    @JavascriptInterface
    public void speak(String text, float pitch, String id) {
        if (act.ttsReady && act.tts != null) {
            act.tts.setPitch(pitch);
            act.tts.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), id);
        } else {
            spoken(id);
        }
    }
    private void spoken(String id) { js("window.PipVoice&&PipVoice.onSpoken(" + JSONObject.quote(id) + ")"); }
    @Override public void onStart(String id) {}
    @Override public void onDone(String id) { spoken(id); }
    @Override public void onError(String id) { spoken(id); }

    /* ---- listening ---- */
    @JavascriptInterface
    public boolean canListen() { return SpeechRecognizer.isRecognitionAvailable(act); }

    @JavascriptInterface
    public boolean hasMic() {
        return act.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @JavascriptInterface
    public void requestMic() {
        act.runOnUiThread(() -> act.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MainActivity.MIC_REQUEST));
    }

    @JavascriptInterface
    public void listen(final String lang) {
        act.runOnUiThread(() -> {
            if (!hasMic()) { js("window.PipVoice&&PipVoice.onError(9)"); return; }
            if (rec == null) {
                rec = SpeechRecognizer.createSpeechRecognizer(act);
                rec.setRecognitionListener(this);
            }
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang);
            i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, act.getPackageName());
            try { rec.startListening(i); }
            catch (Exception e) { js("window.PipVoice&&PipVoice.onError(5)"); }
        });
    }

    @JavascriptInterface
    public void stopListening() {
        act.runOnUiThread(() -> { if (rec != null) rec.cancel(); });
    }

    void destroy() { if (rec != null) { rec.destroy(); rec = null; } }

    @Override public void onReadyForSpeech(Bundle b) {}
    @Override public void onBeginningOfSpeech() { js("window.PipVoice&&PipVoice.onStart()"); }
    @Override public void onRmsChanged(float v) {}
    @Override public void onBufferReceived(byte[] b) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onError(int code) { js("window.PipVoice&&PipVoice.onError(" + code + ")"); }
    @Override public void onResults(Bundle b) {
        ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        JSONArray a = new JSONArray();
        if (r != null) for (String s : r) a.put(s);
        js("window.PipVoice&&PipVoice.onResult(" + a.toString() + ")");
    }
    @Override public void onPartialResults(Bundle b) {}
    @Override public void onEvent(int t, Bundle b) {}

    /* ---- vibration ---- */
    @JavascriptInterface
    public void vibrate() {
        Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        long[] pattern = {0, 400, 200, 400, 200, 400};
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createWaveform(pattern, -1));
        else v.vibrate(pattern, -1);
    }
}
