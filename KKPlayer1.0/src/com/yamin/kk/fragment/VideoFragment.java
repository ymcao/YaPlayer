package com.yamin.kk.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.yamin.kk.R;
import com.yamin.kk.adapter.VideoListAdapter;
import com.yamin.kk.mainui.MainActivity;
import com.yamin.kk.mainui.MediaInfoActivity;
import com.yamin.kk.mainui.VideoPlayerActivity;
import com.yamin.kk.utils.Thumbnailer;
import com.yamin.kk.vlc.Media;
import com.yamin.kk.vlc.MediaDatabase;
import com.yamin.kk.vlc.MediaGroup;
import com.yamin.kk.vlc.MediaLibrary;
import com.yamin.kk.vlc.Util;
import com.yamin.kk.vlc.VlcRunnable;
import com.yamin.kk.vlc.WeakHandler;
import com.yamin.kk.vlc.gui.CommonDialogs;
import com.yamin.kk.vlc.interfaces.ISortable;

public class VideoFragment extends SherlockGridFragment implements ISortable {

	private Activity mActivity;
	public final static String TAG = "KKPlayer/VideoFragment";

	protected static final String ACTION_SCAN_START = "org.videolan.vlc.gui.ScanStart";
	protected static final String ACTION_SCAN_STOP = "org.videolan.vlc.gui.ScanStop";
	protected static final int UPDATE_ITEM = 0;

	/* Constants used to switch from Grid to List and vice versa */
	// FIXME If you know a way to do this in pure XML please do it!
	private static final int GRID_ITEM_WIDTH_DP = 156;
	private static final int GRID_HORIZONTAL_SPACING_DP = 20;
	private static final int GRID_VERTICAL_SPACING_DP = 20;
	private static final int GRID_STRETCH_MODE = GridView.STRETCH_COLUMN_WIDTH;
	private static final int LIST_HORIZONTAL_SPACING_DP = 0;
	private static final int LIST_VERTICAL_SPACING_DP = 10;
	private static final int LIST_STRETCH_MODE = GridView.STRETCH_COLUMN_WIDTH;
	protected LinearLayout mLayoutFlipperLoading;
	protected TextView mTextViewNomedia;
	protected Media mItemToUpdate;
	protected String mGroup;
	protected final CyclicBarrier mBarrier = new CyclicBarrier(2);

	private VideoListAdapter mVideoAdapter;
	private MediaLibrary mMediaLibrary;
	private Thumbnailer mThumbnailer;
	private VideoGridAnimator mAnimator;
	/*
	private FrameLayout video_framelayout;
    private ResideMenu resideMenu;
    MainActivity parentActivity;
    */
	/* All subclasses of Fragment must include a public empty constructor. */
	public VideoFragment() {}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mVideoAdapter = new VideoListAdapter(getActivity(), this);
		mMediaLibrary = MediaLibrary.getInstance(getActivity());
		setListAdapter(mVideoAdapter);

		/* Load the thumbnailer */
		if (mActivity != null)
			mThumbnailer = new Thumbnailer(mActivity, mActivity
					.getWindowManager().getDefaultDisplay());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//
		View v = inflater.inflate(R.layout.video_grid, container, false);

		// init the information for the scan (1/2)
		mLayoutFlipperLoading = (LinearLayout) v
				.findViewById(R.id.layout_flipper_loading);
		mTextViewNomedia = (TextView) v.findViewById(R.id.textview_nomedia);
		/*
		video_framelayout= (FrameLayout) v
				.findViewById(R.id.video_framelayout);
		//Add ignore swipe area
		parentActivity = (MainActivity) getActivity();
	    resideMenu = parentActivity.getResideMenu();
	    if(resideMenu!=null)
	    	resideMenu.addIgnoredView(video_framelayout);
	    */
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(getGridView());

		// Init the information for the scan (2/2)
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SCAN_START);
		filter.addAction(ACTION_SCAN_STOP);
		getActivity()
				.registerReceiver(messageReceiverVideoListFragment, filter);
		Log.i(TAG,
				"mMediaLibrary.isWorking() "
						+ Boolean.toString(mMediaLibrary.isWorking()));
		if (mMediaLibrary.isWorking()) {
			actionScanStart(getActivity().getApplicationContext());
		}

		mAnimator = new VideoGridAnimator(getGridView());
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onContextPopupMenu(View anchor, final int position) {
		if (!Util.isHoneycombOrLater()) {
			// Call the "classic" context menu
			anchor.performLongClick();
			return;
		}

		PopupMenu popupMenu = new PopupMenu(getActivity(), anchor);
		popupMenu.getMenuInflater().inflate(R.menu.video_list,
				popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return handleContextItemSelected(item, position);
			}
		});
		popupMenu.show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMediaLibrary.removeUpdateHandler(mHandler);

		/* Stop the thumbnailer */
		if (mThumbnailer != null)
			mThumbnailer.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Get & set times
		HashMap<String, Long> times = MediaDatabase.getInstance(getActivity())
				.getVideoTimes(getActivity());
		mVideoAdapter.setTimes(times);
		mVideoAdapter.notifyDataSetChanged();
		updateList();
		mMediaLibrary.addUpdateHandler(mHandler);
		updateViewMode();
		mAnimator.animate();

		/* Start the thumbnailer */
		if (mThumbnailer != null)
			mThumbnailer.start(this);
	}

	@Override
	public void onDestroyView() {
		getActivity().unregisterReceiver(messageReceiverVideoListFragment);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mThumbnailer != null)
			mThumbnailer.clearJobs();
		mBarrier.reset();
		mVideoAdapter.clear();
	}

	private boolean hasSpaceForGrid(View v) {
		final Activity activity = getActivity();
		if (activity == null)
			return true;

		final GridView grid = (GridView) v.findViewById(android.R.id.list);

		DisplayMetrics outMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

		final int itemWidth = Util.convertDpToPx(GRID_ITEM_WIDTH_DP);
		final int horizontalspacing = Util
				.convertDpToPx(GRID_HORIZONTAL_SPACING_DP);
		final int width = grid.getPaddingLeft() + grid.getPaddingRight()
				+ horizontalspacing + (itemWidth * 2);
		if (width < outMetrics.widthPixels)
			return true;
		return false;
	}

	private void updateViewMode() {
		if (getView() == null || getActivity() == null) {
			Log.w(TAG, "Unable to setup the view");
			return;
		}

		GridView gv = (GridView) getView().findViewById(android.R.id.list);

		// Compute the left/right padding dynamically
		DisplayMetrics outMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(outMetrics);
		int sidePadding = (int) (outMetrics.widthPixels / 100
				* Math.pow(outMetrics.density, 3) / 2);
		sidePadding = Math.max(0, Math.min(100, sidePadding));
		gv.setPadding(sidePadding, gv.getPaddingTop(), sidePadding,
				gv.getPaddingBottom());

		// Select between grid or list
		if (hasSpaceForGrid(getView())) {
			Log.d(TAG, "Switching to grid mode");
			gv.setNumColumns(GridView.AUTO_FIT);
			gv.setStretchMode(GRID_STRETCH_MODE);
			gv.setHorizontalSpacing(Util
					.convertDpToPx(GRID_HORIZONTAL_SPACING_DP));
			gv.setVerticalSpacing(Util.convertDpToPx(GRID_VERTICAL_SPACING_DP));
			gv.setColumnWidth(Util.convertDpToPx(GRID_ITEM_WIDTH_DP));
			mVideoAdapter.setListMode(false);
		} else {
			Log.d(TAG, "Switching to list mode");
			gv.setNumColumns(1);
			gv.setStretchMode(LIST_STRETCH_MODE);
			gv.setHorizontalSpacing(LIST_HORIZONTAL_SPACING_DP);
			gv.setVerticalSpacing(Util.convertDpToPx(LIST_VERTICAL_SPACING_DP));
			mVideoAdapter.setListMode(true);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
				|| newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			updateViewMode();
		}
	}

	@Override
	public void onGridItemClick(GridView l, View v, int position, long id) {
		Media media = (Media) getListAdapter().getItem(position);
		if (media instanceof MediaGroup) {
			VideoFragment videoList = new VideoFragment();
			videoList.setGroup(media.getTitle());
			MainActivity.ShowFragment(getActivity(), "videolist", videoList);
		} else
			playVideo(media, false);
		super.onGridItemClick(l, v, position, id);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	/**
	 * Handle changes on the list
	 */
	private Handler mHandler = new VideoListHandler(this);

	private static class VideoListHandler extends WeakHandler<VideoFragment> {
		public VideoListHandler(VideoFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VideoFragment fragment = getOwner();
			if (fragment == null)
				return;

			switch (msg.what) {
			case UPDATE_ITEM:
				fragment.updateItem();
				break;
			case MediaLibrary.MEDIA_ITEMS_UPDATED:
				// Don't update the adapter while the layout animation is
				// running
				if (fragment.mAnimator.isAnimationDone())
					fragment.updateList();
				else
					sendEmptyMessageDelayed(msg.what, 500);
				break;
			}
		}
	};

	private void updateItem() {
		mVideoAdapter.update(mItemToUpdate);
		try {
			mBarrier.await();
		} catch (InterruptedException e) {
		} catch (BrokenBarrierException e) {
		}
	}

	private void updateList() {
		List<Media> itemList = mMediaLibrary.getVideoItems();

		if (mThumbnailer != null)
			mThumbnailer.clearJobs();
		else
			Log.w(TAG, "Can't generate thumbnails, the thumbnailer is missing");

		mVideoAdapter.clear();

		if (itemList.size() > 0) {
			if (mGroup != null || itemList.size() <= 10) {
				for (Media item : itemList) {
					if (mGroup == null || item.getTitle().startsWith(mGroup)) {
						mVideoAdapter.add(item);
						if (mThumbnailer != null)
							mThumbnailer.addJob(item);
					}
				}
			} else {
				List<MediaGroup> groups = MediaGroup.group(itemList);
				for (MediaGroup item : groups) {
					mVideoAdapter.add(item.getMedia());
					if (mThumbnailer != null)
						mThumbnailer.addJob(item);
				}
			}
			mVideoAdapter.sort();
		}
	}

	@Override
	public void sortBy(int sortby) {
		mVideoAdapter.sortBy(sortby);
	}

	public void setItemToUpdate(Media item) {
		mItemToUpdate = item;
		mHandler.sendEmptyMessage(UPDATE_ITEM);
	}

	public void setGroup(String prefix) {
		mGroup = prefix;
	}

	public void await() throws InterruptedException, BrokenBarrierException {
		mBarrier.await();
	}

	public void resetBarrier() {
		mBarrier.reset();
	}

	private final BroadcastReceiver messageReceiverVideoListFragment = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("TAG_001",action+"");
			if (action.equalsIgnoreCase(ACTION_SCAN_START)) {
				
				mLayoutFlipperLoading.setVisibility(View.VISIBLE);
				mTextViewNomedia.setVisibility(View.INVISIBLE);
			} else if (action.equalsIgnoreCase(ACTION_SCAN_STOP)) {
				
				mLayoutFlipperLoading.setVisibility(View.INVISIBLE);
				mTextViewNomedia.setVisibility(View.VISIBLE);
			}
		}
	};

	public static void actionScanStart(Context context) {
		if (context == null)
			return;
		Intent intent = new Intent();
		intent.setAction(ACTION_SCAN_START);
		context.getApplicationContext().sendBroadcast(intent);
	}

	public static void actionScanStop(Context context) {
		if (context == null)
			return;
		Intent intent = new Intent();
		intent.setAction(ACTION_SCAN_STOP);
		context.getApplicationContext().sendBroadcast(intent);
	}

	private boolean handleContextItemSelected(MenuItem menu, int position) {
		Media media = mVideoAdapter.getItem(position);
		if (media instanceof MediaGroup)
			return true;
		switch (menu.getItemId()) {
		case R.id.video_list_play:
			playVideo(media, false);
			return true;
		case R.id.video_list_play_from_start:
			playVideo(media, true);
			return true;

		case R.id.video_list_info:
			Intent intent = new Intent(getActivity(), MediaInfoActivity.class);
			intent.putExtra("itemLocation", media.getLocation());
			startActivity(intent);
			return true;
		case R.id.video_list_delete:
			AlertDialog alertDialog = CommonDialogs.deleteMedia(getActivity(),
					media.getLocation(), new VlcRunnable(media) {
						@Override
						public void run(Object o) {
							Media media = (Media) o;
							mMediaLibrary.getMediaItems().remove(media);
							mVideoAdapter.remove(media);
						}
					});
			alertDialog.show();
			return true;
		}
		return false;
	}

	protected void playVideo(Media media, boolean fromStart) {
		VideoPlayerActivity
				.start(getActivity(), media.getLocation(), fromStart);
	}

}
