package com.appenza.beep;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "beep/GcmListener";
    public static final String NEW_FILE = "NEW_FILE";
    public static final String NOTIFY = "NOTIFY";

    File soundFile;
    String message;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);

        String type = data.getString("type");
        String soundName = data.getString("sound");
        message = data.getString("message");
        soundFile = new File(MainActivity.dir + File.separator + soundName);

        assert type != null;
        switch (type) {
            case NEW_FILE:
                Log.i(TAG, "Downloading new file...");
                new DownloadFile().execute(data.getString("downloadUrl"));
                break;
            case NOTIFY:
                Log.i(TAG, "Notify!");
                sendNotification();
                break;
        }

    }

    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentText(message)
                .setSound(Uri.fromFile(soundFile))
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }


    private class DownloadFile extends AsyncTask<String, Void, Void> {

        private static final String LOG_TAG = "beep/GcmListener";

        @Override
        protected Void doInBackground(String... params) {
            try {
                Log.d(LOG_TAG, params[0]);
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream is = url.openStream();
                FileOutputStream output = new FileOutputStream(soundFile);

                int count;
                byte data[] = new byte[1024];
                while ((count = is.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                is.close();
                output.flush();
                output.close();
                Log.i(LOG_TAG, "File Downloaded! xD");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error downloading file");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sendNotification();
        }

    }
}
