package com.ocd.dev.glassmovieplayer;

import java.util.ArrayList;
import java.util.List;

public class MovieBucket {
	private long mId;
	private String mDisplayName;
	private List<Movie> mMovies;
	
	public MovieBucket() {
		init();
	}
	
	public MovieBucket(long id, String displayName) {
		mId = id;
		mDisplayName = displayName;
		init();
	}
	
	private void init() {
		mMovies = new ArrayList<Movie>();
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
	
	public List<Movie> getMovies() {
		return mMovies;
	}
	
	public  void addMovie(Movie movie) {
		mMovies.add(movie);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof MovieBucket)){
			return false;
		}
		
		return mId == ((MovieBucket)o).mId;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(mId).hashCode();
	}
}
