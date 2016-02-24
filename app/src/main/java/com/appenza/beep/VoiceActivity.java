package com.appenza.beep;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.*;

public class VoiceActivity extends AppCompatActivity {

    private static final String TAG = "beep";

    Button recordBtn, stopRecBtn, playBtn, stopPlayingBtn;
    MediaRecorder recorder;
    MediaPlayer player;
    File audiofile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        recordBtn = (Button) findViewById(R.id.recordBtn);
        stopRecBtn = (Button) findViewById(R.id.stopRecBtn);
        recordBtn.setVisibility(View.VISIBLE);
        stopRecBtn.setVisibility(View.GONE);

        playBtn = (Button) findViewById(R.id.playBtn);
        stopPlayingBtn = (Button) findViewById(R.id.stopPlayingBtn);
        playBtn.setVisibility(View.VISIBLE);
        stopPlayingBtn.setVisibility(View.GONE);

    }


    public void startRecording(View view) {
        recordBtn.setVisibility(View.GONE);
        stopRecBtn.setVisibility(View.VISIBLE);

        try {

            File dir = new File(Environment.getExternalStorageDirectory(), "peep-files");
            audiofile = new File(dir, "voice-" + System.currentTimeMillis() + ".3gp");

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.prepare();
            recorder.start();

        } catch (IOException e) {
            Log.e(TAG, "sdcard access error");
        }
    }

    public void stopRecording(View view) {
        stopRecBtn.setVisibility(View.GONE);
        recordBtn.setVisibility(View.VISIBLE);

        recorder.stop();
        recorder.release();

        Toast.makeText(this, "Added File " + audiofile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }

    public void play(View view) {
        playBtn.setVisibility(View.GONE);
        stopPlayingBtn.setVisibility(View.VISIBLE);

        if (audiofile != null) {
            player = MediaPlayer.create(this, Uri.fromFile(audiofile));
            player.start();
        } else {
            Toast.makeText(this, "You have not recorded yet!", Toast.LENGTH_SHORT).show();
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayingBtn.setVisibility(View.GONE);
                playBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    public void stopPlaying(View view) {
        stopPlayingBtn.setVisibility(View.GONE);
        playBtn.setVisibility(View.VISIBLE);

        player.stop();
        player.release();
    }

    public void send(View view) {
        Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show();
    }
}
