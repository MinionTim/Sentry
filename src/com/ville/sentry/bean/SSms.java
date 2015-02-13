package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SSms {
	
	public long date;
	public String number;
	public String name;
	public String body;
	public String type;
	
	@Override
	public String toString() {
		return "SSms [date=" + date + ", number=" + number + ", name=" + name
				+ ", body=" + body + ", type=" + type + "]";
	}

	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("number", number);
			jObj.put("name", name);
			jObj.put("date", date);
			jObj.put("body", body);
			jObj.put("type", type);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jObj;
	}
	
	public static String buildJsonStr(ArrayList<SSms> list){
		JSONArray array = new JSONArray();
		for(SSms sms : list){
			array.put(sms.toJson());
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
			return "Rec";
		case 2:
			return "Sent";
		default:
			return "N/A";
		}
	}
}
