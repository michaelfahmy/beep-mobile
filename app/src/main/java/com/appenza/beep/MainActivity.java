package com.appenza.beep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;


public class MainActivity extends Activity {

    private static final String TAG = "beep";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dir = new File(Environment.getExternalStorageDirectory(), "Beep/");
        if (!dir.mkdirs() || !dir.isDirectory())
            Log.d(TAG, "Directory exists!");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void goToPeep(View view) {
        startActivity(new Intent(this, BeepActivity.class));
    }

    public void goToVoice(View view) {
        startActivity(new Intent(this, VoiceActivity.class));
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
