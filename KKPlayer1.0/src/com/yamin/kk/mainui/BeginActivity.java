package com.yamin.kk.mainui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.yamin.kk.R;
import com.yamin.kk.utils.AppManager;
import com.yamin.kk.utils.Common;
import com.yamin.kk.view.FeatureAnimationListener;
import com.yamin.kk.view.ObservableScrollView;
import com.yamin.kk.view.OnScrollChangedListener;

public class BeginActivity extends BaseActivity implements
		OnGlobalLayoutListener, OnScrollChangedListener {
	private ObservableScrollView mScrollView;
	private View mAnimView;
	private ImageView sloganIcon;
	private int mScrollViewHeight;
	private int mStartAnimateTop;
	private ImageView beginApplication;
	// 延迟2.0秒
	private final long DISPLAY_DURATION = 2000;
	private boolean isFistRun = false;
	private SharedPreferences mSettings;
	SharedPreferences.Editor editor;
	SharedPreferences pref;
	private int mVersionNumber = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/* Get the current version from package */
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("", "package info not found.");
		}
		if (pinfo != null)
			mVersionNumber = pinfo.versionCode;

		/* Get settings */
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);

		/* Check if it's the first run */

		isFistRun = mSettings.getInt("first_run", -1) != mVersionNumber;
		
		mScrollView = (ObservableScrollView) this.findViewById(R.id.scrollViewBegin);
		mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
		mScrollView.setOnScrollChangedListener(this);
		sloganIcon=(ImageView)this.findViewById(R.id.iv_slogan);
		if (isFistRun) {
			mScrollView.setVisibility(View.VISIBLE);
			sloganIcon.setVisibility(View.GONE);
			Editor editor0 = mSettings.edit();
			editor0.putInt("first_run", mVersionNumber);
			editor0.commit();
			pref = getSharedPreferences(Common.NAME, MODE_PRIVATE);
			editor = pref.edit();
			editor.putBoolean("enable_brightness_gesture", true);
			editor.putBoolean("enable_jump_buttons", true);
			editor.putString("screen_orientation_value", "0");
			editor.putString("hardware_acceleration", "-1");
			editor.putString("chroma_format", "RV32");
			editor.commit();
			/* */

		} else {
			/*非首次进入应用*/
			mScrollView.setVisibility(View.GONE);
			sloganIcon.setVisibility(View.VISIBLE);
			/*延迟2秒进入主界面*/
			new Handler().postDelayed(new Runnable() {
 
				public void run() {
					startPlayerMainActiity();
				}
			}, DISPLAY_DURATION);
		}
		beginApplication = (ImageView) findViewById(R.id.beginApp);
		beginApplication.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startPlayerMainActiity();

			}
		});
		mAnimView = this.findViewById(R.id.anim1);
		mAnimView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onGlobalLayout() {
		mScrollViewHeight = mScrollView.getHeight();
		mStartAnimateTop = mScrollViewHeight / 3 * 2;
	}

	boolean hasStart = false;

	@Override
	public void onScrollChanged(int top, int oldTop) {
		int animTop = mAnimView.getTop() - top;

		if (top > oldTop) {
			if (animTop < mStartAnimateTop && !hasStart) {
				Animation anim1 = AnimationUtils.loadAnimation(this,
						R.anim.feature_anim2scale_in);
				anim1.setAnimationListener(new FeatureAnimationListener(
						mAnimView, true));

				mAnimView.startAnimation(anim1);
				hasStart = true;
			}
		} else {
			if (animTop > mStartAnimateTop && hasStart) {
				Animation anim1 = AnimationUtils.loadAnimation(this,
						R.anim.feature_alpha_out);
				anim1.setAnimationListener(new FeatureAnimationListener(
						mAnimView, false));

				mAnimView.startAnimation(anim1);
				hasStart = false;
			}
		}
	}

	@Override
	public void initWidget() {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetClick(View v) {
		// TODO Auto-generated method stub

	}

	private void startPlayerMainActiity() {
		Intent intent = new Intent(BeginActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BeginActivity.this.startActivity(intent);
		BeginActivity.this.overridePendingTransition(R.anim.activity_enter,
				R.anim.activity_exit);
		BeginActivity.this.finish();
		AppManager.getAppManager().finishActivity(BeginActivity.this);
	}
}
