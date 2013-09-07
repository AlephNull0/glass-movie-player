package com.ocd.dev.glassmovieplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.glass.horizontalscroll.ViewRecycler;
import com.google.glass.widget.TipsView;

public class MovieAdapter extends SimpleCursorAdapter implements ViewRecycler {
	private Context mContext;
	private LayoutInflater mInflater;
	private int mVideoColumnIndex, mIdColumnIndex;
	
	public MovieAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.movie_row, parent, false);
		view.setTag(com.google.glass.common.R.id.tag_horizontal_scroll_item_view_recycler, this);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		mVideoColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
		mIdColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
		
		TextView name = (TextView)view.findViewById(R.id.name);
		ImageView thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
		TipsView count = (TipsView)view.findViewById(R.id.count);
		count.setText(String.format("%d of %d", cursor.getPosition() + 1, cursor.getCount()));
		name.setText(cursor.getString(mVideoColumnIndex));
		
		long id = cursor.getLong(mIdColumnIndex);
		
		new ImageLoader().execute(thumbnail, Long.valueOf(id));
	}

	@Override
	public void recycleView(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public class ImageLoader extends AsyncTask<Object, String, Bitmap> {

	    private ImageView view;

	    @Override
	    protected Bitmap doInBackground(Object... parameters) {

	        // Get the passed arguments here
	        view = (ImageView) parameters[0];
	        long id = (long)(Long)parameters[1];

	        // Create bitmap from passed in Uri here
	        BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(MovieAdapter.this.mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, options);
			
	        return curThumb;
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (bitmap != null && view != null) {
	        	view.setColorFilter(Color.rgb(0xAA, 0xAA, 0xAA), android.graphics.PorterDuff.Mode.MULTIPLY);
				view.setImageBitmap(bitmap);
	        }
	    }
	}

}
