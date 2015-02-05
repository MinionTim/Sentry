package com.ville.sentry.db;

public class DBContract {
	
	interface TableLocation {
		String _ID = "_id";
		String TIME = "time";
		String LATITUDE = "latitude";
		String LONGITUDE = "longitude";
		String RADIUS = "radius";
		String ADDR = "addr";
		String PROVINCE = "province";
		String CITY = "city";
		String LOC_TYPE = "locType";
	}
	
	interface Tables {
		String TABLE_LOCATION = "t_location";
	}
}
