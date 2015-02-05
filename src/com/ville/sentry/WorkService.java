package com.ville.sentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ville.sentry.bean.SLocation;
import com.ville.sentry.bean.WebResult;
import com.ville.sentry.db.DBDao;
import com.ville.sentry.db.DBImpl;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

public class WorkService extends Service {

	public static final String ACTION_LOCATION_REQ 		= "sentry.action.loc.req";
	public static final String ACTION_LOCATION_UPLOAD 	= "sentry.action.loc.upload";
	
	public static final String KEY_LOCATION_UPLOAD_TIME = "sentry.key.loc.upload.time";
	
	private LocationClient mClient;
	private MyLocationListener mLocationListener;
	private static final String TAG = "WorkService";
	
	public WorkService() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mClient = new LocationClient(this.getApplicationContext());
		mLocationListener = new MyLocationListener();
		mClient.registerLocationListener(mLocationListener);
		
		initLocationParams();
	}
	
	private void initLocationParams(){
		//定位模式
		LocationMode mode = LocationMode.Hight_Accuracy;
		//坐标类型
		String coor = BDGeofence.COORD_TYPE_GCJ;
		//扫描的时间间隔
		int span=10000;
		
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(mode);
		option.setCoorType(coor);
		// setScanSpan(): 当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。
		// 每调用一次requestLocation( )，定位SDK会发起一次定位。请求定位与监听结果一一对应。
		//option.setScanSpan(span);
		option.setIsNeedAddress(true); //是否解析为实际地址
		mClient.setLocOption(option);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "repeating alarm", Toast.LENGTH_SHORT).show();
		AppLog.d(TAG, "[onStartCommand] " + new Date().toGMTString() + ", " + intent.getAction());
		String action = intent.getAction();
		if(ACTION_LOCATION_REQ.equals(action)){
			mClient.start();
			mClient.requestLocation();
		}else if (ACTION_LOCATION_UPLOAD.equals(action)){
			new UploadTask().execute();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mClient.stop();
		mClient.unRegisterLocationListener(mLocationListener);
	}
	
	/**
	 * 实现实位回调监听
	 */
	private class MyLocationListener implements BDLocationListener {
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			StringBuffer sb = new StringBuffer(256);
			sb.append("Now: " + new Date() + "\n");
			sb.append("时间: " + location.getTime()).append("\n");
			sb.append("ErrorCode : " + location.getLocType()).append("\n");
			sb.append("纬度 : " + location.getLatitude()).append("\n");
			sb.append("精度 : " + location.getLongitude()).append("\n");
			sb.append("精度(m) : " + location.getRadius()).append("\n");
			sb.append("地址: " + location.getAddrStr()).append("\n");
			int locType = location.getLocType();
			String locTypeStr = null;
			if (locType == BDLocation.TypeGpsLocation){
				locTypeStr = "GPS定位";
				sb.append("类型：GPS定位").append("\n");
				sb.append("速度: " + location.getSpeed()).append("\n");
				sb.append("卫星数: " + location.getSatelliteNumber()).append("\n");
			} else if (locType == BDLocation.TypeNetWorkLocation){
				locTypeStr = "网络定位-" + parseOperatorCode(location.getOperators());
				sb.append("类型：网络定位").append("\n");
				sb.append("运营商 : " + parseOperatorCode(location.getOperators())).append("\n");
			} else if (locType == BDLocation.TypeOffLineLocation){
				locTypeStr = "离线定位";
				sb.append("类型：离线定位").append("\n");
			} else {
				sb.append("定位失败").append("\n");
			}
			if(locType == BDLocation.TypeGpsLocation
					|| locType == BDLocation.TypeNetWorkLocation
					|| locType == BDLocation.TypeOffLineLocation){
				SLocation loc = new SLocation();
				loc.time = location.getTime();
				loc.addr = location.getAddrStr();
				loc.city = location.getCity();
				loc.latitude = location.getLatitude();
				loc.longitude = location.getLongitude();
				loc.province = location.getProvince();
				loc.locType = locTypeStr;
				loc.radius = location.getRadius();
				DBDao db = DBImpl.getInstance();
				db.insertLocation(loc);
				AppLog.d(TAG, "insertIntoLocation: <" + loc.addr + ">");
			}else {
				AppLog.d(TAG, "request location FAILED!!");
			}
			
			if(mClient.isStarted()){
				mClient.stop();
			}
		}
	}
	
	private String getLatestFromServer(){
		String latest = NetUtil.doGet(Common.URL_LOCATION_LATEST, null);
		if(TextUtils.isEmpty(latest)){
			latest = "2015-01-01 01:00:00";
		}
		return latest;
	}
	private String getLatestFromLocal(){
		DBDao db = DBImpl.getInstance();
		String latest = db.getLatestTime();
		if(TextUtils.isEmpty(latest)){
			latest = "2015-01-01 01:00:00";
		}
		return latest;
	}
	private boolean uploadLocations(ArrayList<SLocation> locations){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("locations", SLocation.buildJsonStr(locations));
		map.put("uuid", SentryApplication.getApp().getUuid());
		map.put("model", android.os.Build.MODEL);
		String responce = NetUtil.doPost(Common.URL_LOCATION_UPLOAD, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	private boolean upload(){
		String serverLatest = getLatestFromServer();
		ArrayList<SLocation> locations = DBImpl.getInstance().getLocations(serverLatest);
		AppLog.d(TAG, "size is " + locations.size() + ", Since Sever " + serverLatest);
		boolean success = false;
		if(locations != null && locations.size() > 0){
			success = uploadLocations(locations);
			AppLog.d(TAG, "uploadLocations success ? " + success);
		} else {
			AppLog.d(TAG, "No new locations Found!!");
		}
		return success;
	}
	
	private String parseOperatorCode(int code){
		switch (code) {
		case BDLocation.OPERATORS_TYPE_MOBILE:
			return "中国移动";
		case BDLocation.OPERATORS_TYPE_TELECOMU:
			return "中国电信";
		case BDLocation.OPERATORS_TYPE_UNICOM:
			return "中国联通";
		case BDLocation.OPERATORS_TYPE_UNKONW:
		default:
			return "未知/WiFi";
		}
	}
	
	class UploadTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return upload();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
				String lastest = getLatestFromLocal();
				SentryApplication.getApp().setPreString(KEY_LOCATION_UPLOAD_TIME, lastest);
			}
		}
		
	}

}
