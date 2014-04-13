package com.yamin.kk.view;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FeatureAnimationListener implements AnimationListener {
	private View mAnimView;
	private boolean mAnimIn;
	
	public FeatureAnimationListener(View animView, boolean animIn) {
		mAnimView = animView;
		mAnimIn = animIn;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if(!mAnimIn) {
			mAnimView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {
		if(mAnimIn) {
			mAnimView.setVisibility(View.VISIBLE);
		}
	}

}