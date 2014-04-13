package com.yamin.kk.fragment;

import org.videolan.libvlc.LibVLC;
import org.videolan.vlc.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.yamin.kk.R;
import com.yamin.kk.adapter.HistoryAdapter;
import com.yamin.kk.mainui.MainActivity;
import com.yamin.kk.service.AudioServiceController;
import com.yamin.kk.utils.Common;
import com.yamin.kk.view.FlingViewGroup;
import com.yamin.kk.view.SwitchButton;

public class ProfileFragment extends SherlockListFragment {
	private TabHost mTabHost;
	FlingViewGroup mFlingViewGroup;
	private int mCurrentTab = 0;
	SwitchButton bright_gesture_switch;
	//SwitchButton handset_switch;
	SwitchButton backforward_switch;
	RelativeLayout hardwareseedLayout;
	/*
	RelativeLayout textencodingLayout;
	*/
	RelativeLayout chroma_format_settings;
	RelativeLayout screen_oritation_settings;
	Context context;
	SharedPreferences pref;
    SharedPreferences.Editor editor;
    private HistoryAdapter mHistoryAdapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context=getActivity().getApplicationContext();
        mHistoryAdapter = new HistoryAdapter(getActivity());
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.profile, container, false);
		setListAdapter(mHistoryAdapter);
		mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		mFlingViewGroup = (FlingViewGroup) v
				.findViewById(R.id.fling_view_group);
		mTabHost.setup();

		addNewTab(mTabHost, "settings", getResources().getString(R.string.menu_settings));
		addNewTab(mTabHost, "history",
				getResources().getString(R.string.history));

		mTabHost.setCurrentTab(mCurrentTab);
		mFlingViewGroup.snapToScreen(mCurrentTab);

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				mCurrentTab = mTabHost.getCurrentTab();
				mFlingViewGroup.smoothScrollTo(mCurrentTab);
			}
		});

		mFlingViewGroup
				.setOnViewSwitchedListener(new FlingViewGroup.ViewSwitchListener() {
					@Override
					public void onSwitching(float progress) {
					}

					@Override
					public void onSwitched(int position) {
						mTabHost.setCurrentTab(position);
					}

					@Override
					public void onTouchDown() {
					}

					@Override
					public void onTouchUp() {
					}

					@Override
					public void onTouchClick() {
					}
				});
		/**  */
		hardwareseedLayout=(RelativeLayout) v.findViewById(R.id.hardware_speed_rl);
		//textencodingLayout=(RelativeLayout) v.findViewById(R.id.text_encoding_rl);
		chroma_format_settings=(RelativeLayout) v.findViewById(R.id.chroma_format_rl);
		screen_oritation_settings=(RelativeLayout) v.findViewById(R.id.screen_oritation_rl);
		backforward_switch = (SwitchButton) v.findViewById(R.id.forward_backward_switch);
		bright_gesture_switch = (SwitchButton) v.findViewById(R.id.brightness_switch);
		//handset_switch = (SwitchButton) v.findViewById(R.id.headset_switch);
		pref = context.getSharedPreferences(Common.NAME,context.MODE_PRIVATE);
		boolean isBackForward=pref.getBoolean("enable_brightness_gesture", true);
		boolean bright_gesture=pref.getBoolean("enable_jump_buttons", false);
		backforward_switch.setChecked(isBackForward);
		bright_gesture_switch.setChecked(bright_gesture);
		
		backforward_switch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						pref = context.getSharedPreferences(Common.NAME,context.MODE_PRIVATE);
						editor=pref.edit();
						editor.putBoolean("enable_jump_buttons", isChecked);
						editor.commit();
					}
				});
		/* 屏幕左侧滑动调节屏幕亮度 */
		bright_gesture_switch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						pref = context.getSharedPreferences(Common.NAME,context.MODE_PRIVATE);
						editor=pref.edit();
						editor.putBoolean("enable_brightness_gesture", isChecked);
						editor.commit();
					}
				});
		/*硬件加速选项*/
		hardwareseedLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.popuHardwareSpeedDialog();
			}
		});
		/*
		//字母编码格式
		textencodingLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		*/
		//画面输出格式
		chroma_format_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.popuChromaFormatsDialog();
			}
		});
		//视频屏幕方向
		screen_oritation_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.popuScreenSettingsDialog();
				
			}
		});

		return v;
	}
	private class DummyContentFactory implements TabHost.TabContentFactory {
        private final Context mContext;
        public DummyContentFactory(Context ctx) {
            mContext = ctx;
        }
        @Override
        public View createTabContent(String tag) {
            View dummy = new View(mContext);
            return dummy;
        }
    }

    private void addNewTab(TabHost tabHost, String tag, String title) {
        DummyContentFactory dcf = new DummyContentFactory(tabHost.getContext());
        TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(getNewTabIndicator(tabHost.getContext(), title));
        tabSpec.setContent(dcf);
        tabHost.addTab(tabSpec);
    }

    private View getNewTabIndicator(Context context, String title) {
        View v = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        TextView tv = (TextView) v.findViewById(R.id.textView);
        tv.setText(title);
        return v;
    }

    public static String getVersion(Context ctx) {
        String versionName = "";
        PackageInfo packageInfo;
        try {
            packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versionName = "v" + packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    @Override
    public void onListItemClick(ListView l, View v, int p, long id) {
        playListIndex(p);
    }

    private void playListIndex(int position) {
        AudioServiceController audioController = AudioServiceController.getInstance();
        LibVLC.getExistingInstance().setMediaList();
        audioController.playIndex(position);
    }
    public void refresh() {

        if( mHistoryAdapter != null )
            mHistoryAdapter.refresh();
    }
}
