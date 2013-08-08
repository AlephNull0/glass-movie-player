package com.ocd.dev.glassmovieplayer;

import java.util.Locale;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.glass.horizontalscroll.HorizontallyTuggableView;
import com.google.glass.input.InputListener;
import com.google.glass.input.SwipeDirection;
import com.google.glass.input.TouchDetector;
import com.google.glass.widget.TypophileTextView;

public class SingleMenuItemShower extends Dialog implements InputListener {
	private ImageView mIcon;
	private TypophileTextView mLabel, mPosition;
	private boolean mDismissing;
	private TouchDetector mTouchDetector;
    private boolean mReplay;
    private HorizontallyTuggableView mTugView;
	
	public SingleMenuItemShower(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		
		View v = LayoutInflater.from(context).inflate(R.layout.option_menu_item, null);
		mTugView = new HorizontallyTuggableView(context);
		mTugView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mTugView.setView(v);
		mTugView.activate();
		setContentView(mTugView);
		mIcon = (ImageView)v.findViewById(R.id.icon);
		mLabel = (TypophileTextView)v.findViewById(R.id.label);
		mPosition = (TypophileTextView)v.findViewById(R.id.description);
		mIcon.setImageResource(R.drawable.ic_media_play);
		mTouchDetector = new TouchDetector(context, this);
	}
	
	public boolean shouldReplay() {
		return mReplay;
	}
	
	public void setText(String text) {
		mLabel.setText(text);
	}
	
	public void setPosition(int position) {
		int totalSeconds = position / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds - seconds) / 60;
		int hours = (totalSeconds - 60*minutes - seconds) / 60;
		String pos = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
		mPosition.setText(pos);
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
		if(!mDismissing) {
			mReplay = true;
			dismiss();
			mDismissing = true;
		}
		return true;
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
		return mTugView.onFingerCountChanged(arg0, arg1);
	}

	@Override
	public boolean onPrepareSwipe(int arg0, float arg1, float arg2, float arg3,
			float arg4, int arg5, int arg6) {
		mTugView.onPrepareSwipe(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		return true;
	}

	@Override
	public boolean onSwipe(int arg0, SwipeDirection arg1) {
		mTugView.onSwipe(arg0, arg1);
		return true;
	}

	@Override
	public boolean onSwipeCanceled(int arg0) {
		return mTugView.onSwipeCanceled(arg0);
	}

	@Override
	public boolean onVerticalHeadScroll(float arg0, float arg1) {
		return false;
	}
	
}
