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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "beep/GcmListener";

    File soundFile;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        String downloadUrl = data.getString("downloadUrl");
        String soundname = data.getString("soundName");

        soundFile = new File(MainActivity.dir + File.separator + soundname);

        if (downloadUrl != null) {
            new DownloadFile().execute(downloadUrl, soundname);
        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
    }

    private void sendNotification(String message) {
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

        private static final String LOG_TAG = "DownloadFile";

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(soundFile);

                int count;
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                Log.d(LOG_TAG, "File Downloaded! xD");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error downloading file");
                e.printStackTrace();
            }
            return null;
        }
    }
}
