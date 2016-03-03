package com.appenza.beep;


import android.app.ProgressDialog;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import java.util.ArrayList;


import android.media.SoundPool.*;


public class BeepActivity extends AppCompatActivity {


    private static final String TAG = "beep";
    private static final String TONE10 = "file \'tone10_1000.wav\'" + "\n";
    private static final String TONE20 = "file \'tone20_1000.wav\'" + "\n";
    private static final String TONE50 = "file \'tone50_1000.wav\'" + "\n";
    private static final String TONE100 = "file \'tone100_1000.wav\'" + "\n";
    private static final String TONE200 = "file \'tone200_1000.wav\'" + "\n";

    private static final String AMB10 = "file \'amb10.wav\'" + "\n";
    private static final String AMB20 = "file \'amb20.wav\'" + "\n";
    private static final String AMB50 = "file \'amb50.wav\'" + "\n";
    private static final String AMB100 = "file \'amb100.wav\'" + "\n";
    private static final String AMB200 = "file \'amb200.wav\'" + "\n";


    File srcFile;
    String filePath;


    Button recordBtn, stopRecBtn, beepBtn;
    int toneID;
    SoundPool beep;
    long ts_tone, ts_amb;
    ArrayList<Pair<Long, Boolean>> data;


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


        beep = setSoundPool();
        toneID = beep.load(this, R.raw.tone200_1000, 0);

        beepBtn = (Button) findViewById(R.id.beepBtn);
        beepBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        beep.play(toneID, 1, 1, 0, -1, 1);
                        if (isRecording) {
                            started = true;
                            isPressed = true;

                            ts_tone = System.currentTimeMillis();
                            ts_amb = System.currentTimeMillis() - ts_amb;
                            data.add(Pair.create(ts_amb, false));
                            Log.d(TAG, "Amb - " + ts_amb);
                        } else
                            Snackbar.make(v, "Press Record!", Snackbar.LENGTH_SHORT).show();
                        break;

                    case MotionEvent.ACTION_UP:
                        beep.autoPause();
                        if (isRecording) {
                            isPressed = false;

                            ts_amb = System.currentTimeMillis();
                            ts_tone = System.currentTimeMillis() - ts_tone;
                            data.add(Pair.create(ts_tone, true));
                            Log.d(TAG, "Tone - " + ts_tone);
                        }
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
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, R.raw.tone200_1000);
        }
        return soundPool;
    }


    public void startRecording(View view) {
        recordBtn.setVisibility(View.GONE);
        stopRecBtn.setVisibility(View.VISIBLE);

        isRecording = true;

        data = new ArrayList<>();
    }


    public void stopRecording(View view) {
        stopRecBtn.setVisibility(View.GONE);
        recordBtn.setVisibility(View.VISIBLE);

        isRecording = false;

        new SavingFile().execute();
    }


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




    class SavingFile extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        BufferedWriter bw;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(BeepActivity.this);
            progress.setMessage("Saving...");
            progress.show();

            srcFile = new File(MainActivity.dir, "beep.txt");
            filePath = srcFile.getAbsolutePath();

            try {
                bw = new BufferedWriter(new FileWriter(srcFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 1; i < data.size(); ++i) {
                    long sprint = data.get(i).first;
                    boolean f = data.get(i).second;
                    if (f) {
                        while (sprint >= 200) {
                            bw.write(TONE200);
                            sprint -= 200;
                        }
                        while (sprint >= 100) {
                            bw.write(TONE100);
                            sprint -= 100;
                        }
                        while (sprint >= 50) {
                            bw.write(TONE50);
                            sprint -= 50;
                        }
                        while (sprint >= 20) {
                            bw.write(TONE20);
                            sprint -= 20;
                        }
                        while (sprint >= 10) {
                            bw.write(TONE10);
                            sprint -= 10;
                        }
                    } else {
                        while (sprint >= 200) {
                            bw.write(AMB200);
                            sprint -= 200;
                        }
                        while (sprint >= 100) {
                            bw.write(AMB100);
                            sprint -= 100;
                        }
                        while (sprint >= 50) {
                            bw.write(AMB50);
                            sprint -= 50;
                        }
                        while (sprint >= 20) {
                            bw.write(AMB20);
                            sprint -= 20;
                        }
                        while (sprint >= 10) {
                            bw.write(AMB10);
                            sprint -= 10;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            progress.dismiss();
        }
    }
}