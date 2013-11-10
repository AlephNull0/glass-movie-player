package com.ocd.dev.glassmovieplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.google.glass.app.GlassApplication;
import com.google.glass.input.InputListener;
import com.google.glass.input.SwipeDirection;
import com.google.glass.input.TouchDetector;
import com.google.glass.sound.SoundManager;
import com.google.glass.sound.SoundManager.SoundId;

public class MovieBucketBrowserActivity extends Activity implements InputListener, LoaderCallbacks<Cursor> {
	public static final int RESULT_VIDEO = 1;
	private static final int URL_LOADER = 0;
	private HorizontalList mList;
	private TouchDetector mTouchDetector;
	private MovieBucketAdapter mAdapter;
	private View mEmptyMessage;
	private int mLength;
	
	private List<MovieBucket> mMovieBuckets;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_movie_picker);
		mLength = -1;
		mList = (HorizontalList)findViewById(R.id.list);
		mEmptyMessage = findViewById(R.id.empty);
		
		mTouchDetector = new TouchDetector(this, this);
		
        getLoaderManager().initLoader(URL_LOADER, null, this);
        
        mList.activate();
        
        mMovieBuckets = new ArrayList<MovieBucket>();
        mAdapter = new MovieBucketAdapter(this, mMovieBuckets);
        mList.setAdapter(mAdapter);
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		mTouchDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onCameraButtonPressed() {
		return false;
	}

	@Override
	public boolean onConfirm() {
		int position = mList.getSelectedItemPosition();
		
		if(mLength > 0 && position != -1) {
			getSoundManager().playSound(SoundId.TAP);
			Intent intent = new Intent(this, MoviePickerActivity.class);
			intent.putExtra(MoviePickerActivity.EXTRA_MOVIE_BUCKET, mMovieBuckets.get(position).getId());
			startActivity(intent);
			return true;
		}
		
		return false;
	}
	
	private GlassApplication getGlassApplication()
    {
      return GlassApplication.from(this);
    }
    
    private SoundManager getSoundManager()
    {
      return getGlassApplication().getSoundManager();
    }

	@Override
	public boolean onDismiss(DismissAction arg0) {
		return false;
	}

	@Override
	public boolean onFingerCountChanged(int arg0, boolean arg1) {
		return mList.onFingerCountChanged(arg0, arg1);
	}

	@Override
	public boolean onPrepareSwipe(int arg0, float arg1, float arg2, float arg3,
			float arg4, int arg5, int arg6) {
		mList.onPrepareSwipe(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		return true;
	}

	@Override
	public boolean onSwipe(int arg0, SwipeDirection arg1) {
		mList.onSwipe(arg0, arg1);
		return true;
	}

	@Override
	public boolean onVerticalHeadScroll(float arg0) {
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		switch(loaderId) {
		case URL_LOADER:
			String[] proj = {
					MediaStore.Video.Media._ID,
					MediaStore.Video.Media.DISPLAY_NAME,
					MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Video.Media.BUCKET_ID,
					MediaStore.Video.Media.DATA,
			};
			
			String selection = MediaStore.Video.Media.DATA + " not like ? ";
		    String[] selectionArgs = new String[] {"%sdcard/glass_cached_files%"};
		    
			// select bucket_name, bucket_id from qwer;
			return new CursorLoader(this, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					proj, selection, selectionArgs, MediaStore.Video.Media.DISPLAY_NAME);

		default:
			return null;
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		HashMap<Long, MovieBucket> buckets = new HashMap<Long, MovieBucket>();
		
		int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
		int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
		int displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
		
		if(cursor.moveToFirst()) {
			do {
				Movie movie = new Movie(cursor.getLong(idIndex), cursor.getString(displayNameIndex));
				long bucketId = cursor.getLong(bucketIdIndex);
				if(!buckets.containsKey(bucketId)) {
					buckets.put(bucketId, new MovieBucket(bucketId, cursor.getString(displayNameIndex)));
				}

				buckets.get(bucketId).addMovie(movie);
			} while(cursor.moveToNext());
		}
		
		mMovieBuckets.clear();
		mMovieBuckets.addAll(buckets.values());
		mAdapter.notifyDataSetChanged();
		
		mLength = cursor.getCount();
		invalidateOptionsMenu();
		
		if(mLength == 0) {
			mEmptyMessage.setVisibility(View.VISIBLE);
		} else {
			mEmptyMessage.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mMovieBuckets.clear();
		mAdapter.notifyDataSetChanged();
	}

}
