package com.ocd.dev.glassmovieplayer;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.google.android.glass.touchpad.GestureDetector.FingerListener;
import com.google.android.glass.touchpad.GestureDetector.ScrollListener;
import com.ocd.dev.glassmovieplayer.SoundManager.SoundId;

public class MoviePlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
	public static final String TAG = "VID";
	public static final String EXTRA_PLAYLIST = "play list";
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
    private MovieSeekBar mMovieSeekBar;
    private GestureDetector mTouchDetector;
	private ArrayList<CharSequence> mVideoList;
	private int mVideoIndex = 0;
	private boolean paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_player);
        
        initProgressBar();

        initMovieSurface();
        
        initMoviePlayer();
        
        initWakeLock();
        
		initMovieSeekBar();
		
		initGestureDetector();
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
		
		mVideoList = intent.getCharSequenceArrayListExtra(EXTRA_PLAYLIST);
		if(mVideoList == null) {
	    	mMovieUri = intent.getData();
		} else if (mVideoList.size() > 0) {
			mMovieUri = Uri.parse((String)mVideoList.get(mVideoIndex));
		}
        
        try {
	        mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(this, mMovieUri);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(mErrorListener);
            Log.e(TAG, "initialized movie player");
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
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
	}

	private void initMovieSeekBar() {
        mHandler = new Handler();
		mMovieSeekBar = new MovieSeekBar(this);
		mMovieSeekBar.setAnchorView(mContainer);
        mMovieSeekBar.setProgress(getCurrentPosition());
        updateSeekBarProgressWhileShowing();
	}
	
	private void initGestureDetector() {
		mTouchDetector = new GestureDetector(this);
		mTouchDetector.setBaseListener(mBaseListener);
		mTouchDetector.setFingerListener(mFingerListener);
		mTouchDetector.setScrollListener(mScrollListener);
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mWakeLock.acquire();
    	
    	Log.e(TAG, "on resume. Prepared: " + mPrepared);
    	
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
    	
    	Log.e(TAG, "on pause called. player stopped and released");
    	
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
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.video_player, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.resume:
    		paused = false;
    		resumeMovie();
    		return true;
    	default:
	    	return super.onOptionsItemSelected(item);
    	}
    }
    
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
    	mTouchDetector.onMotionEvent(event);
    	return true;
    }
    
    private void pauseMovie() {
    	if(isPlaying()) {
    		getSoundManager().playSound(SoundId.TAP);
			pause();
			mMovieSeekBar.hideNow();
		}
    }
    
    private void resumeMovie() {
    	if(!isPlaying()) {
			getSoundManager().playSound(SoundId.TAP);
			mMovieSeekBar.setProgress(getCurrentPosition());
			mMovieSeekBar.show();
			mMovieSeekBar.hide();
			updateSeekBarProgressWhileShowing();
			start();
    	}
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
        Log.e(TAG, "called prepareAsync in surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
		Log.e(TAG, "video at index " + mVideoIndex + " prepared. starting");
		
		// some videos are occasionally not loaded properly. this code tries to reload
		if(mPlayer.getDuration() <= 0) {
			Log.e(TAG, "something went wrong while preparing. retrying");
			try {
				mPlayer.reset();
				mPlayer.setDataSource(MoviePlayerActivity.this, mMovieUri);
				mPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(mPlayer.getDuration() <= 0) {
			Log.e(TAG, "could not fix. moving on to next video.");
			Toast.makeText(MoviePlayerActivity.this, "Couldn't load video " +
				(mVideoIndex + 1) + ". Skipping." , Toast.LENGTH_SHORT).show();
			++mVideoIndex;
			playNextMovieIfNeeded();
			return;
		}
		
		++mVideoIndex;

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
    		Log.e(TAG, "video completed");
    		playNextMovieIfNeeded();
    	}
    };
    
    private void playNextMovieIfNeeded() {
    	if(mVideoList == null || mVideoIndex >= mVideoList.size()) {
			Log.e(TAG, "all videos completed");
    		getSoundManager().playSound(SoundId.VIDEO_STOP);
    		mMovieSeekBar.hideNow();
    		finish();
		} else {
    		getSoundManager().playSound(SoundId.VIDEO_START);
			playNextMovie();
		}
    }
    
    private void playNextMovie() {
		try {
			Log.e(TAG, "preparing video at index " + mVideoIndex);
			mMovieUri = Uri.parse((String)mVideoList.get(mVideoIndex));
			Log.e(TAG, "URI: " + mMovieUri);
			mPlayer.reset();
			mPlayer.setDataSource(MoviePlayerActivity.this, mMovieUri);
			mPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
    
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -2147483648) {
				fatalErrorMessage("Unsupported video format");
			} else {
				fatalErrorMessage(String.format("Cannot play video. Error code (%d, %d)", what, extra));
			}
			
			return false;
		}
	};
	
	private void fatalErrorMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		finish();
	}
	
	private BaseListener mBaseListener = new BaseListener() {
		
		@Override
		public boolean onGesture(Gesture gesture) {
			if(gesture == Gesture.TAP) {
				paused = true;
				openOptionsMenu();
				pauseMovie();
				return true;
			} 

			return false;
		}
	};
	
	public void onOptionsMenuClosed(Menu menu) {
		if(paused) {
			finish();
		}
	}
	
	private FingerListener mFingerListener = new FingerListener() {
		
		@Override
		public void onFingerCountChanged(int previousCount, int currentCount) {
			if(currentCount == 1) {
				mMovieSeekBar.show();
			} else if(currentCount == 0) {
				mMovieSeekBar.hide();
				//x = 0f;
				updateSeekBarProgressWhileShowing();
			}
		}
	};
	
	private ScrollListener mScrollListener = new ScrollListener() {
		
		@Override
		public boolean onScroll(float displacement, float delta, float velocity) {
			float dx = delta;
			
            int duration = getDuration();
            float rate = Math.min(duration / 2000f, 400f);
            int pos =  (int) (getCurrentPosition() + dx*rate);
            
            if(pos < 0) pos = 0;
            if(pos > duration) pos = duration;
            seekTo(pos);
            mMovieSeekBar.setProgress(pos);

			return true;
		}
	};
	
	private SoundManager getSoundManager() {
		return ((GlassApplication)getApplication()).getSoundManager();
	}

}
