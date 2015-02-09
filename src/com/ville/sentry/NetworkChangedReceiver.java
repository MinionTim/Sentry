package com.ville.sentry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NetworkChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "Sentry/NetworkChangedReceiver";
	private static final long TIME_INTEVEL = 60 * 60 * 1000;  // 1 hour
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean isConntected = Utility.isConnected(context);
		Toast.makeText(context, "isConntected ? " + isConntected, Toast.LENGTH_SHORT).show();
		AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] isConntected ? " + isConntected);
		if(!isConntected){
			return;
		}
		/*
		Date now = new Date();
		if(Utility.isValidUploadWindow(now)){
			String str = SentryApplication.getApp().getLastUploadTime();
			Date last = null;
			try {
				last = DATE_FORMAT.parse(str);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(last != null && !Utility.isValidUploadWindow(last)){
				AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] startUploadLocation");
				SentryApplication.getApp().startUploadLocation(context);
			}
		}
		*/
		Date now = new Date();
		Date last = null;
		String str = SentryApplication.getApp().getLocationLastUploadTime();
		try {
			last = DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			AppLog.e(TAG, "Error: " + e.getMessage());
			e.printStackTrace();
		}
		if(last != null){
			AppLog.d(TAG, "now: " + now);
			AppLog.d(TAG, "last: " + last);
			AppLog.d(TAG, "T: " + (now.getTime() - last.getTime()) / 1000);
		}
		
		if(last != null && (now.getTime() - last.getTime()) > TIME_INTEVEL){
			AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] startReqLocation");
			SentryApplication.getApp().startReqLocation(context);
		}
		
	}
}
