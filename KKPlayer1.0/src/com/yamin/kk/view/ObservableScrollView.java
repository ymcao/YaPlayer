package com.yamin.kk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
	private OnScrollChangedListener onScrollChangedListener;

	public ObservableScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ObservableScrollView(Context context) {
		super(context);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(this.onScrollChangedListener != null) {
			onScrollChangedListener.onScrollChanged(t, oldt);
		}
	}

	public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
		this.onScrollChangedListener = onScrollChangedListener;
	}

}
