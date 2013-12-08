package com.ocd.dev.glassmovieplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	private SoundPool mSoundPool;
	private int mDismiss, mTap, mVideoStart, mVideoStop;
	
	public enum SoundId { TAP, DISMISS, VIDEO_START, VIDEO_STOP }
	
	public SoundManager(Context context) {
		mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
		mDismiss = mSoundPool.load(context, R.raw.sound_dismiss, 1);
		mTap = mSoundPool.load(context, R.raw.sound_tap, 1);
		mVideoStart = mSoundPool.load(context, R.raw.sound_video_start, 1);
		mVideoStop = mSoundPool.load(context, R.raw.sound_video_stop, 1);
	}
	
	public void playSound(SoundId soundId) {
		int id = -1;
		switch(soundId) {
		case TAP:
			id = mTap;
			break;
		case DISMISS:
			id = mDismiss;
			break;
		case VIDEO_START:
			id = mVideoStart;
			break;
		case VIDEO_STOP:
			id = mVideoStop;
			break;
		}
		
		if(id != -1) {
			mSoundPool.play(id, 1f, 1f, 0, 0, 1);
		}
	}
	
	public void close() {
		mSoundPool.unload(mDismiss);
		mSoundPool.unload(mTap);
		mSoundPool.unload(mVideoStart);
		mSoundPool.unload(mVideoStop);
	}

}
