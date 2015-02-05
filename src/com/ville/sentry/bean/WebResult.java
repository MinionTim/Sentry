package com.ville.sentry.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class WebResult {
	public boolean success;
	public String detail;
	
	private WebResult(String jsonStr) {
		// TODO Auto-generated constructor stub
		try {
			JSONObject jObject = new JSONObject(jsonStr);
			success = jObject.optBoolean("success", false);
			detail = jObject.optString("detail", "");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static WebResult parseByJson(String json){
		if(json == null || "".equals(json)){
			return null;
		}
		return new WebResult(json);
	}
	
}
