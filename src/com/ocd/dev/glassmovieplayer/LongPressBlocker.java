package com.ocd.dev.glassmovieplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LongPressBlocker extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		abortBroadcast();
	}

}
