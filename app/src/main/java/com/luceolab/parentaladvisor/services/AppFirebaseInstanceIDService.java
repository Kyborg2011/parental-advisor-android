package com.luceolab.parentaladvisor.services;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.Utils;

public class AppFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private String mAccessToken;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = mSharedPreferences.getString("access_token", null);

        if (mAccessToken != null) {
            Utils.sendFirebaseToken(refreshedToken, mAccessToken);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("firebase_token", refreshedToken);
            editor.putBoolean(Constants.SENT_TOKEN_TO_SERVER, true);
            editor.apply();
        }
    }
}
