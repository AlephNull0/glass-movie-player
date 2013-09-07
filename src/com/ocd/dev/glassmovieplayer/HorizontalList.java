package com.ocd.dev.glassmovieplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.glass.horizontalscroll.BaseHorizontalScrollView;

public class HorizontalList extends BaseHorizontalScrollView<Integer, String> {
	public HorizontalList(Context arg0, boolean arg1) {
		super(arg0, arg1);
	}
	
	public HorizontalList(Context context) {
		super(context, false);
	}
	
	public HorizontalList(Context context, AttributeSet attrs) {
		super(context, attrs, false);
	}
	
	public HorizontalList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle, false);
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
