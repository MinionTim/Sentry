package com.ville.sentry;

import android.util.Log;

public class AppLog {
	
	private static final boolean DEUBG = true;
	
	public static void i(String tag, String msg){
		if(DEUBG) Log.i(tag, msg);
	}
	public static void d(String tag, String msg){
		if(DEUBG) Log.d(tag, msg);
	}
	public static void e(String tag, String msg){
		if(DEUBG) Log.e(tag, msg);
	}
	public static void w(String tag, String msg){
		if(DEUBG) Log.w(tag, msg);
	}
}
