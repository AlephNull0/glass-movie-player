package com.ocd.dev.glassmovieplayer;

import android.app.Application;

public class GlassApplication extends Application {
	private  SoundManager mSoundManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mSoundManager = new SoundManager(this);
	}
	
	public SoundManager getSoundManager() {
		return mSoundManager;
	}
}
