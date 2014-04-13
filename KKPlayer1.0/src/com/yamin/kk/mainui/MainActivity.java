package com.yamin.kk.mainui;

import java.util.HashMap;

import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.vlc.MediaLibrary;
import org.videolan.vlc.Util;
import org.videolan.vlc.VLCCallbackTask;
import org.videolan.vlc.WeakHandler;
import org.videolan.vlc.gui.CompatErrorActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Window;
import com.yamin.kk.R;
import com.yamin.kk.adapter.PreferenceAdapter;
import com.yamin.kk.fragment.AudioFragment;
import com.yamin.kk.fragment.AudioMiniPlayer;
import com.yamin.kk.fragment.AudioPlayerFragment;
import com.yamin.kk.fragment.HomeFragment;
import com.yamin.kk.fragment.ProfileFragment;
import com.yamin.kk.fragment.SearchFragment;
import com.yamin.kk.fragment.VideoFragment;
import com.yamin.kk.residemenu.ResideMenu;
import com.yamin.kk.residemenu.ResideMenuItem;
import com.yamin.kk.service.AudioService;
import com.yamin.kk.service.AudioServiceController;
import com.yamin.kk.utils.AppManager;
import com.yamin.kk.utils.Common;
import com.yamin.kk.view.ActionSheet;
import com.yamin.kk.view.ActionSheet.OnActionSheetSelected;
import com.yamin.kk.view.CustomDialog;
import com.yamin.kk.view.CustomDialogBottom;
import com.yamin.kk.view.MyListView;

/**
 * 
 * @author yamin.cao
 * 
 */

public class MainActivity extends BaseActivity implements OnActionSheetSelected {

	private static ResideMenu resideMenu;
	private ResideMenuItem itemHome;
	private ResideMenuItem itemVideo;
	private ResideMenuItem itemAudio;
	private ResideMenuItem itemProfile;
	private LinearLayout titile_bar_ll;
	private Button search_button;
	private Button online_button;
	private TextView text_view;
	private boolean isMenOpen = false;
	protected static final String ACTION_SHOW_PROGRESSBAR = "org.videolan.vlc.gui.ShowProgressBar";
	protected static final String ACTION_HIDE_PROGRESSBAR = "org.videolan.vlc.gui.HideProgressBar";
	protected static final String ACTION_SHOW_TEXTINFO = "org.videolan.vlc.gui.ShowTextInfo";
	public static final String ACTION_SHOW_PLAYER = "org.videolan.vlc.gui.ShowPlayer";
	private static final int ACTIVITY_RESULT_PREFERENCES = 1;
	private static final int ACTIVITY_SHOW_INFOLAYOUT = 2;
	private AudioMiniPlayer mAudioPlayer;
	private AudioServiceController mAudioController;
	private View mInfoLayout;
	private ProgressBar mInfoProgress;
	private TextView mInfoText;
	private String mCurrentFragment;
	private String mCurrentFragmentName;
	private FrameLayout main_fragment;
	private boolean mScanNeeded = true;
	static LayoutInflater flater;
	private Handler mHandler = new MenuActivityHandler(this);
	private String TAG = "MainActivity";
	String[] frament_flags = { "home", "video", "audio", "profile", "search",
			"audioplayer" };
	private HashMap<String, Fragment> mFragments;
	private long exitTime = 0;
	/**/
	static Context context;
	static MyListView sosListView;
	static MyListView chfListView;
	static MyListView hcssListView;
	static SharedPreferences.Editor editor;
	static SharedPreferences pref;
	public final static int RESULT_RESCAN = RESULT_FIRST_USER + 1;
	public final static int RESULT_RESTART = RESULT_FIRST_USER + 2;
	static PreferenceAdapter adapter;
	static String[] screens;
	static String[] screens_ids;
	static String[] hardware_speeds;
	static String[] hardware_speeds_ids;
	static String[] chroma_formats;
	static String[] chroma_formats_ids;
	CustomDialog openmrlDialog;

	// Activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("TAG_001", "onCreate");
		if (!LibVlcUtil.hasCompatibleCPU(this)) {
			Log.e(TAG, LibVlcUtil.getErrorMsg());
			Intent i = new Intent(this, CompatErrorActivity.class);
			startActivity(i);
			finish();
			super.onCreate(savedInstanceState);
			return;
		}

		try {
			// Start LibVLC
			Util.getLibVlcInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
			Intent i = new Intent(this, CompatErrorActivity.class);
			i.putExtra("runtimeError", true);
			i.putExtra("message",
					"LibVLC failed to initialize (LibVlcException)");
			startActivity(i);
			finish();
			super.onCreate(savedInstanceState);
			return;
		}
		super.onCreate(savedInstanceState);
		/* Enable the indeterminate progress feature */
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		/* Init home ui */
		setContentView(R.layout.kk_main);
		context = this;
		setUpView();
		// initData();
		/* Set up the mini audio player */
		mAudioPlayer = new AudioMiniPlayer();
		mAudioController = AudioServiceController.getInstance();
		mAudioPlayer.setAudioPlayerControl(mAudioController);
		mAudioPlayer.update();

		/* Prepare the progressBar */
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SHOW_PROGRESSBAR);
		filter.addAction(ACTION_HIDE_PROGRESSBAR);
		filter.addAction(ACTION_SHOW_TEXTINFO);
		filter.addAction(ACTION_SHOW_PLAYER);
		registerReceiver(messageReceiver, filter);

		/* Reload the latest preferences */
		reloadPreferences();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.audio_mini_player, mAudioPlayer).commit();

	}

	/*
	 * private void initData() { // TODO Auto-generated method stub
	 * //screen_oritation_list
	 * =this.getResources().getStringArray(R.array.screen_orientation_list); }
	 */
	@Override
	public void initWidget() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("TAG_001", "onResume");
		mAudioController.addAudioPlayer(mAudioPlayer);
		AudioServiceController.getInstance().bindAudioService(this);
		/*
		 * FIXME: this is used to avoid having MainActivity twice in the
		 * backstack
		 */
		if (getIntent().hasExtra(AudioService.START_FROM_NOTIFICATION))
			getIntent().removeExtra(AudioService.START_FROM_NOTIFICATION);

		/* Load media items from database and storage */
		if (mScanNeeded)
			MediaLibrary.getInstance(this).loadMediaItems(this);
	}

	/**
	 * Stop audio player and save opened tab
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("TAG_001", "onPause");
		/* Check for an ongoing scan that needs to be resumed during onResume */
		mScanNeeded = MediaLibrary.getInstance(this).isWorking();
		/* Stop scanning for files */
		MediaLibrary.getInstance(this).stop();
		/* Save the tab status in pref */

		SharedPreferences.Editor editorPause = getSharedPreferences(
				"MainActivity", MODE_PRIVATE).edit();
		editorPause.putString("fragment_id", mCurrentFragment);
		editorPause.putString("fragment_name", mCurrentFragmentName);
		editorPause.commit();
		mAudioController.removeAudioPlayer(mAudioPlayer);
		AudioServiceController.getInstance().unbindAudioService(this);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// Figure out if currently-loaded fragment is a top-level fragment.
		Fragment current = getSupportFragmentManager().findFragmentById(
				R.id.main_fragment);
		boolean found = false;
		if (current != null) {
			for (int i = 0; i < frament_flags.length; i++) {
				if (frament_flags[i].equals(current.getTag())) {
					found = true;
					break;
				}
			}
		} else {
			found = true;
		}

		/**
		 * Let's see if Android recreated anything for us in the bundle. Prevent
		 * duplicate creation of fragments, since mSidebarAdapter might have
		 * been purged (losing state) when this activity was killed.
		 */
		for (int i = 0; i < frament_flags.length; i++) {
			String fragmentTag = frament_flags[i];
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					fragmentTag);
			if (fragment != null) {
				Log.d(TAG, "Restoring automatically recreated fragment \""
						+ fragmentTag + "\"");
				restoreFragment(fragmentTag, fragment);
			}
		}
		Log.d("TAG_001", "onResumeFragments");
		Log.i("TAG_001", mCurrentFragment);
		/**
		 * Restore the last view.
		 * 
		 * Replace: - null fragments (freshly opened Activity) - Wrong fragment
		 * open AND currently displayed fragment is a top-level fragment
		 * 
		 * Do not replace: - Non-sidebar fragments. It will try to remove() the
		 * currently displayed fragment (i.e. tracks) and replace it with a
		 * blank screen. (stuck menu bug)
		 */
		if (current == null
				|| (!current.getTag().equals(mCurrentFragment) && found)) {
			Log.d(TAG, "Reloading displayed fragment");
			/*
			 * Fragment ff = getFragment(mCurrentFragment); FragmentTransaction
			 * ft = getSupportFragmentManager() .beginTransaction();
			 * ft.replace(R.id.fragment_placeholder, ff, mCurrentFragment);
			 * ft.commit();
			 */
			ShowFragment(getFragment(mCurrentFragment), mCurrentFragmentName,
					mCurrentFragment);
		}

		/*
		 * if (!TextUtils.isEmpty("mCurrentFragment") &&
		 * mFragments.containsKey("mCurrentFragment")) {
		 * ShowFragment(fetchFragment(mCurrentFragment), mCurrentFragmentName,
		 * mCurrentFragment); } else { ShowFragment(new HomeFragment(),
		 * MainActivity.this.getStringById(R.string.menu_home), "home"); }
		 */
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			super.openOptionsMenu();//
			ActionSheet.showSheet(MainActivity.this, this, listener);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				AppManager.getAppManager().AppExit(this);
			}
			return true;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (resideMenu != null)

			resideMenu.clearIgnoredViewList();

		try {
			unregisterReceiver(messageReceiver);
		} catch (IllegalArgumentException e) {
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("TAG_001", "onRestart");
		/* Reload the latest preferences */
		reloadPreferences();
	}

	@Override
	public void widgetClick(View view) {
		// TODO Auto-generated method stub
		if (view == itemHome) {
			ShowFragment(getFragment(frament_flags[0]),
					MainActivity.this.getStringById(R.string.menu_home), "home");
		} else if (view == itemVideo) {
			ShowFragment(getFragment(frament_flags[1]),
					MainActivity.this.getStringById(R.string.menu_video),
					"video");
		} else if (view == itemAudio) {
			ShowFragment(getFragment(frament_flags[2]),
					MainActivity.this.getStringById(R.string.menu_audio),
					"audio");
		} else if (view == itemProfile) {
			ShowFragment(getFragment(frament_flags[3]),
					MainActivity.this.getStringById(R.string.menu_profile),
					"profile");
		} else if (view == search_button) {
			ShowFragment(getFragment(frament_flags[4]),
					MainActivity.this.getStringById(R.string.search), "search");

		} else if (view == online_button) {
			onOpenMRL();
		} else if (view == titile_bar_ll) {
			if (isMenOpen) {
				resideMenu.closeMenu();
			} else {
				resideMenu.openMenu();
			}
		}

	}

	/*
	 * private void reloadPreferences() { SharedPreferences sharedPrefs =
	 * getSharedPreferences("MainActivity", MODE_PRIVATE); mCurrentFragment =
	 * sharedPrefs.getString("fragment", "video"); }
	 */
	private void setUpView() {
		flater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		mFragments = new HashMap<String, Fragment>(frament_flags.length);
		titile_bar_ll = (LinearLayout) findViewById(R.id.title_bar_LL);
		search_button = (Button) findViewById(R.id.search_bar_menu);
		online_button = (Button) findViewById(R.id.online_menu);
		main_fragment = (FrameLayout) findViewById(R.id.main_fragment);
		text_view = (TextView) findViewById(R.id.title_bar_text);
		// attach to current activity;
		resideMenu = new ResideMenu(this);
		resideMenu.setBackground(R.drawable.menu_background);
		resideMenu.attachToActivity(this);

		resideMenu.setMenuListener(menuListener);
		// create menu items;
		itemHome = new ResideMenuItem(this, R.drawable.icon_home,
				R.string.menu_home);
		itemVideo = new ResideMenuItem(this, R.drawable.icon_video,
				R.string.menu_video);
		itemAudio = new ResideMenuItem(this, R.drawable.icon_audio,
				R.string.menu_audio);
		itemProfile = new ResideMenuItem(this, R.drawable.icon_phone,
				R.string.menu_profile);

		itemHome.setOnClickListener(this);
		itemVideo.setOnClickListener(this);
		itemAudio.setOnClickListener(this);
		itemProfile.setOnClickListener(this);

		resideMenu.addMenuItem(itemHome);
		resideMenu.addMenuItem(itemVideo);
		resideMenu.addMenuItem(itemAudio);
		resideMenu.addMenuItem(itemProfile);

		resideMenu.addIgnoredView(main_fragment);
		titile_bar_ll.setOnClickListener(this);
		search_button.setOnClickListener(this);
		online_button.setOnClickListener(this);
		/* Initialize UI variables */
		mInfoLayout = findViewById(R.id.info_layout);
		mInfoProgress = (ProgressBar) findViewById(R.id.info_progress);
		mInfoText = (TextView) findViewById(R.id.info_text);

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		return resideMenu.onInterceptTouchEvent(ev)
				|| super.dispatchTouchEvent(ev);
	}

	private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
		@Override
		public void openMenu() {
			isMenOpen = true;
		}

		@Override
		public void closeMenu() {
			isMenOpen = false;
		}
	};

	private void ShowFragment(Fragment fragmentClass, String title, String tag) {

		text_view.setText(title);

		mCurrentFragment = tag;
		mCurrentFragmentName = title;
		/*
		 * mFragments.put(tag, fragmentClass);
		 */
		ShowFragment(this, tag, fragmentClass);
	}

	public static void ShowFragment(FragmentActivity activity, String tag,
			Fragment fragment) {

		if (fragment == null) {
			Log.e("", "Cannot show a null fragment, ShowFragment(" + tag
					+ ") aborted.");
			return;
		}

		FragmentManager fm = activity.getSupportFragmentManager();

		// abort if fragment is already the current one
		Fragment current = fm.findFragmentById(R.id.main_fragment);
		if (current != null && current.getTag().equals(tag))
			return;

		// try to pop back if the fragment is already on the backstack
		if (fm.popBackStackImmediate(tag, 0))
			return;

		// fragment is not there yet, spawn a new one
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(R.anim.anim_enter_right, R.anim.anim_leave_left,
				R.anim.anim_enter_left, R.anim.anim_leave_right);

		ft.replace(R.id.main_fragment, fragment, tag);
		ft.addToBackStack(tag);
		ft.commit();
	}

	// What good method is to access resideMenu
	public ResideMenu getResideMenu() {
		if (resideMenu == null) {
			resideMenu = new ResideMenu(this);
		}
		return resideMenu;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTIVITY_RESULT_PREFERENCES) {
			if (resultCode == RESULT_RESCAN)
				MediaLibrary.getInstance(this).loadMediaItems(this, true);
		}
	}

	/*
	 * private void showInfoDialog() { final Dialog infoDialog = new
	 * Dialog(this, R.style.info_dialog);
	 * infoDialog.setContentView(R.layout.info_dialog); Button okButton =
	 * (Button) infoDialog.findViewById(R.id.ok);
	 * okButton.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View view) { CheckBox notShowAgain =
	 * (CheckBox) infoDialog .findViewById(R.id.not_show_again); if
	 * (notShowAgain.isChecked() && mSettings != null) { Editor editor =
	 * mSettings.edit(); editor.putInt(PREF_SHOW_INFO, mVersionNumber);
	 * editor.commit(); } //Close the dialog infoDialog.dismiss(); //and finally
	 * open the sliding menu if first run if (mFirstRun){ //mMenu.showMenu(); }
	 * } }); infoDialog.show(); }
	 */
	public static void showProgressBar(Context context) {
		if (context == null)
			return;
		Intent intent = new Intent();
		intent.setAction(ACTION_SHOW_PROGRESSBAR);
		context.getApplicationContext().sendBroadcast(intent);
	}

	public static void hideProgressBar(Context context) {
		if (context == null)
			return;
		Intent intent = new Intent();
		intent.setAction(ACTION_HIDE_PROGRESSBAR);
		context.getApplicationContext().sendBroadcast(intent);
	}

	public static void sendTextInfo(Context context, String info, int progress,
			int max) {
		if (context == null)
			return;
		Intent intent = new Intent();
		intent.setAction(ACTION_SHOW_TEXTINFO);
		intent.putExtra("info", info);
		intent.putExtra("progress", progress);
		intent.putExtra("max", max);
		context.getApplicationContext().sendBroadcast(intent);
	}

	public static void clearTextInfo(Context context) {
		sendTextInfo(context, null, 0, 100);
	}

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String b = intent
					.getStringExtra(AudioService.START_FROM_NOTIFICATION);
			/*
			 * From Notification by audio player
			 */
			if (b != null) {
				Log.i("TAG_001", b + "");
			}
			if (action.equalsIgnoreCase(ACTION_SHOW_PROGRESSBAR)) {
				setProgressBarIndeterminateVisibility(true);
				getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} else if (action.equalsIgnoreCase(ACTION_HIDE_PROGRESSBAR)) {
				setProgressBarIndeterminateVisibility(false);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} else if (action.equalsIgnoreCase(ACTION_SHOW_TEXTINFO)) {
				String info = intent.getStringExtra("info");
				int max = intent.getIntExtra("max", 0);
				int progress = intent.getIntExtra("progress", 100);
				mInfoText.setText(info);
				mInfoProgress.setMax(max);
				mInfoProgress.setProgress(progress);

				if (info == null) {
					/* Cancel any upcoming visibility change */
					mHandler.removeMessages(ACTIVITY_SHOW_INFOLAYOUT);
					mInfoLayout.setVisibility(View.GONE);
				} else {
					/*
					 * Slightly delay the appearance of the progress bar to
					 * avoid unnecessary flickering
					 */
					if (!mHandler.hasMessages(ACTIVITY_SHOW_INFOLAYOUT)) {
						Message m = new Message();
						m.what = ACTIVITY_SHOW_INFOLAYOUT;
						mHandler.sendMessageDelayed(m, 300);
					}
				}
			} else if (action.equalsIgnoreCase(ACTION_SHOW_PLAYER)) {
				ShowFragment(getFragment(frament_flags[5]),
						MainActivity.this
								.getStringById(R.string.menu_audio_player),
						"audioplayer");
			}
		}
	};

	private static class MenuActivityHandler extends WeakHandler<MainActivity> {
		public MenuActivityHandler(MainActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity ma = getOwner();
			if (ma == null)
				return;

			switch (msg.what) {
			case ACTIVITY_SHOW_INFOLAYOUT:
				ma.mInfoLayout.setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	@Override
	public void onClick(String whichButton) {
		// TODO Auto-generated method stub

	};

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void onOpenMRL() {
		View mrl = flater.inflate(R.layout.open_mrl, null);
		openmrlDialog = new CustomDialog(this, mrl,
				R.style.Theme_dialog);
		openmrlDialog.show();
		Button openBtn = (Button) mrl.findViewById(R.id.openBtn);
		Button cancelBtn = (Button) mrl.findViewById(R.id.cancelBtn);
		final EditText website = (EditText) mrl.findViewById(R.id.webSiteEdit);
		website.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_URI);
		openBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * Start this in a new thread as to not block the UI
				 * thread
				 */
				VLCCallbackTask task = new VLCCallbackTask(
						MainActivity.this) {
					@Override
					public void run() {
						AudioServiceController c = AudioServiceController
								.getInstance();
						String s = website.getText().toString();

						/*
						 * Use the audio player by default. If a video
						 * track is detected, then it will automatically
						 * switch to the video player. This allows us to
						 * support more types of streams (for example,
						 * RTSP and TS streaming) where ES can be
						 * dynamically adapted rather than a simple
						 * scan.
						 */
						c.load(s, false);
					}
				};
				task.execute();
			}
		});
		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openmrlDialog.dismiss();
			}
		});

	}

	/**
	 * When Android has automatically recreated a fragment from the bundle
	 * state, use this function to 'restore' the recreated fragment into this
	 * sidebar adapter to prevent it from trying to create the same fragment
	 * again.
	 * 
	 * @param id
	 *            ID of the fragment
	 * @param f
	 *            The fragment itself
	 */
	/*
	 * public void restoreFragment(String id, Fragment f) { if (f == null) {
	 * Log.e(TAG, "Can't set null fragment for " + id + "!"); return; }
	 * mFragments.put(id, f); // if Android added it, it's been implicitly added
	 * already... }
	 * 
	 * private Fragment getFragment(String id) { return fetchFragment(id); }
	 * 
	 * public Fragment fetchFragment(String id) {
	 * 
	 * if (mFragments.containsKey(id) && mFragments.get(id) != null) { return
	 * mFragments.get(id); } Fragment f = null; if (id.equals("audio")) { f =
	 * new AudioFragment(); } else if (id.equals("video")) { f = new
	 * VideoFragment(); } else if (id.endsWith("profile")) { f = new
	 * ProfileFragment(); } else if (id.endsWith("home")) { f = new
	 * HomeFragment(); } f.setRetainInstance(true); mFragments.put(id, f);
	 * return f; }
	 */
	private void reloadPreferences() {
		SharedPreferences sharedPrefs = getSharedPreferences("MainActivity",
				MODE_PRIVATE);
		mCurrentFragment = sharedPrefs.getString("fragment_id", "");
		mCurrentFragmentName = sharedPrefs.getString("fragment_name", "");
	}

	/*
	 * Tools for find loaded fragment
	 */
	private String getStringById(int id) {
		String s = this.getResources().getString(id);
		return s;

	}

	private Fragment getFragment(String id) {
		return fetchFragment(id);
	}

	public Fragment fetchFragment(String id) {
		Fragment f;
		if (mFragments.containsKey(id) && mFragments.get(id) != null) {
			return mFragments.get(id);
		}

		if (id.equals(frament_flags[0])) {
			f = new HomeFragment();
		} else if (id.equals(frament_flags[1])) {
			f = new VideoFragment();
		} else if (id.equals(frament_flags[2])) {
			f = new AudioFragment();
		} else if (id.equals(frament_flags[3])) {
			f = new ProfileFragment();
		} else if (id.equals(frament_flags[4])) {
			f = new SearchFragment();
		} else {
			f = new AudioPlayerFragment();
		}
		f.setRetainInstance(true);
		mFragments.put(id, f);
		return f;

	}

	/**
	 * When Android has automatically recreated a fragment from the bundle
	 * state, use this function to 'restore' the recreated fragment into this
	 * sidebar adapter to prevent it from trying to create the same fragment
	 * again.
	 * 
	 * @param id
	 *            ID of the fragment
	 * @param f
	 *            The fragment itself
	 */
	public void restoreFragment(String id, Fragment f) {
		if (f == null) {
			Log.e(TAG, "Can't set null fragment for " + id + "!");
			return;
		}
		mFragments.put(id, f);
		// if Android added it, it's been implicitly added already...
	}

	public static void popuScreenSettingsDialog() {

		View sos = flater.inflate(R.layout.screen_oritation_settings, null);
		sosListView = (MyListView) sos
				.findViewById(R.id.screen_oritation_listview);
		screens = context.getResources().getStringArray(
				R.array.screen_orientation_list);
		screens_ids = context.getResources().getStringArray(
				R.array.screen_orientation_values);
		adapter = new PreferenceAdapter(context, screens);
		pref = context.getSharedPreferences(Common.NAME, MODE_PRIVATE);
		String select = pref.getString("screen_orientation_value", "");
		for (int i = 0; i < screens_ids.length; i++) {
			if (select.equals(screens_ids[i])) {
				adapter.setItemState(i);
			}
		}
		sosListView.setAdapter(adapter);
		sosListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						pref = context.getSharedPreferences(Common.NAME,
								MODE_PRIVATE);
						editor = pref.edit();
						editor.putString("screen_orientation_value",
								screens_ids[arg2]);
						editor.commit();
						adapter.setItemState(arg2);
						adapter.notifyDataSetChanged();
					}
				});
		CustomDialogBottom.showSheet(context, sos, listener);

	}

	public static void popuHardwareSpeedDialog() {
		View hcss = flater.inflate(R.layout.hard_codec_speed_settings, null);
		hcssListView = (MyListView) hcss.findViewById(R.id.hard_codec_listview);
		hardware_speeds = context.getResources().getStringArray(
				R.array.hardware_acceleration_list);
		hardware_speeds_ids = context.getResources().getStringArray(
				R.array.screen_orientation_values);
		adapter = new PreferenceAdapter(context, hardware_speeds);
		pref = context.getSharedPreferences(Common.NAME, MODE_PRIVATE);
		String select = pref.getString("hardware_acceleration", "");
		for (int i = 0; i < hardware_speeds_ids.length; i++) {
			if (select.equals(hardware_speeds_ids[i])) {
				adapter.setItemState(i);
			}
		}
		hcssListView.setAdapter(adapter);
		hcssListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						pref = context.getSharedPreferences(Common.NAME,
								MODE_PRIVATE);
						editor = pref.edit();
						editor.putString("hardware_acceleration",
								hardware_speeds_ids[arg2]);
						editor.commit();
						try {
							Util.getLibVlcInstance().setHardwareAcceleration(
									Integer.valueOf(pref.getString(
											"hardware_acceleration",
											hardware_speeds_ids[arg2])));
						} catch (LibVlcException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						adapter.setItemState(arg2);
						adapter.notifyDataSetChanged();
					}
				});
		CustomDialogBottom.showSheet(context, hcss, listener);
	}

	public static void popuChromaFormatsDialog() {
		View chf = flater.inflate(R.layout.chroma_formats_settings, null);
		chfListView = (MyListView) chf
				.findViewById(R.id.chroma_formats_listview);
		chroma_formats = context.getResources().getStringArray(
				R.array.chroma_formats);
		chroma_formats_ids = context.getResources().getStringArray(
				R.array.chroma_formats_values);
		adapter = new PreferenceAdapter(context, chroma_formats);
		pref = context.getSharedPreferences(Common.NAME, MODE_PRIVATE);
		String select = pref.getString("chroma_format", "");
		for (int i = 0; i < chroma_formats_ids.length; i++) {
			if (select.equals(chroma_formats_ids[i])) {
				adapter.setItemState(i);
			}
		}
		chfListView.setAdapter(adapter);
		chfListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						pref = context.getSharedPreferences(Common.NAME,
								MODE_PRIVATE);
						editor = pref.edit();
						editor.putString("chroma_format",
								chroma_formats_ids[arg2]);
						editor.commit();
						try {
							Util.getLibVlcInstance().setChroma(
									pref.getString("chroma_format",
											chroma_formats_ids[arg2]));
						} catch (LibVlcException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						adapter.setItemState(arg2);
						adapter.notifyDataSetChanged();
					}
				});
		CustomDialogBottom.showSheet(context, chf, listener);
	}

	private static OnCancelListener listener = new DialogInterface.OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			if (dialog != null) {
				dialog.dismiss();
			}
		}
	};

}
