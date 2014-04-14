package com.yamin.kk.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.yamin.kk.R;

public class CustomDialogBottom {
	
	private CustomDialogBottom() {
	}

	public static Dialog showSheet(Context context,View v,OnCancelListener cancelListener) {
		final Dialog dlg = new Dialog(context, R.style.ActionSheet);
		final int cFullFillWidth = 10000;
		v.setMinimumWidth(cFullFillWidth);
		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		dlg.onWindowAttributesChanged(lp);
		dlg.setCanceledOnTouchOutside(true);
		if (cancelListener != null)
			dlg.setOnCancelListener(cancelListener);
		dlg.setContentView(v);
		dlg.show();
		return dlg;
	}

}
