package com.ville.sentry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;


public class LocationActivity extends Activity{

	private static final String TAG = "LocationActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		
		Intent intent = new Intent(LocationActivity.this, WorkService.class);
		intent.setAction(WorkService.ACTION_LOCATION_REQ);
	    PendingIntent sender = PendingIntent.getService(LocationActivity.this, 0, intent, 0);
	    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
	    am.cancel(sender);
	    am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, 2 * 60 * 1000, sender);
	    AppLog.d(TAG, "AlarmManager setRepeating");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		Intent intent = new Intent(LocationActivity.this, WorkService.class);
//		intent.setAction(WorkService.ACTION_LOCATION_REQ);
//	    PendingIntent sender = PendingIntent.getService(LocationActivity.this, 0, intent, 0);
//	    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//	    am.cancel(sender);
	}

	
	/*
	 * 获取error code：
	public int getLocType ( )
	返回值：
	
	61 ： GPS定位结果
	62 ： 扫描整合定位依据失败。此时定位结果无效。
	63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
	65 ： 定位缓存的结果。
	66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
	67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
	68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
	161： 表示网络定位结果
	162~167： 服务端定位失败
	
	
	Hight_Accuracy
	高精度定位模式下，会同时使用GPS、Wifi和基站定位，返回的是当前条件下精度最好的定位结果</string>
    Battery_Saving
    低功耗定位模式下，仅使用网络定位即Wifi和基站定位，返回的是当前条件下精度最好的网络定位结果</string>
   	Device_Sensors
    仅用设备定位模式下，只使用用户的GPS进行定位。这个模式下，由于GPS芯片锁定需要时间，首次定位速度会需要一定的时间</string>
	 
	 * */
}

