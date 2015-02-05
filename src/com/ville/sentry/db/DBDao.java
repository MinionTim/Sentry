package com.ville.sentry.db;

import java.util.ArrayList;
import com.ville.sentry.bean.SLocation;

public interface DBDao {
	
	boolean insertLocation(SLocation loc);
	boolean delLocation(String time);
	ArrayList<SLocation> getAllLocations();
	ArrayList<SLocation> getLocations(String sinceTime);
	String getLatestTime();
}
