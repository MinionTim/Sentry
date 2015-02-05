package com.ville.sentry;

import com.ville.sentry.db.DBImpl;
import android.app.Application;
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
	public void getPreString(String key, String defValue){
		mPreferences.getString(key, defValue);
	}
	public void setPreString(String key, String value){
		mPreferences.edit().putString(key, value).apply();
	}
	
	
}
