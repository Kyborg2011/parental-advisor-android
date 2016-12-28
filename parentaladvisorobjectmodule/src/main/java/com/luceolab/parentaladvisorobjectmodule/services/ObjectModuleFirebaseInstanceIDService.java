package com.luceolab.parentaladvisorobjectmodule.services;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.luceolab.parentaladvisorobjectmodule.Constants;

public class ObjectModuleFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private SharedPreferences mSharedPreferences;

    @Override
    public void onTokenRefresh() {

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("firebase_token", refreshedToken);
        editor.apply();
    }
}
