package com.ocd.dev.glassmovieplayer;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.glass.widget.TipsView;
import com.google.glass.widget.horizontalscroll.ViewRecycler;

public class MovieBucketAdapter extends BaseAdapter implements ViewRecycler {
	private Context mContext;
	private LayoutInflater mInflater;
	private List<MovieBucket> mMovieBuckets;
	
	public MovieBucketAdapter(Context context, List<MovieBucket> movieBuckets) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mMovieBuckets = movieBuckets;
	}
	
	@Override
	public void recycleView(View arg0) {
		
	}
	
	@Override
	public int getCount() {
		return mMovieBuckets.size();
	}

	@Override
	public Object getItem(int position) {
		return mMovieBuckets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		
		if(convertView == null) {
			view = mInflater.inflate(R.layout.movie_bucket_row, parent, false);
			view.setTag(com.google.glass.common.R.id.tag_horizontal_scroll_item_view_recycler, this);
			holder = new ViewHolder();
			
			holder.name = (TextView)view.findViewById(R.id.name);
			ImageView thumbnail1 = (ImageView)view.findViewById(R.id.thumbnail1);
			ImageView thumbnail2 = (ImageView)view.findViewById(R.id.thumbnail2);
			ImageView thumbnail3 = (ImageView)view.findViewById(R.id.thumbnail3);
			ImageView thumbnail4 = (ImageView)view.findViewById(R.id.thumbnail4);
			holder.thumbnails = new ImageView[] { thumbnail1, thumbnail2, thumbnail3, thumbnail4 };
			holder.count = (TipsView)view.findViewById(R.id.count);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
		
		MovieBucket item = mMovieBuckets.get(position);
		holder.name.setText(item.getDisplayName());
		holder.count.setText(Long.toString(position+1) + " of " + mMovieBuckets.size());
		
		List<Movie> movies = item.getMovies();
		for(int i=0; i<Math.min(4, movies.size()); ++i) {
			new BucketCoverLoader().execute(holder.thumbnails[i], movies.get(i).getId());
		}
		
		return view;
	}
	
	private static class ViewHolder {
		public ImageView[] thumbnails;
		public TextView name;
		public TipsView count;
	}
	
	private class BucketCoverLoader extends AsyncTask<Object, String, Bitmap> {

	    private ImageView view1;

	    @Override
	    protected Bitmap doInBackground(Object... parameters) {
	        // Get the passed arguments here
	        view1 = (ImageView) parameters[0];
	        long id = (long)(Long)parameters[1];

	        // Create bitmap from passed in Uri here
	        BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(MovieBucketAdapter.this.mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, options);
			
	        return curThumb;
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (bitmap != null && view1!= null) {
	        	view1.setColorFilter(Color.rgb(0xAA, 0xAA, 0xAA), android.graphics.PorterDuff.Mode.MULTIPLY);
				view1.setImageBitmap(bitmap);
	        }
	    }
	}

}
