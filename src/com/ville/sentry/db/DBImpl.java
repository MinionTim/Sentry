package com.ville.sentry.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ville.sentry.AppLog;
import com.ville.sentry.bean.SLocation;
import com.ville.sentry.db.DBContract.TableLocation;
import com.ville.sentry.db.DBContract.Tables;

public class DBImpl implements DBDao {
	
	private static final String TAG = "DBImpl";
	private static DBImpl mInstance = null;
	private Context mContext;
	private DBHelper mDBHelper;
	private DBImpl(Context context){
		mContext = context;
		mDBHelper = new DBHelper(mContext);
	}
	public static void init(Context context){
		if(mInstance == null){
			mInstance = new DBImpl(context);
		}
	}
	public synchronized static DBImpl getInstance(){
		if(mInstance == null){
			AppLog.e(TAG, "You Have to INIT first!!");
		}
		return mInstance;
	}
	
	@Override
	public boolean insertLocation(SLocation loc) {
		// TODO Auto-generated method stub
		//http://stackoverflow.com/questions/25996040/mysql-insert-where-not-exists
//		INSERT INTO USER (name,email)
//		SELECT 'John','john@mmm.com'
//		WHERE NOT EXISTS
//		    (SELECT id FROM USER WHERE email = 'john@mmm.com')
		
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String sql = "INSERT OR IGNORE INTO " + Tables.TABLE_LOCATION + "(" + 
				TableLocation.TIME + ", " + 
				TableLocation.ADDR + ", " + 
				TableLocation.CITY + ", " + 
				TableLocation.LATITUDE + ", " + 
				TableLocation.LONGITUDE + ", " + 
				TableLocation.LOC_TYPE + ", " + 
				TableLocation.PROVINCE + ", " + 
				TableLocation.RADIUS + ") " + 
				"VALUES( ?, ?, ?, ?, ?, ?, ?, ? )";
		
		db.execSQL(sql, new Object[]{loc.time, loc.addr, loc.city, loc.latitude,
				loc.longitude, loc.locType, loc.province, loc.radius});
		db.close();
		return true;
	}
	@Override
	public boolean delLocation(String time) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public ArrayList<SLocation> getAllLocations() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		ArrayList<SLocation> list = new ArrayList<SLocation>();
		Cursor cursor = db.query(Tables.TABLE_LOCATION, new String[] {
				TableLocation.TIME, TableLocation.ADDR, TableLocation.CITY,
				TableLocation.PROVINCE, TableLocation.LATITUDE,
				TableLocation.LONGITUDE, TableLocation.LOC_TYPE,
				TableLocation.RADIUS },
        		null,
        		null,
        		null, null, null); 
        while (cursor.moveToNext()) {
        	SLocation loc = new SLocation();
        	loc.time = cursor.getString(cursor.getColumnIndex(TableLocation.TIME));
            loc.addr = cursor.getString(cursor.getColumnIndex(TableLocation.ADDR));
            loc.city = cursor.getString(cursor.getColumnIndex(TableLocation.CITY));
            loc.province = cursor.getString(cursor.getColumnIndex(TableLocation.PROVINCE));
            loc.latitude = cursor.getDouble(cursor.getColumnIndex(TableLocation.LATITUDE));
            loc.longitude = cursor.getDouble(cursor.getColumnIndex(TableLocation.LONGITUDE));
            loc.locType = cursor.getString(cursor.getColumnIndex(TableLocation.LOC_TYPE));
            loc.radius = cursor.getDouble(cursor.getColumnIndex(TableLocation.RADIUS));
            list.add(loc);
        }
        db.close();
		return list;
	}
	@Override
	public String getLatestTime() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		// "SELECT time FROM t_location ORDER BY time DESC LIMIT 1";
		// 第一个参数String：表名  
        // 第二个参数String[]:要查询的列名  
        // 第三个参数String：查询条件  
        // 第四个参数String[]：查询条件的参数  
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制  
        // 第七个参数String：对查询的结果进行排序
		String orderBy = TableLocation.TIME + " DESC ";
		String limit = "1";
		String[] columns = new String[]{TableLocation.TIME};
		Cursor cursor = db.query(Tables.TABLE_LOCATION, columns, null, 
				null, null, null, orderBy, limit);
		String latest = null;
		if(cursor.getCount() == 1 && cursor.moveToNext()){
			latest =  cursor.getString(cursor.getColumnIndex(TableLocation.TIME));
		}
		cursor.close();
		db.close();
		return latest;
	}
	
	@Override
	public ArrayList<SLocation> getLocations(String sinceTime) {
		// TODO Auto-generated method stub
		// select * from t_location where datetime(time) > datetime('2015-02-01 15:00');
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		ArrayList<SLocation> list = new ArrayList<SLocation>();
		// 第一个参数String：表名  
        // 第二个参数String[]:要查询的列名  
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数  
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制  
        // 第七个参数String：对查询的结果进行排序
		String selection = "datetime(" + TableLocation.TIME + ") > datetime('" + sinceTime + "')";
		Cursor cursor = db.query(Tables.TABLE_LOCATION, new String[] {
				TableLocation.TIME, TableLocation.ADDR, TableLocation.CITY,
				TableLocation.PROVINCE, TableLocation.LATITUDE,
				TableLocation.LONGITUDE, TableLocation.LOC_TYPE,
				TableLocation.RADIUS },
				selection,
				null,
        		null, null, null);
        while (cursor.moveToNext()) {
        	SLocation loc = new SLocation();
        	loc.time = cursor.getString(cursor.getColumnIndex(TableLocation.TIME));
            loc.addr = cursor.getString(cursor.getColumnIndex(TableLocation.ADDR));
            loc.city = cursor.getString(cursor.getColumnIndex(TableLocation.CITY));
            loc.province = cursor.getString(cursor.getColumnIndex(TableLocation.PROVINCE));
            loc.latitude = cursor.getDouble(cursor.getColumnIndex(TableLocation.LATITUDE));
            loc.longitude = cursor.getDouble(cursor.getColumnIndex(TableLocation.LONGITUDE));
            loc.locType = cursor.getString(cursor.getColumnIndex(TableLocation.LOC_TYPE));
            loc.radius = cursor.getDouble(cursor.getColumnIndex(TableLocation.RADIUS));
            list.add(loc);
        }
        cursor.close();
        db.close();
		return list;
	}

	
//	@Override
//	public boolean addUser(String name, String pwd, String phone) {
//		// TODO Auto-generated method stub
//		SQLiteDatabase db = mDBHelper.getWritableDatabase();
//		ContentValues values = new ContentValues();
//		values.put(TableUser.NAME, name);
//		values.put(TableUser.PASSWORD, pwd);
//		values.put(TableUser.PHONE, phone);
//		long id = db.insert(Tables.TABLE_USER, null, values);
//		return (id != -1);
//	}
//	@Override
//	public boolean delUser(String name) {
//		// TODO Auto-generated method stub
//		SQLiteDatabase db = mDBHelper.getWritableDatabase();
//		String whereClause = TableUser.NAME + "=?";
//		String[] whereArgs = new String[]{name};
//		db.delete(Tables.TABLE_USER, whereClause, whereArgs);
//		return false;
//	}
	
	

}
