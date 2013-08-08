package com.ocd.dev.glassmovieplayer;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.glass.app.GlassApplication;
import com.google.glass.sound.SoundManager;
import com.google.glass.sound.SoundManager.SoundId;

public class MoviePlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
	public static final String TAG = "VID";
	public static final String PREFS_PREVIOUS_URL = "prev id";
	public static final String PREFS_PREVIOUS_POSITION = "prev pos";
    private SurfaceView mMovieSurface;
    private MediaPlayer mPlayer;
    private Uri mMovieUri;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;
    private boolean mPrepared;
    private Handler mHandler;
    private FrameLayout mContainer;
    private ProgressBar mLoadProgressBar;
    private float mStartX;
    private boolean mDidSeek = false;
    private MovieSeekBar mMovieSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_player);
        
        initProgressBar();

        initMovieSurface();
        
        initMoviePlayer();
        
        initWakeLock();
        
		initMovieSeekBar();
    }

	private void initProgressBar() {
		mLoadProgressBar = new ProgressBar(this);
        mLoadProgressBar.setIndeterminate(true);
        mLoadProgressBar.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
	        LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL |
	        Gravity.CENTER_VERTICAL));
        
		mContainer = (FrameLayout) findViewById(R.id.movieSurfaceContainer);
        mContainer.addView(mLoadProgressBar);
	}

	private void initMovieSurface() {
		mMovieSurface = (SurfaceView) findViewById(R.id.movieSurface);
        SurfaceHolder movieHolder = mMovieSurface.getHolder();
        movieHolder.addCallback(this);
        
        mMovieSurface.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.e(TAG, "focus");
				Toast.makeText(MoviePlayerActivity.this, "focus", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void initMoviePlayer() {
		Intent intent = getIntent();
    	mMovieUri = intent.getData();
        
        try {
	        mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(this, mMovieUri);
            mPlayer.setOnPreparedListener(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            errorLoadingMediaPlayer();
        } catch (SecurityException e) {
            e.printStackTrace();
            errorLoadingMediaPlayer();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            errorLoadingMediaPlayer();
        } catch (IOException e) {
            e.printStackTrace();
            errorLoadingMediaPlayer();
        }
	}
	
	private void errorLoadingMediaPlayer() {
		throw new RuntimeException("Error loading MediaPlayer");
	}

	private void initWakeLock() {
		mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
	}

	private void initMovieSeekBar() {
        mHandler = new Handler();
		mMovieSeekBar = new MovieSeekBar(this);
		mMovieSeekBar.setAnchorView(mContainer);
        mMovieSeekBar.setProgress(getCurrentPosition());
        updateSeekBarProgressWhileShowing();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mWakeLock.acquire();
    	
    	if(mPrepared) {
    		mPlayer.setOnCompletionListener(mCompletion);
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mWakeLock.release();
    	
    	// commit current position and url for restore
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putString(PREFS_PREVIOUS_URL, mMovieUri.toString());
    	editor.putInt(PREFS_PREVIOUS_POSITION, getCurrentPosition());
    	editor.commit();
    	
    	mPlayer.setOnCompletionListener(null);
    	mPlayer.stop();
    	mPlayer.release();
    	
    	// don't want callback to trigger after pause
    	mHandler.removeCallbacks(mSeekRunnable);
    	mMovieSeekBar.stop();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.e(TAG, "keydown " + event.getKeyCode());
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		getSoundManager().playSound(SoundId.DISMISS);
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
    	// one-finger tap pauses the movie
    	// two-finger swipe seeks (that is, rewinds and fast forwards)
    	switch(event.getActionMasked()) {
    	case MotionEvent.ACTION_DOWN:
			mStartX = event.getX();
			mDidSeek = false;
			break;
    		
    	case MotionEvent.ACTION_POINTER_DOWN:
            mDidSeek = true;
			mMovieSeekBar.setProgress(getCurrentPosition());
			mMovieSeekBar.show();
    		break;
    	
    	case MotionEvent.ACTION_MOVE:
    		if(event.getPointerCount() == 2) {
    			float x = event.getX();
    			float dx = x - mStartX;
    			mStartX = x;
    			
	            int duration = getDuration();
	            float rate = Math.min(duration / 2000f, 400f);
	            int pos =  (int) (getCurrentPosition() + dx*rate);
	            
	            if(pos < 0) pos = 0;
	            if(pos > duration) pos = duration;
	            seekTo(pos);
	            mMovieSeekBar.setProgress(pos);
    		}
    		break;
    		
    	case MotionEvent.ACTION_POINTER_UP:
    		mMovieSeekBar.hide();
    		break;
    		
    	case MotionEvent.ACTION_UP:
    		if(mDidSeek) {
    			updateSeekBarProgressWhileShowing();
    		} else {
    			showPauseMenu();
    		}
    		break;
    		
    	default:
	    	return super.onGenericMotionEvent(event);
    	}
    	
    	
    	return true;
    }
    
    private void showPauseMenu() {
    	if(isPlaying()) {
    		getSoundManager().playSound(SoundId.TAP);
			pause();
			final SingleMenuItemShower pauseShower = new SingleMenuItemShower(this);
			pauseShower.setText("Resume");
			pauseShower.setPosition(getCurrentPosition());
			pauseShower.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				
				public void onDismiss(DialogInterface dialog) {
					if(pauseShower.shouldReplay()) {
						getSoundManager().playSound(SoundId.TAP);
						mMovieSeekBar.setProgress(getCurrentPosition());
						mMovieSeekBar.show();
						mMovieSeekBar.hide();
						updateSeekBarProgressWhileShowing();
						start();
					} else {
						getSoundManager().playSound(SoundId.DISMISS);
						MoviePlayerActivity.this.finish();
					}
				}
			});
			mMovieSeekBar.hideNow();
			pauseShower.show();
		}
    }
    
    private GlassApplication getGlassApplication()
    {
      return GlassApplication.from(this);
    }
    
    private SoundManager getSoundManager()
    {
      return getGlassApplication().getSoundManager();
    }
    
    private Runnable mSeekRunnable = new Runnable() {
		
		@Override
		public void run() {
			mMovieSeekBar.setProgress(getCurrentPosition());
			
			if(mMovieSeekBar.isShowing()) {
				seekUpdate();
			}
		}
	};
    
    private void updateSeekBarProgressWhileShowing() {
		mMovieSeekBar.setProgress(getCurrentPosition());
    	mHandler.postDelayed(mSeekRunnable, 200);
    }
    
    private void seekUpdate() {
    	mHandler.postDelayed(mSeekRunnable, 200);
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	mPlayer.setDisplay(holder);
        mPlayer.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
        
        mPlayer.setOnCompletionListener(mCompletion);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MoviePlayerActivity.this);
        String prevUrl = prefs.getString(PREFS_PREVIOUS_URL, null);
        int prevPos = prefs.getInt(PREFS_PREVIOUS_POSITION, 0);
        
		mMovieSeekBar.setDuration(getDuration());
				
		if(mMovieUri.toString().equals(prevUrl) && prevPos >= 0 && prevPos <= getDuration()) {
			seekTo(prevPos);
			mMovieSeekBar.setProgress(prevPos);
			mMovieSeekBar.show();
			mMovieSeekBar.hide();
			updateSeekBarProgressWhileShowing();
		}
		
		mContainer.removeView(mLoadProgressBar);
		
		mPrepared = true;
    }
    
    
    private OnCompletionListener mCompletion = new OnCompletionListener() {

    	@Override
    	public void onCompletion(MediaPlayer mp) {
    		getSoundManager().playSound(SoundId.VIDEO_STOP);
    		final SingleMenuItemShower stop = new SingleMenuItemShower(MoviePlayerActivity.this);
    		stop.setText("Replay");
    		stop.setOnDismissListener(new DialogInterface.OnDismissListener() {

    			@Override
    			public void onDismiss(DialogInterface dialog) {
    				if(stop.shouldReplay()) {
    					start();
    					getSoundManager().playSound(SoundId.VIDEO_START);
    					mMovieSeekBar.setProgress(getCurrentPosition());
    					mMovieSeekBar.show();
    					mMovieSeekBar.hide();
    					updateSeekBarProgressWhileShowing();
    				} else {
    					getSoundManager().playSound(SoundId.DISMISS);
    					MoviePlayerActivity.this.finish();
    				}
    			}
    		});
    		mMovieSeekBar.hideNow();
    		stop.show();
    	}
    };

    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void seekTo(int i) {
        mPlayer.seekTo(i);
    }

    public void start() {
        mPlayer.start();
    }

}