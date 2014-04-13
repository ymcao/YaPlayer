package com.yamin.kk.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamin.kk.R;

@SuppressLint("NewApi")
public class KKViewPager extends LinearLayout {
	class TitleData {
		TextView childtitle;
		ImageView childpic;
		View childtitleview;
	}

	private Context context;
	private List<KKViewPagerCotroller> data;// 原始数据

	private View fatherview;// 主界�?
	private LinearLayout titleitem;// 标题布局
	private ViewPager viewpager;// 下面部分

	private int currindex;// 当前选择
	private int oldcurrindex;// 上次选中

	private OnPageChange listener;

	// *******viewpager部分
	private MyViewPagerAdapter adapter;

	// *******标题部分
	private List<TitleData> titledatas;
	private int titleweight;// 标题平分宽度

	private int title_currcolor = 0xff36c77b;// 标题选中的字体color
	private int title_nocurrcolor = 0xffC3C3C3;// 标题未选中时的字体color
	private int title_currimage = R.drawable.curr_flag;// 标题选中时的下划Tab

	public void setCurrcolor(int color) {
		title_currcolor = color;
	}

	public void setnoCurrcolor(int color) {
		title_nocurrcolor = color;
	}

	public void setCurrimage(int id) {
		title_currimage = id;
	}

	public KKViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public KKViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public KKViewPager(Context context) {
		super(context);

	}

	private void init(Context context) {
		this.context = context;

		// 父界
		fatherview = LayoutInflater.from(this.context).inflate(
				R.layout.layout_sybviewpager, null);
		titleitem = (LinearLayout) fatherview.findViewById(R.id.Layout_title);
		viewpager = (ViewPager) fatherview.findViewById(R.id.vPager);

		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						// 现在布局全部完成，可以获取到任何View组件的宽度�?高度、左边�?右边等信�?
						if (titleweight == 0 && titledatas != null) {
							titleweight = (int) ((getWidth() * 1.0) / data
									.size());// 计算平均宽度
							for (TitleData data : titledatas) {
								// 重新调整宽度，使其平�?
								data.childtitleview
										.setLayoutParams(new LayoutParams(
												titleweight,
												LayoutParams.WRAP_CONTENT));
							}
						}
					}
				});
	}

	public void setOnPageChangeListener(OnPageChange listener) {
		this.listener = listener;
	}

	/**
	 * 设置界面
	 * 
	 * @param viewdatas界面数据（包�
	 *            �标题名字�?界面控制器）
	 * @param defcurrindex默�
	 *            ��选中�?
	 */
	public synchronized void setViews(List<KKViewPagerCotroller> viewdatas,
			int defcurrindex) {
		data = viewdatas;

		titleweight = (int) ((getWidth() * 1.0) / data.size());

		// viewpager 初始�?
		adapter = new MyViewPagerAdapter(data);
		viewpager.setAdapter(adapter);
		viewpager.setOnPageChangeListener(new MyOnPageChangeListener());

		// title 初始化
		super.removeAllViews();
		titleitem.removeAllViews();
		titledatas = new ArrayList<TitleData>();
		for (int i = 0; i < data.size(); i++) {

			View childtitleview = LayoutInflater.from(this.context).inflate(
					R.layout.layout_sybviewpager_title, null);
			childtitleview.setOnClickListener(new MyOnClickListener(i));

			TextView childtitle = (TextView) childtitleview
					.findViewById(R.id.title_text);
			ImageView childpic = (ImageView) childtitleview
					.findViewById(R.id.title_flag);

			TitleData cdata = new TitleData();
			cdata.childtitleview = childtitleview;
			cdata.childtitle = childtitle;
			cdata.childtitle.setText(data.get(i).getTitle());
			cdata.childpic = childpic;

			if (i == defcurrindex) {
				// 初始化默认中项
				cdata.childpic.setVisibility(View.VISIBLE);// 默认显示
				cdata.childtitle.setTextColor(title_currcolor);
				oldcurrindex = 0;
				currindex = 0;
			} else {
				// 初始化非第一项隐
				cdata.childpic.setVisibility(View.INVISIBLE);// 显示标示,默认(占用空间)隐藏
				cdata.childtitle.setTextColor(title_nocurrcolor);
			}
			// 设置选中时的下划
			cdata.childpic.setBackgroundResource(title_currimage);

			if (titleweight != 0)
				childtitleview.setLayoutParams(new LayoutParams(titleweight,
						LayoutParams.WRAP_CONTENT));
			titledatas.add(cdata);
			titleitem.addView(childtitleview);
		}

		if (defcurrindex != 0) {
			// 显示默认界面，之后触发onshow事件
			viewpager.setCurrentItem(defcurrindex);
		}
		// 触发子事件显示
		if (data.get(defcurrindex) != null) {
			data.get(defcurrindex).onshow();
		}
		super.addView(fatherview);
	}

	public void setCurrentItem(int index) {
		// 触发子事件显示
		if (data.get(index) != null) {
			data.get(index).onshow();
		}
		viewpager.setCurrentItem(index);
	}

	/**
	 * 标题点击
	 * 
	 * @author ping
	 * 
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewpager.setCurrentItem(index);
		}
	}

	private class MyViewPagerAdapter extends PagerAdapter {
		private List<KKViewPagerCotroller> mListViews;

		public MyViewPagerAdapter(List<KKViewPagerCotroller> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position).getView(), 0);
			return mListViews.get(position).getView();
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	private class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int index) {
			// 页面转换
			currindex = index;
			// 改变下方横向图片
			titledatas.get(oldcurrindex).childpic.setVisibility(View.INVISIBLE);
			titledatas.get(currindex).childpic.setVisibility(View.VISIBLE);
			// 改变标题颜色
			titledatas.get(oldcurrindex).childtitle
					.setTextColor(title_nocurrcolor);
			titledatas.get(currindex).childtitle.setTextColor(title_currcolor);

			oldcurrindex = index;
			// 触发子事件
			if (listener != null) {
				listener.onPageSelected(index);
			}
			// 触发子事件显示
			if (data.get(index) != null) {
				data.get(index).onshow();
			}
		}

	}

	public interface OnPageChange {
		public void onPageSelected(int currindex);
	}

}
