package com.luceolab.parentaladvisor.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.ArrayMap;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.Utils;
import com.luceolab.parentaladvisor.views.DashboardActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> dataCollection = remoteMessage.getData();

        // Processing of new object's state
        if (Integer.parseInt(dataCollection.get("action")) == Constants.NEW_STATE_ACTION) {
            Bundle bundle = new Bundle();
            bundle.putString("object", dataCollection.get("object"));
            bundle.putString("object_name",  dataCollection.get("object_name"));
            bundle.putString("timestamp",  dataCollection.get("timestamp"));
            bundle.putString("object_phone",  dataCollection.get("object_phone"));
            bundle.putDouble("battery_level", Double.parseDouble(dataCollection.get("battery_level")));
            bundle.putDouble("lng", Double.parseDouble(dataCollection.get("lng")));
            bundle.putDouble("lat", Double.parseDouble(dataCollection.get("lat")));

            // Get current top activity
            Activity currentActivity = Utils.getActivity();

            // Send notification when user's top activity - Dashboard
            if (currentActivity.getClass() == DashboardActivity.class) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("action", Constants.NEW_STATE_ACTION);
                intent.putExtras(bundle);

                // Don't restart activity when it on top
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Send data to activity (receive in onNewIntent callback
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
