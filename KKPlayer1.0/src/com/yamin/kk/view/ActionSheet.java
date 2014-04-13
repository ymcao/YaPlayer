package com.yamin.kk.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yamin.kk.R;
import com.yamin.kk.utils.AppManager;

public class ActionSheet {

	public interface OnActionSheetSelected {
		void onClick(String whichButton);
	}

	private ActionSheet() {
	}

	public static Dialog showSheet(final Context context, final OnActionSheetSelected actionSheetSelected,
			OnCancelListener cancelListener) {
		final Dialog dlg = new Dialog(context, R.style.ActionSheet);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.actionsheet, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);
        /*
		TextView sortby = (TextView) layout.findViewById(R.id.sortby);
		TextView refresh = (TextView) layout.findViewById(R.id.refresh);
		TextView equalizer = (TextView) layout.findViewById(R.id.equalizer);
		TextView preferences = (TextView) layout.findViewById(R.id.preferences);
		*/
		TextView quit = (TextView) layout.findViewById(R.id.quit);
		/*
		sortby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick("sortby");
				dlg.dismiss();
			}
		});

		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick("refresh");
				dlg.dismiss();
			}
		});
		equalizer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick("equalizer");
				dlg.dismiss();
			}
		});

		preferences.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick("preferences");
				dlg.dismiss();
			}
		});
        */
		quit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick("quit");
				dlg.dismiss();
				AppManager.getAppManager().AppExit(context);
			}
		});


		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		dlg.onWindowAttributesChanged(lp);
		dlg.setCanceledOnTouchOutside(false);
		if (cancelListener != null)
			dlg.setOnCancelListener(cancelListener);

		dlg.setContentView(layout);
		dlg.show();
		return dlg;
	}

}
