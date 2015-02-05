package com.ville.sentry.db;

import com.ville.sentry.AppLog;
import com.ville.sentry.db.DBContract.TableLocation;
import com.ville.sentry.db.DBContract.Tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "sentry_app.db";
	
    private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DBHelper";
	

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		/**
		 * values.put(TableLocation.TIME, loc.time);
		values.put(TableLocation.LATITUDE, loc.latitude);
		values.put(TableLocation.LONGITUDE, loc.longitude);
		values.put(TableLocation.RADIUS, loc.radius);
		values.put(TableLocation.ADDR, loc.addr);
		values.put(TableLocation.PROVINCE, loc.province);
		values.put(TableLocation.CITY, loc.city);
		values.put(TableLocation.LOC_TYPE, loc.locType);
		 */
		 db.execSQL("CREATE TABLE " + Tables.TABLE_LOCATION + " ("
				 + TableLocation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                 + TableLocation.TIME + " TEXT UNIQUE NOT NULL,"
                 + TableLocation.LATITUDE + " DOUBLE,"
                 + TableLocation.LONGITUDE + " DOUBLE,"
                 + TableLocation.RADIUS + " DOUBLE,"
                 + TableLocation.ADDR + " TEXT,"
                 + TableLocation.PROVINCE + " TEXT,"
                 + TableLocation.CITY + " TEXT,"
                 + TableLocation.LOC_TYPE + " TEXT, "
                 + "UNIQUE (" + TableLocation.TIME + ")"
                 + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		 // Logs that the database is being upgraded
        AppLog.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Kills the table and existing data
        db.execSQL("DROP TABLE IF EXISTS " + Tables.TABLE_LOCATION);

        // Recreates the database with a new version
        onCreate(db);
	}

}
