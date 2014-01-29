package com.ocd.dev.glassmovieplayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

// partially taken from GlassHome.apk and modified to work with the GDK
public class VolumeHelper {
	public static final int HEADSET_STATE_PLUGGED = 1;
	public static final int HEADSET_STATE_UNPLUGGED = 0;
	private static final int NUM_VALUES_BCT = 10;
	private static final int NUM_VALUES_EARBUDS = 19;
	private static final int INVALID = -1;
	private AudioManager mAudioManager;

	public static int getHeadsetState(Context context)
	  {
	    return getHeadsetState(context.registerReceiver(null, new IntentFilter("android.intent.action.HEADSET_PLUG")));
	  }

	  public static int getHeadsetState(Intent intent)
	  {
	    if (intent == null)
	    {
	      return 0;
	    }
	    int i = intent.getIntExtra("state", 0);
	    return i;
	  }

	public static int getNumVolumeValues(int headsetState)
	{
		if (headsetState == 1)
			return NUM_VALUES_EARBUDS;
		return NUM_VALUES_BCT;
	}
	
	public VolumeHelper(Context context) {
		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public int readAudioVolume()
	{
		try
		{
			return Integer.parseInt(mAudioManager.getParameters("earbuds_volume"));
		}
		catch (NumberFormatException localNumberFormatException)
		{
			return INVALID;
		}
	}

	public void writeAudioVolume(int volume)
	{
		// note: requires permission: android.permission.MODIFY_AUDIO_SETTINGS
		mAudioManager.setParameters("earbuds_volume=" + volume);
	}
}
