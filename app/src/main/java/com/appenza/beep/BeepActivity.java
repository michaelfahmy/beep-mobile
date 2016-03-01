package com.appenza.beep;


import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import android.media.SoundPool.*;


public class BeepActivity extends AppCompatActivity {


    private static final String TAG = "beep";
    private static final String TONE = "file \'tone.wav\'" + "\n";
    private static final String AMB = "file \'amb.wav\'" + "\n";


    File srcFile;
    String filePath;


    Button recordBtn, stopRecBtn, playBtn, stopPlayingBtn, beepBtn;
    SoundPool beep;
    int toneID;
    BufferedWriter bw;


    boolean isRecording = false;
    boolean isPressed = false;
    boolean started = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beep);
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;


        recordBtn = (Button) findViewById(R.id.recordBtn);
        stopRecBtn = (Button) findViewById(R.id.stopRecBtn);
        recordBtn.setVisibility(View.VISIBLE);
        stopRecBtn.setVisibility(View.GONE);


//        playBtn = (Button) findViewById(R.id.playBtn);
//        stopPlayingBtn = (Button) findViewById(R.id.stopPlayingBtn);
//        playBtn.setVisibility(View.VISIBLE);
//        stopPlayingBtn.setVisibility(View.GONE);


        beep = setSoundPool();
        toneID = beep.load(this, R.raw.tone, 0);


        beepBtn = (Button) findViewById(R.id.beepBtn);
        beepBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        beep.play(toneID, 1, 1, 0, -1, 1);
                        if (isRecording) {
                            isPressed = true;
                            started = true;
                        } else
                            Snackbar.make(v, "Press Record!", Snackbar.LENGTH_SHORT).show();
                        break;

                    case MotionEvent.ACTION_UP:
                        beep.autoPause();
                        isPressed = false;
                        break;
                }
                return true;
            }
        });
    }


    public SoundPool setSoundPool() {
        SoundPool soundPool;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, R.raw.tone);
        }

        return soundPool;
    }


    public void startRecording(View view) {
        recordBtn.setVisibility(View.GONE);
        stopRecBtn.setVisibility(View.VISIBLE);

        isRecording = true;

        try {
            srcFile = new File(MainActivity.dir, "beep.txt");
            filePath = srcFile.getAbsolutePath();

            bw = new BufferedWriter(new FileWriter(srcFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new recording().start();
    }


    public void stopRecording(View view) {
        stopRecBtn.setVisibility(View.GONE);
        recordBtn.setVisibility(View.VISIBLE);

        isRecording = false;

        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void play(View view) {
//
//        if (!srcFile.exists())
//            Snackbar.make(view, "Record your tone first!", Snackbar.LENGTH_SHORT).show();
//        else if (isRecording)
//            Snackbar.make(view, "Still Recording!", Snackbar.LENGTH_SHORT).show();
//        else {
//            Log.d(TAG, "Playing tone...");
//            playBtn.setVisibility(View.GONE);
//            stopPlayingBtn.setVisibility(View.VISIBLE);
//        }
//
//
//    }
//
//
//    public void stopPlaying(View view) {
//        stopPlayingBtn.setVisibility(View.GONE);
//        playBtn.setVisibility(View.VISIBLE);
//
//
//    }


    public void send(View view) {
        if (isRecording) {
            Snackbar.make(view, "Still recording...", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(view, "Sending...", Snackbar.LENGTH_SHORT).show();

        try {
            String uploadID = new MultipartUploadRequest(this, MainActivity.baseUrl + "/receiver/receive")
                    .addFileToUpload(filePath, "file", srcFile.getName(), "plain/text")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .addParameter("type", "BEEP")
                    .startUpload();
            Log.i(TAG, "Upload ID: " + uploadID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class recording extends Thread {
        @Override
        public void run() {
            try {
                while (isRecording && started) {
                    sleep(200);
                    if (isPressed) {
                        bw.write(TONE);
                    } else {
                        bw.write(AMB);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}