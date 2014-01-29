package com.ocd.dev.glassmovieplayer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.google.android.glass.touchpad.GestureDetector.FingerListener;
import com.google.android.glass.touchpad.GestureDetector.ScrollListener;
import com.ocd.dev.glassmovieplayer.SoundManager.SoundId;

public class VolumeDialog extends Dialog {
	private static final String TAG = "VolumeDialog";
	private Context mContext;
	private ImageView mVolumeIcon;
	private SeekBar mVolumeSeek;
	private GestureDetector mGestureDetector;
	private int mVolume;
	private SoundManager mSoundManager;
	private VolumeHelper mVolumeHelper;
	private Handler mHandler;
	private int mNumVolumeValues;

	public VolumeDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_volume);

		mVolumeIcon = (ImageView)findViewById(R.id.volume_icon);
		mVolumeSeek = (SeekBar)findViewById(R.id.volume_seek);
		
		mVolumeHelper = new VolumeHelper(mContext);
		int headsetState = VolumeHelper.getHeadsetState(mContext);
		mNumVolumeValues = VolumeHelper.getNumVolumeValues(headsetState);
		mVolumeSeek.setMax(mNumVolumeValues - 1);
		mVolumeSeek.setOnSeekBarChangeListener(mSeekBarChangeListener);
		
		mVolume = mVolumeHelper.readAudioVolume();
		updateVolumeDrawable(mVolume);
		
		mVolumeSeek.setProgress(mVolume);
		mSoundManager = new SoundManager(mContext);

		// used to move the seekbar. temporary solution until
		// the GDK improves
		mGestureDetector = new GestureDetector(mContext);
		mGestureDetector.setBaseListener(mBaseListener);
		mGestureDetector.setFingerListener(mFingerListener);
		mGestureDetector.setScrollListener(mScrollListener);
		mHandler = new Handler();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mContext.registerReceiver(mHeadsetReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
		Log.d(TAG, "headset receiver registered");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mContext.unregisterReceiver(mHeadsetReceiver);
		Log.d(TAG, "headset receiver unregistered");
	}
	
	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			updateVolumeDrawable(progress);
			mVolumeHelper.writeAudioVolume(progress);
			Log.d(TAG, "volume changed to " + progress);
		}
	};
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		super.onGenericMotionEvent(event);
		return mGestureDetector.onMotionEvent(event);
	}
	
	boolean mIsDismissing;
	
	private BaseListener mBaseListener = new BaseListener() {
		
		@Override
		public boolean onGesture(Gesture gesture) {
			if(gesture == Gesture.TAP) {
				dismiss();
				return true;
			} else {
				return false;
			}
		}
	};
	
	private FingerListener mFingerListener = new FingerListener() {
		
		@Override
		public void onFingerCountChanged(int previousCount, int currentCount) {
			Log.d(TAG, "finger coung chanted from " + previousCount + " to " + currentCount);
			if(previousCount > 0 && currentCount == 0) {
				mVolume = mVolumeSeek.getProgress();
				
				mHandler.removeCallbacks(mDingRunnable);
				mHandler.postDelayed(mDingRunnable, 100);
			}
		}
	};
	
	private Runnable mDingRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(isShowing()) {
			    mSoundManager.playSound(SoundId.VOLUME_CHANGE);
			}
		}
	};
	
	private ScrollListener mScrollListener = new ScrollListener() {
		
		@Override
		public boolean onScroll(float displacement, float delta, float velocity) {
			int value = (int)(mVolume + displacement / 100);
			if(value >= mNumVolumeValues) {
				value = mNumVolumeValues - 1;
			}
	
			if(value < 0) {
				value = 0;
			}

			mVolumeSeek.setProgress(value);
			
		    return true;
		}
	}; 
	
	private void updateVolumeDrawable(int volume) {
		int icon;
	    if(volume == 0) {
	    	icon = R.drawable.ic_volume_0_large;
	    } else if(volume < mNumVolumeValues/2) {
	    	icon = R.drawable.ic_volume_1_large;
	    } else {
	    	icon = R.drawable.ic_volume_2_large;
	    }
	    mVolumeIcon.setImageResource(icon);
	}
	
	private BroadcastReceiver mHeadsetReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int headsetState = intent.getIntExtra("state", 0);
			mNumVolumeValues = VolumeHelper.getNumVolumeValues(headsetState);
			mVolumeSeek.setMax(mNumVolumeValues - 1);
			Log.d(TAG, "headset state changed to " + headsetState);
		}
	};
}
