package com.appenza.beep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;


public class MainActivity extends Activity {

    private static final String TAG = "beep";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String baseUrl = "http://192.168.8.101:3000";
    public static File dir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkConnection()) {
            Toast.makeText(this, "Network is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        BroadcastReceiver registrationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Registered!", Toast.LENGTH_SHORT).show();
            }
        };

        if (checkPlayServices()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(registrationReceiver, new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }


        dir = new File(Environment.getExternalStorageDirectory(), "Beep/");
        if (!dir.mkdirs() || !dir.isDirectory())
            Log.d(TAG, "Directory exists!");

    }

    public void goToPeep(View view) {
        startActivity(new Intent(this, BeepActivity.class));
    }

    public void goToVoice(View view) {
        startActivity(new Intent(this, VoiceActivity.class));
    }


    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
