package com.yamin.kk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yamin.kk.R;
/**
* @ClassName: MyListView 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author ymcao
* @date 2013-6-23 下午11:51:35 
*
 */
public class MyListView extends ListView {
	public MyListView(Context context) {
		super(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 @Override 
     public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {      
         int expandSpec = MeasureSpec.makeMeasureSpec( 
                 Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST); 
         super.onMeasure(widthMeasureSpec, expandSpec); 
     } 
	/****
	 * 拦截触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int itemnum = pointToPosition(x, y);
			if (itemnum == AdapterView.INVALID_POSITION)
				break;
			else {
				if (itemnum == 0) {
					if (itemnum == (getAdapter().getCount() - 1)) {
						// 只有一项
						setSelector(R.drawable.list_round_selector_holo_light);
					} else {
						// 第一项
						setSelector(R.drawable.list_top_selector_holo_light);
					}
				} else if (itemnum == (getAdapter().getCount() - 1))
					// 最后一项
					setSelector(R.drawable.list_bottom_selector_holo_light);
				else {
					// 中间项
					setSelector(R.drawable.list_center_selector_holo_light);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onTouchEvent(ev);
	}
}