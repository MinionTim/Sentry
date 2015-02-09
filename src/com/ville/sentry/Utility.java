package com.ville.sentry;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static int getNetType(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return networkInfo.getType();
        }
        return -1;
    }

    public static boolean isGprs(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 时间上传的时间窗口
     * 当满足以下条件之一即为有效窗口时间： <br>
     * 1.lastUploadDate不是当天的时间<br>
     * 2.lastUploadDate为当天时间，同时处于下述四个时间段之一:<br>
     *  7:00  - 9:00
	 	11:30 - 13:00
	 	17:00 - 19:00
		20:30 - 22:00
		@param last 上次的上传时间
     * @return
     */
    public static boolean isValidUploadWindow(Date date){
    	Calendar calendar = Calendar.getInstance(Locale.US);
    	calendar.setTime(date);
    	int hour = calendar.get(Calendar.HOUR_OF_DAY);
    	if(hour >= 7 && hour <= 9) {
    		return true;
    	}else if(hour >= 11 && hour <= 13){
    		return true;
    	}else if (hour >= 17 && hour <= 19){
    		return true;
    	}else if (hour >= 20 && hour <= 22){
    		return true;
    	}
    	
    	return false;
    }
    
    public static boolean isSameDay(Date d1, Date d2){
    	String d1Str = DATE_FORMAT.format(d1);
    	String d2Str = DATE_FORMAT.format(d2);
    	return d1Str.equals(d2Str);
    }
    
    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String genContactId(String name, String id) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update((name + id).getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(name.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
}
