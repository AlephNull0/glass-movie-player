package com.ocd.dev.glassmovieplayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;
import com.ocd.dev.glassmovieplayer.SoundManager.SoundId;

public class MovieBucketBrowserActivity extends Activity implements LoaderCallbacks<Cursor> {
	public static final int RESULT_VIDEO = 1;
	private static final int URL_LOADER = 0;
	private CardScrollView mCardScrollView;
	private GestureDetector mTouchDetector;
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
		mCardScrollView = (CardScrollView)findViewById(R.id.list);
		mEmptyMessage = findViewById(R.id.empty);
		
		mCardScrollView.setOnItemClickListener(mItemClickListener);
		
        getLoaderManager().initLoader(URL_LOADER, null, this);
        
        mCardScrollView.activate();
        
        mMovieBuckets = new ArrayList<MovieBucket>();
        mAdapter = new MovieBucketAdapter(this, mMovieBuckets);
        mCardScrollView.setAdapter(mAdapter);
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return false;
	}
	
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(mLength > 0 && position != -1) {
				getSoundManager().playSound(SoundId.TAP);
				Intent intent = new Intent(MovieBucketBrowserActivity.this, MoviePickerActivity.class);
				intent.putExtra(MoviePickerActivity.EXTRA_MOVIE_BUCKET, mMovieBuckets.get(position).getId());
				startActivity(intent);
			}
		}
	};
	
	/*
	private BaseListener mBaseListener = new BaseListener() {
		
		@Override
		public boolean onGesture(Gesture gesture) {
			if(gesture == Gesture.TAP) {
				int position = mCardScrollView.getSelectedItemPosition();
				
				if(mLength > 0 && position != -1) {
					getSoundManager().playSound(SoundId.TAP);
					Intent intent = new Intent(MovieBucketBrowserActivity.this, MoviePickerActivity.class);
					intent.putExtra(MoviePickerActivity.EXTRA_MOVIE_BUCKET, mMovieBuckets.get(position).getId());
					startActivity(intent);
					return true;
				}
			}
			return false;
		}
	};
	*/

    private SoundManager getSoundManager()
    {
      return ((GlassApplication)getApplication()).getSoundManager();
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
