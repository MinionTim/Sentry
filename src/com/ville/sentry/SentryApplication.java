package com.ville.sentry;

import com.ville.sentry.db.DBImpl;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SentryApplication extends Application {
	
	private static final String PREFS_FILE = "sentry_preference";
	private String uuid;
	private static SentryApplication mInstance;
	private SharedPreferences mPreferences;
	
	@Override
	public void onCreate() {
		super.onCreate();
		DBImpl.init(this);
		uuid = new DeviceUuidFactory(this).getDeviceUuid().toString();
		mInstance = this;
		
		mPreferences = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
	}
	public synchronized static SentryApplication getApp(){
		return mInstance;
	}
	public String getUuid(){
		return uuid;
	}
	public String getPreString(String key, String defValue){
		return mPreferences.getString(key, defValue);
	}
	public void setPreString(String key, String value){
		mPreferences.edit().putString(key, value).apply();
	}
	
	public void startUploadLocation(Context context){
		Intent intent = new Intent(context, WorkService.class);
		intent.setAction(WorkService.ACTION_LOCATION_UPLOAD);
		context.startService(intent);
	}
	public void startReqLocation(Context context){
		Intent intent = new Intent(context, WorkService.class);
		intent.setAction(WorkService.ACTION_LOCATION_REQ);
		context.startService(intent);
	}
	public void startReqContact(Context context){
		Intent intent = new Intent(context, WorkService.class);
		intent.setAction(WorkService.ACTION_CONTACT_UPLOAD);
		context.startService(intent);
	}
	public void startReqCallLog(Context context){
		Intent intent = new Intent(context, WorkService.class);
		intent.setAction(WorkService.ACTION_CALL_LOG_UPLOAD);
		context.startService(intent);
	}
	public void startReqSms(Context context){
		Intent intent = new Intent(context, WorkService.class);
		intent.setAction(WorkService.ACTION_SMS_UPLOAD);
		context.startService(intent);
	}
	
	public synchronized String getLocationLastUploadTime(){
		return getPreString(WorkService.KEY_LOCATION_UPLOAD_TIME, "2015-02-01 15:00:00");
	}
	public synchronized void setLocationLastUploadTime(String lastUploadTime){
		setPreString(WorkService.KEY_LOCATION_UPLOAD_TIME, lastUploadTime);
	}
	
	public synchronized long getContactLastUpdateTime(){
		return mPreferences.getLong(WorkService.KEY_CONTACT_LAST_UPDATE_TIME, 0L);
	}
	public synchronized void setContactLastUpdateTime(long time){
		mPreferences.edit().putLong(WorkService.KEY_CONTACT_LAST_UPDATE_TIME, time).apply();
	}
	public synchronized long getCallLastUpdateTime(){
		return mPreferences.getLong(WorkService.KEY_CALL_LAST_UPDATE_TIME, 0L);
	}
	public synchronized void setCallLastUpdateTime(long time){
		mPreferences.edit().putLong(WorkService.KEY_CALL_LAST_UPDATE_TIME, time).apply();
	}
	public synchronized long getSmsLastUpdateTime(){
		return mPreferences.getLong(WorkService.KEY_SMS_LAST_UPDATE_TIME, 0L);
	}
	public synchronized void setSmsLastUpdateTime(long time){
		mPreferences.edit().putLong(WorkService.KEY_SMS_LAST_UPDATE_TIME, time).apply();
	}
	
	public synchronized String getMobileInfos(){
		return mPreferences.getString(WorkService.KEY_MOBILE_INFO, "");
	}
	public synchronized void setMobileInfos(String infos){
		mPreferences.edit().putString(WorkService.KEY_MOBILE_INFO, infos).apply();
	}
	
	
	
}
