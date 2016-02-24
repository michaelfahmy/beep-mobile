package com.appenza.beep;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;


public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "beep/tokenListener";

    @Override
    public void onTokenRefresh() {
        Log.i(TAG, "Token Refreshed");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
