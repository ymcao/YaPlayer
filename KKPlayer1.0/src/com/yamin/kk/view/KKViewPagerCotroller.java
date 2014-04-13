package com.yamin.kk.view;

import android.app.Activity;
import android.view.View;

/**
 * 
 * @author yamin
 * 
 */
public abstract class KKViewPagerCotroller {
	public KKViewPagerCotroller(Activity activity) {
	};

	public abstract View getView();// viewpager获取view

	public abstract String getTitle();// viewpager获取标题名称

	public abstract void onshow();// 显示界面触发事件
}
