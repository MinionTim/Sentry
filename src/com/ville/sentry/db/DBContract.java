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
	
	interface TableContact {
		String _ID = "_id";
		String ID_STR = "idStr";
		String NAME = "name";
		String NUMBERS = "numbers";
	}
	
	interface TableCall {
		String _ID = "_id";
		String NUMBER = "number";
		String NAME = "name";
		String DATE = "date";
		String DURATION = "duration";
		String TYPE = "type";
	}
	
	
	interface Tables {
		String TABLE_LOCATION 	= "t_location";
		String TABLE_CONTACT 	= "t_contact";
		String TABLE_CALL 		= "t_call";
	}
}
