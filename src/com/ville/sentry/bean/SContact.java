package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SContact {
	public String _idStr;
	public String name;
	public String number;
	
	@Override
	public String toString() {
		return "SContact [_idStr=" + _idStr + ", name=" + name + ", number="
				+ number + "]";
	}

	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("_idStr", _idStr);
			jObj.put("name", name);
			jObj.put("number", number);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jObj;
	}
	
	public static String buildJsonStr(ArrayList<SContact> list){
		JSONArray array = new JSONArray();
		for(SContact contact : list){
			array.put(contact.toJson());
		}
		return array.toString();
	}
}
