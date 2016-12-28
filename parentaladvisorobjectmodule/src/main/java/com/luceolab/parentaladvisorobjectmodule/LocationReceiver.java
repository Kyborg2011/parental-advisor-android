package com.luceolab.parentaladvisorobjectmodule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.luceolab.parentaladvisorobjectmodule.restclient.AccountsService;
import com.luceolab.parentaladvisorobjectmodule.restclient.ServiceGenerator;
import com.luceolab.parentaladvisorobjectmodule.restclient.models.State;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;
    private Context mContext;
    private String mPhoneNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);

            SharedPreferences mPref = context.getSharedPreferences(Constants.PREFS_NAME, 0);
            mPhoneNumber = mPref.getString("phone", null);

            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
            showNotification(context, this.mLocationResult.toString());
            send(mLocationResult.getLastLocation());
        }
    }

    private float getBatteryLevel() {
        Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    private void send(Location loc) {
        if (mPhoneNumber != null) {
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();

            // Creating of retrofit service
            AccountsService loginService =
                    ServiceGenerator.createService(AccountsService.class);
            Call<State> call = loginService.sendCurrentState(
                    mPhoneNumber,
                    lng,
                    lat,
                    getBatteryLevel()
            );

            call.enqueue(new Callback<State>() {
                @Override
                public void onResponse(Call<State> call, Response<State> response) {

                }

                @Override
                public void onFailure(Call<State> call, Throwable t) {
                }
            });

            Log.d("LocationPA", "lat: " + lat);
        }
    }

    private void showNotification(Context context, String res) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_name)
                        .setContentTitle("Location manager")
                        .setContentText("Phone number " + mPhoneNumber);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
