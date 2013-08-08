package com.ocd.dev.glassmovieplayer;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.google.glass.app.GlassApplication;
import com.google.glass.horizontalscroll.BaseHorizontalScrollView;
import com.google.glass.input.InputListener;
import com.google.glass.input.SwipeDirection;
import com.google.glass.input.TouchDetector;
import com.google.glass.sound.SoundManager;
import com.google.glass.sound.SoundManager.SoundId;

public class MoviePickerActivity extends Activity implements InputListener, LoaderCallbacks<Cursor> {
	public static final int RESULT_VIDEO = 1;
	private static final int URL_LOADER = 0;
	private Cursor mMoviecursor;
	private HorizontalList mList;
	private TouchDetector mTouchDetector;
	private MovieAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		mList = new HorizontalList(this, false);
		setContentView(mList);
		
		mTouchDetector = new TouchDetector(this, this);
		
        getLoaderManager().initLoader(URL_LOADER, null, this);
        
        mList.activate();
        
		String[] proj = { 
				MediaStore.Video.Media.DISPLAY_NAME };
        mAdapter = new MovieAdapter(this, R.layout.movie_row, mMoviecursor, proj, new int[] { R.id.name } , 0);
        mList.setAdapter(mAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.video_picker, menu);
		return true;
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		mTouchDetector.onTouchEvent(event);
		return true;
	}
	
	private class HorizontalList extends BaseHorizontalScrollView<Integer, String> {

		public HorizontalList(Context arg0, boolean arg1) {
			super(arg0, arg1);
		}

		@Override
		public int findIdPosition(Integer position) {
			return position;
		}

		@Override
		public int findItemPosition(String arg0) {
			return 0;
		}

		@Override
		public int getHomePosition() {
			return 0;
		}

		@Override
		public View getViewForPosition(int position) {
			return getAdapter().getView(position, null, this);
		}

		@Override
		public void rebindView(int arg0, View arg1) {
			
		}
		
	}

	@Override
	public boolean onCameraButtonPressed() {
		return false;
	}

	@Override
	public boolean onConfirm() {
		int position = mList.getSelectedItemPosition();
		
		if(position != -1) {
			int index = mMoviecursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			mMoviecursor.moveToPosition(position);
			String videoLocationPath = mMoviecursor.getString(index);
			Uri videoLocation = Uri.parse(videoLocationPath);
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(videoLocation, "video/*");
			getSoundManager().playSound(SoundId.VIDEO_START);
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
	public boolean onDoubleTap() {
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
	public boolean onSwipeCanceled(int arg0) {
		return mList.onSwipeCanceled(arg0);
	}

	@Override
	public boolean onVerticalHeadScroll(float arg0, float arg1) {
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		switch(loaderId) {
		case URL_LOADER:
			String[] proj = { MediaStore.Video.Media._ID,
					MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.DISPLAY_NAME,
					MediaStore.Video.Media.SIZE };
			String selection = MediaStore.Video.Media.DATA + " not like ? ";
		            
		    String[] selectionArgs = new String[] {"%sdcard/glass_cached_files%"};
	        
			return new CursorLoader(this, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					proj, selection, selectionArgs, MediaStore.Video.Media.DISPLAY_NAME);
			
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mMoviecursor = cursor;
		mAdapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}

}
