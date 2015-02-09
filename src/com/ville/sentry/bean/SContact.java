package com.ville.sentry.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SContact {
	public String idStr;
	public String name;
	public String numbers;
	
	@Override
	public String toString() {
		return "SContact [idStr=" + idStr + ", name=" + name + ", numbers="
				+ numbers + "]";
	}

	public JSONObject toJson(){
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("idStr", idStr);
			jObj.put("name", name);
			jObj.put("numbers", numbers);
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
