package com.ocd.dev.glassmovieplayer;


public class Movie {
	private long mId;
	private String mDisplayName;
	
	public Movie() {}
	
	public Movie(long id, String displayName) {
		mId = id;
		mDisplayName = displayName;
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		mId = id;
	}
	
	public String getDisplayName() {
		return mDisplayName;
	}
	
	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
	}
	
}
