package com.yamin.kk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yamin.kk.R;

public class PreferenceAdapter extends BaseAdapter {

	private String[] lists;
	private LayoutInflater mLayoutInflater = null;
	int itemState;

	public PreferenceAdapter(Context context,String[] lists) {
		super();
		this.lists = lists;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getItemState() {
		return itemState;
	}

	public void setItemState(int itemState) {
		this.itemState = itemState;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.length;
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return lists[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			view = mLayoutInflater.inflate(R.layout.list_item, null);
			holder = new ViewHolder(view);
			/*
			 * holder.imgFileIcon=(ImageView)view.findViewById(R.id.
			 * imgSearchFileIcon);
			 * holder.imgFileSelectIcon=(ImageView)view.findViewById
			 * (R.id.imFileSelectIcon);
			 */
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		String text = getItem(position);
		// TODO

		holder.listText.setText(text);

		updateIsChecked(position, holder.listImgIcon);
		return view;
	}

	static class ViewHolder {
		ImageView listImgIcon;
		TextView listText;

		public ViewHolder(View view) {
			listImgIcon = (ImageView) view.findViewById(R.id.list_item_checked);
			listText = (TextView) view.findViewById(R.id.list_item_textview);

		}
	}

	public void updateIsChecked(int position, ImageView select) {

		if (itemState == position) {
			select.setVisibility(View.VISIBLE);
		} else {
			select.setVisibility(View.GONE);
		}

	}
}
