package com.ville.sentry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteRecevier extends BroadcastReceiver{

	private static final long INTERVAL_MILLIS = 50 * 60 * 1000L; // 50 min
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		AppLog.d("Sentry", "[BootCompleteRecevier.onReceive] Sentry Start");
		Intent i = new Intent(context, WorkService.class);
		i.setAction(WorkService.ACTION_LOCATION_REQ);
	    PendingIntent sender = PendingIntent.getService(context, 0, i, 0);
	    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    am.cancel(sender);
	    am.setRepeating(AlarmManager.RTC_WAKEUP, 
	    		System.currentTimeMillis() + 5000, 
	    		INTERVAL_MILLIS, sender);
	}

}
