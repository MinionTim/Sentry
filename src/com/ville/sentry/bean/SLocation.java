package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SLocation {
	/** 当前定位时间 */
	public String time;
	
	/** 纬度 */
	public double latitude;
	
	/** 经度 */
	public double longitude;
	
	/** 精度，单位：米 */
	public double radius;
	
	/** 详细地址 */
	public String addr;
	
	public String province;
	
	public String city;
	
	/** 定位类型 <br>
	 * GPS定位 ， 网络定位 或 离线定位 
	 * */
	public String locType;

	@Override
	public String toString() {
		return "SLocation [time=" + time + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", radius=" + radius + ", addr="
				+ addr + ", province=" + province + ", city=" + city
				+ ", locType=" + locType + "]";
	}
	
	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("time", time);
			jObj.put("latitude", latitude);
			jObj.put("longitude", longitude);
			jObj.put("radius", radius);
			jObj.put("addr", addr);
			jObj.put("province", province);
			jObj.put("city", city);
			jObj.put("locType", locType);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jObj;
	}
	
	public static String buildJsonStr(ArrayList<SLocation> list){
		JSONArray array = new JSONArray();
		for(SLocation loc : list){
			array.put(loc.toJson());
		}
		return array.toString();
	}

}
