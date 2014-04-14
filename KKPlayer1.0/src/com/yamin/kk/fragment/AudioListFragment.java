/*****************************************************************************
 * AudioListActivity.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.yamin.kk.fragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.yamin.kk.R;
import com.yamin.kk.adapter.AudioListAdapter;
import com.yamin.kk.service.AudioServiceController;
import com.yamin.kk.vlc.Media;
import com.yamin.kk.vlc.MediaLibrary;
import com.yamin.kk.vlc.WeakHandler;

public class AudioListFragment extends SherlockListFragment {

    public final static String TAG = "KKPlayer/AudioListFragment";

    private AudioServiceController mAudioController;
    private MediaLibrary mMediaLibrary;

    private TextView mTitle;
    private AudioListAdapter mSongsAdapter;

    public final static int SORT_BY_TITLE = 0;
    public final static int SORT_BY_LENGTH = 1;
    private boolean mSortReverse = false;
    private int mSortBy = SORT_BY_TITLE;
    public final static String EXTRA_NAME = "name";
    public final static String EXTRA_NAME2 = "name2";
    public final static String EXTRA_MODE = "mode";
    /*
	private LinearLayout audio_list_layout;
    private ResideMenu resideMenu;
    MainActivity parentActivity;
   */
    /* All subclasses of Fragment must include a public empty constructor. */
    public AudioListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAudioController = AudioServiceController.getInstance();

        mMediaLibrary = MediaLibrary.getInstance(getActivity());

        mSongsAdapter = new AudioListAdapter(getActivity());
        setListAdapter(mSongsAdapter);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //registerForContextMenu(getListView());
        updateList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list, container, false);
        mTitle = (TextView) v.findViewById(R.id.title);
        //Add ignore swipe area
        /*
        audio_list_layout= (LinearLayout) v
				.findViewById(R.id.audio_framelayout);
        
		parentActivity = (MainActivity) getActivity();
	    resideMenu = parentActivity.getResideMenu();
	    if(resideMenu!=null)
	        resideMenu.addIgnoredView(audio_list_layout);
        */
        return v;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSongsAdapter.clear();
    }

    public static void set(Intent intent, String name, String name2, int mode) {
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_NAME2, name2);
        intent.putExtra(EXTRA_MODE, mode);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mAudioController.load(mSongsAdapter.getLocations(), position);
        AudioPlayerFragment.start(getActivity());
        super.onListItemClick(l, v, position, id);
    }

    /**
     * Handle changes on the list
     */
    private Handler mHandler = new AudioListHandler(this);

    private static class AudioListHandler extends WeakHandler<AudioListFragment> {
        public AudioListHandler(AudioListFragment owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            AudioListFragment fragment = getOwner();
            if(fragment == null) return;

            switch (msg.what) {
                case MediaLibrary.MEDIA_ITEMS_UPDATED:
                    fragment.updateList();
                    break;
            }
        }
    };

    private Comparator<Media> byMRL = new Comparator<Media>() {
        public int compare(Media m1, Media m2) {
            if( m1 == null)
                return -1;
            else if( m2 == null)
                return 1;
            return String.CASE_INSENSITIVE_ORDER.compare(m1.getLocation(), m2.getLocation());
        };
    };

    private Comparator<Media> byLength = new Comparator<Media>() {
        public int compare(Media m1, Media m2) {
            if (m1.getLength() > m2.getLength())
                return -1;
            if (m1.getLength() < m2.getLength())
                return 1;
            else
                return 0;
        };
    };

    private void updateList() {
        final Bundle b = getArguments();
        String name = b.getString(EXTRA_NAME);
        String name2 = b.getString(EXTRA_NAME2);
        int mode = b.getInt(EXTRA_MODE, 0);

        List<Media> audioList;
        List<String> itemList;
        String currentItem = null;
        int currentIndex = -1;

        if (name == null || mode == AudioFragment.MODE_SONG) {
            mTitle.setText(R.string.songs);
            itemList = mAudioController.getItems();
            currentItem = mAudioController.getItem();
            audioList = MediaLibrary.getInstance(getActivity()).getMediaItems(itemList);
        }
        else {
            mTitle.setText(name2 != null ? name2 : name);
            audioList = MediaLibrary.getInstance(getActivity()).getAudioItems(name, name2, mode);
        }

        mSongsAdapter.clear();
        switch (mSortBy) {
            case SORT_BY_LENGTH:
                Collections.sort(audioList, byLength);
                break;
            case SORT_BY_TITLE:
            default:
                Collections.sort(audioList, byMRL);
                break;
        }
        if (mSortReverse) {
            Collections.reverse(audioList);
        }

        for (int i = 0; i < audioList.size(); i++) {
            Media media = audioList.get(i);
            if (currentItem != null && currentItem.equals(media.getLocation()))
                currentIndex = i;
            mSongsAdapter.add(media);
        }
        mSongsAdapter.setCurrentIndex(currentIndex);
        getListView().setSelection(currentIndex);

        mSongsAdapter.notifyDataSetChanged();
    }
}
