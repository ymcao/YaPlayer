package com.yamin.kk.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.yamin.kk.R;
import com.yamin.kk.adapter.AudioListAdapter;
import com.yamin.kk.adapter.AudioPlaylistAdapter;
import com.yamin.kk.mainui.MainActivity;
import com.yamin.kk.service.AudioServiceController;
import com.yamin.kk.view.KKViewPager;
import com.yamin.kk.view.KKViewPager.OnPageChange;
import com.yamin.kk.view.KKViewPagerCotroller;
import com.yamin.kk.vlc.Media;
import com.yamin.kk.vlc.MediaLibrary;
import com.yamin.kk.vlc.WeakHandler;
import com.yamin.kk.vlc.interfaces.ISortable;

public class AudioFragment extends SherlockFragment implements ISortable {

	public final static String TAG = "KKPlayer/AudioBrowserFragment";

	private AudioServiceController mAudioController;
	private MediaLibrary mMediaLibrary;

	private AudioListAdapter mSongsAdapter;
	private AudioPlaylistAdapter mArtistsAdapter;
	private AudioPlaylistAdapter mAlbumsAdapter;
	private AudioPlaylistAdapter mGenresAdapter;

	public final static int SORT_BY_TITLE = 0;
	public final static int SORT_BY_LENGTH = 1;
	private boolean mSortReverse = false;
	private int mSortBy = SORT_BY_TITLE;

	public final static int MODE_TOTAL = 4; // Number of audio browser modes
	public final static int MODE_ARTIST = 0;
	public final static int MODE_ALBUM = 1;
	public final static int MODE_SONG = 2;
	public final static int MODE_GENRE = 3;
	ArrayList<KKViewPagerCotroller> views;
	ListView songsList;
	ExpandableListView artistList;
	ExpandableListView albumList;
	ExpandableListView genreList;
	KKViewPager myviewpager;
	view_Albums albums;
	view_Songs songs;
	view_Artists artists;
	view_Genres genres;
	int position = 0;

	/*
	 * private LinearLayout audio_framelayout; private ResideMenu resideMenu;
	 * MainActivity parentActivity;
	 */
	/* All subclasses of Fragment must include a public empty constructor. */
	public AudioFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAudioController = AudioServiceController.getInstance();
		mMediaLibrary = MediaLibrary.getInstance(getActivity());
		mSongsAdapter = new AudioListAdapter(getActivity());
		mArtistsAdapter = new AudioPlaylistAdapter(getActivity(),
				R.plurals.albums_quantity, R.plurals.songs_quantity);
		mAlbumsAdapter = new AudioPlaylistAdapter(getActivity(),
				R.plurals.songs_quantity, R.plurals.songs_quantity);
		mGenresAdapter = new AudioPlaylistAdapter(getActivity(),
				R.plurals.albums_quantity, R.plurals.songs_quantity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//
		View v = inflater.inflate(R.layout.audio_browser, container, false);

		// registerForContextMenu(songsList);
		// registerForContextMenu(artistList);
		// registerForContextMenu(albumList);
		// registerForContextMenu(genreList);

		myviewpager = (KKViewPager) v.findViewById(R.id.myiewpager);
		// myviewpager.setCurrcolor(0xff505050);// 
		// myviewpager.setnoCurrcolor(0xffAFAFAF);//
		// myviewpager.setCurrimage(R.drawable.ic_launcher);//

		albums = new view_Albums(getActivity());
		songs = new view_Songs(getActivity());
		artists = new view_Artists(getActivity());
		genres = new view_Genres(getActivity());

		// ����view
		views = new ArrayList<KKViewPagerCotroller>();
		views.add(artists);
		views.add(albums);
		views.add(songs);
		views.add(genres);
		myviewpager.setViews(views, 0);

		myviewpager.setOnPageChangeListener(new OnPageChange() {
			@Override
			public void onPageSelected(int currindex) {
				// 
				position = currindex;
				Log.i(TAG, "onPageSelected:" + currindex);
			}
		});
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		updateLists();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMediaLibrary.removeUpdateHandler(mHandler);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMediaLibrary.addUpdateHandler(mHandler);
	}

	OnItemClickListener songListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int p, long id) {
			mAudioController.load(mSongsAdapter.getLocations(), p);
			AudioPlayerFragment.start(getActivity());
		}
	};

	OnGroupClickListener playlistListener = new OnGroupClickListener() {
		@Override
		public boolean onGroupClick(ExpandableListView elv, View v,
				int groupPosition, long id) {
			AudioPlaylistAdapter adapter = (AudioPlaylistAdapter) elv
					.getExpandableListAdapter();
			if (adapter.getChildrenCount(groupPosition) > 2)
				return false;

			String name = adapter.getGroup(groupPosition);

			AudioListFragment audioList = new AudioListFragment();
			Bundle b = new Bundle();
			b.putString(AudioListFragment.EXTRA_NAME, name);
			b.putString(AudioListFragment.EXTRA_NAME2, null);
			b.putInt(AudioListFragment.EXTRA_MODE, position);
			audioList.setArguments(b);

			MainActivity.ShowFragment(getActivity(), "tracks", audioList);
			return true;
		}
	};

	OnChildClickListener playlistChildListener = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView elv, View v,
				int groupPosition, int childPosition, long id) {
			AudioPlaylistAdapter adapter = (AudioPlaylistAdapter) elv
					.getExpandableListAdapter();
			String name = adapter.getGroup(groupPosition);
			String child = adapter.getChild(groupPosition, childPosition);

			AudioListFragment audioList = new AudioListFragment();
			Bundle b = new Bundle();
			b.putString(AudioListFragment.EXTRA_NAME, name);
			b.putString(AudioListFragment.EXTRA_NAME2, child);
			b.putInt(AudioListFragment.EXTRA_MODE, position);
			audioList.setArguments(b);

			MainActivity.ShowFragment(getActivity(), "tracks", audioList);
			return false;
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSongsAdapter.clear();
		mArtistsAdapter.clear();
		mAlbumsAdapter.clear();
		mGenresAdapter.clear();
	}

	/**
	 * Handle changes on the list
	 */
	private Handler mHandler = new AudioBrowserHandler(this);

	private static class AudioBrowserHandler extends WeakHandler<AudioFragment> {
		public AudioBrowserHandler(AudioFragment owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			AudioFragment fragment = getOwner();
			if (fragment == null)
				return;

			switch (msg.what) {
			case MediaLibrary.MEDIA_ITEMS_UPDATED:
				fragment.updateLists();
				break;
			}
		}
	};

	private final Comparator<Media> byName = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			return String.CASE_INSENSITIVE_ORDER.compare(m1.getTitle(),
					m2.getTitle());
		};
	};

	private final Comparator<Media> byMRL = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			return String.CASE_INSENSITIVE_ORDER.compare(m1.getLocation(),
					m2.getLocation());
		};
	};

	private final Comparator<Media> byLength = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			if (m1.getLength() > m2.getLength())
				return -1;
			if (m1.getLength() < m2.getLength())
				return 1;
			else
				return 0;
		};
	};

	private final Comparator<Media> byAlbum = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			int res = String.CASE_INSENSITIVE_ORDER.compare(m1.getAlbum(),
					m2.getAlbum());
			if (res == 0)
				res = byMRL.compare(m1, m2);
			return res;
		};
	};

	private final Comparator<Media> byArtist = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			int res = String.CASE_INSENSITIVE_ORDER.compare(m1.getArtist(),
					m2.getArtist());
			if (res == 0)
				res = byAlbum.compare(m1, m2);
			return res;
		};
	};

	private final Comparator<Media> byGenre = new Comparator<Media>() {
		@Override
		public int compare(Media m1, Media m2) {
			int res = String.CASE_INSENSITIVE_ORDER.compare(m1.getGenre(),
					m2.getGenre());
			if (res == 0)
				res = byArtist.compare(m1, m2);
			return res;
		};
	};

	private void updateLists() {
		List<Media> audioList = MediaLibrary.getInstance(getActivity())
				.getAudioItems();
		mSongsAdapter.clear();
		mArtistsAdapter.clear();
		mAlbumsAdapter.clear();
		mGenresAdapter.clear();

		switch (mSortBy) {
		case SORT_BY_LENGTH:
			Collections.sort(audioList, byLength);
			break;
		case SORT_BY_TITLE:
		default:
			Collections.sort(audioList, byName);
			break;
		}
		if (mSortReverse) {
			Collections.reverse(audioList);
		}
		for (int i = 0; i < audioList.size(); i++)
			mSongsAdapter.add(audioList.get(i));

		Collections.sort(audioList, byArtist);
		for (int i = 0; i < audioList.size(); i++) {
			Media media = audioList.get(i);
			mArtistsAdapter.add(media.getArtist(), null, media);
			mArtistsAdapter.add(media.getArtist(), media.getAlbum(), media);
		}

		Collections.sort(audioList, byAlbum);
		for (int i = 0; i < audioList.size(); i++) {
			Media media = audioList.get(i);
			mAlbumsAdapter.add(media.getAlbum(), null, media);
		}

		Collections.sort(audioList, byGenre);
		for (int i = 0; i < audioList.size(); i++) {
			Media media = audioList.get(i);
			mGenresAdapter.add(media.getGenre(), null, media);
			mGenresAdapter.add(media.getGenre(), media.getAlbum(), media);
		}

		mSongsAdapter.notifyDataSetChanged();
		mArtistsAdapter.notifyDataSetChanged();
		mAlbumsAdapter.notifyDataSetChanged();
		mGenresAdapter.notifyDataSetChanged();
	}

	@Override
	public void sortBy(int sortby) {
		if (mSortBy == sortby) {
			mSortReverse = !mSortReverse;
		} else {
			mSortBy = sortby;
			mSortReverse = false;
		}
		updateLists();
	}

	/*
	     * 
	     */
	class view_Albums extends KKViewPagerCotroller {
		private Activity mactivity;
		private View mview;

		public view_Albums(Activity activity) {
			super(activity);
			mactivity = activity;
			mview = LayoutInflater.from(mactivity).inflate(
					R.layout.audio_albums, null);
			albumList = (ExpandableListView) mview
					.findViewById(R.id.albums_list);
			albumList.setAdapter(mAlbumsAdapter);
			albumList.setOnGroupClickListener(playlistListener);
			albumList.setOnChildClickListener(playlistChildListener);
		}

		@Override
		public View getView() {
			return mview;
		}

		@Override
		public String getTitle() {
			return "专辑";
		}

		@Override
		public void onshow() {
			Log.i(TAG, "onPageSelected:" + "");
		}

		public void dosth() {
			Log.i(TAG, "");
		}

	}

	class view_Artists extends KKViewPagerCotroller {
		private Activity mactivity;
		private View mview;

		public view_Artists(Activity activity) {
			super(activity);
			mactivity = activity;
			mview = LayoutInflater.from(mactivity).inflate(
					R.layout.audio_artists, null);
			artistList = (ExpandableListView) mview
					.findViewById(R.id.artists_list);
			artistList.setAdapter(mArtistsAdapter);
			artistList.setOnGroupClickListener(playlistListener);
			artistList.setOnChildClickListener(playlistChildListener);
		}

		@Override
		public View getView() {
			return mview;
		}

		@Override
		public String getTitle() {
			return "作者";
		}

		@Override
		public void onshow() {
			Log.i(TAG, "onPageSelected:" + "");
		}

		public void dosth() {
			Log.i(TAG, "");
		}

	}

	class view_Songs extends KKViewPagerCotroller {
		private Activity mactivity;
		private View mview;

		public view_Songs(Activity activity) {
			super(activity);
			mactivity = activity;
			mview = LayoutInflater.from(mactivity).inflate(
					R.layout.audio_songs, null);
			songsList = (ListView) mview.findViewById(R.id.songs_list);
			songsList.setAdapter(mSongsAdapter);
			songsList.setOnItemClickListener(songListener);
		}

		@Override
		public View getView() {
			return mview;
		}

		@Override
		public String getTitle() {
			return "歌曲";
		}

		@Override
		public void onshow() {
			Log.i(TAG, "");
		}

		public void dosth() {
			Log.i(TAG, "");
		}

	}

	class view_Genres extends KKViewPagerCotroller {
		private Activity mactivity;
		private View mview;

		public view_Genres(Activity activity) {
			super(activity);
			mactivity = activity;
			mview = LayoutInflater.from(mactivity).inflate(
					R.layout.audio_genres, null);
			genreList = (ExpandableListView) mview
					.findViewById(R.id.genres_list);
			genreList.setAdapter(mGenresAdapter);
			genreList.setOnGroupClickListener(playlistListener);
			genreList.setOnChildClickListener(playlistChildListener);

		}

		@Override
		public View getView() {
			return mview;
		}

		@Override
		public String getTitle() {
			return "流派";
		}

		@Override
		public void onshow() {
			//
		}

		public void dosth() {
			Log.i(TAG, "");
		}
	}

}
