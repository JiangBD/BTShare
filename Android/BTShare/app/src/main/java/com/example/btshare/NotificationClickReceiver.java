package com.example.btshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class NotificationClickReceiver extends BroadcastReceiver {
    public final static int NOTIFICATION_ID = 7749;
    @Override
    public void onReceive(Context context, Intent intent) {
        ProcessorController.cancelTransferring();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}