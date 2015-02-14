package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SCall {
	
	public String number;
	public String name;
	public long _date;
	public long duration;
	public String type;
	
	@Override
	public String toString() {
		return "SCall [number=" + number + ", name=" + name + ", _date=" + _date
				+ ", duration=" + duration + ", type=" + type + "]";
	}

	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("number", number);
			jObj.put("name", name);
			jObj.put("_date", _date);
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
	
	/**
	 * 
	 * @param type query from db
	 * @return
	 */
	public static String parseType(int type){
		switch (type) {
		case 1:
			return "In";
		case 2:
			return "Out";
		case 3:
			return "Miss";
		}
		return "N/A";
	}
}
