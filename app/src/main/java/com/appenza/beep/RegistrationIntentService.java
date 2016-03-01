package com.appenza.beep;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "beep/Register";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            if (!sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false)) {
                // get device_token
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                Log.i(TAG, "Registering... with: " + token);
                sendRegistrationToServer(token);

                // Notify UI that registration has completed, so the progress indicator can be hidden.
                Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
            } else {
                Log.i(TAG, "Registered before!");
            }


        } catch (Exception e) {
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();

            Log.e(TAG, "Failed to register", e);
            e.printStackTrace();
        }

    }


    private void sendRegistrationToServer(String token) throws Exception {
        URL url = new URL(MainActivity.baseUrl + "/users/signup");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();

        JSONObject json = new JSONObject()
                .put("user", new JSONObject()
                        .put("name", "dev")
                        .put("email", "developerappenza@gmail.com")
                        .put("device_token", token));

        String msg = json.toString(2);
        Log.d(TAG, msg);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(json.toString(2));
        writer.close();
        os.close();

        int respcode = connection.getResponseCode();
        String respmsg = connection.getResponseMessage();
        Log.i(TAG, "response code: " + respcode + "\nresponse msg: " + respmsg);


        if (respcode == HttpURLConnection.HTTP_OK) {
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
            Log.i(TAG, "Registered Successfully!");
        } else {
            Log.e(TAG, respmsg);
        }

    }

}