package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SCall {
	
	public String number;
	public String name;
	public long date;
	public long duration;
	public int type;
	
	
	
	@Override
	public String toString() {
		return "SCall [number=" + number + ", name=" + name + ", date=" + date
				+ ", duration=" + duration + ", type=" + type + "]";
	}

	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("number", number);
			jObj.put("name", name);
			jObj.put("date", date);
			jObj.put("duration", duration);
			jObj.put("type", type);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jObj;
	}
	
	public static String buildJsonStr(ArrayList<SCall> list){
		JSONArray array = new JSONArray();
		for(SCall call : list){
			array.put(call.toJson());
		}
		return array.toString();
	}
}
