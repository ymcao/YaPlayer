
package com.yamin.kk.adapter;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yamin.kk.R;
import com.yamin.kk.utils.AudioUtil;
import com.yamin.kk.vlc.Media;
import com.yamin.kk.vlc.VLCApplication;
import com.yamin.kk.vlc.WeakHandler;

public class HistoryAdapter extends BaseAdapter {
    public final static String TAG = "VLC/HistoryAdapter";

    private LayoutInflater mInflater;
    private LibVLC mLibVLC;

    public HistoryAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        try {
            mLibVLC = LibVLC.getInstance();
        } catch (LibVlcException e) {
            Log.d(TAG, "LibVlcException encountered in HistoryAdapter", e);
            return;
        }

        EventHandler em = mLibVLC.getPrimaryMediaList().getEventHandler();
        em.addHandler(new HistoryEventHandler(this));
    }

    @Override
    public int getCount() {
        return mLibVLC.getPrimaryMediaList().size();
    }

    @Override
    public Object getItem(int arg0) {
        return mLibVLC.getPrimaryMediaList().getMRL(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DirectoryAdapter.DirectoryViewHolder holder;
        View v = convertView;

        /* If view not created */
        if (v == null) {
            v = mInflater.inflate(R.layout.list_history_item, parent, false);
            holder = new DirectoryAdapter.DirectoryViewHolder();
            holder.layout = v.findViewById(R.id.layout_item);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.text = (TextView) v.findViewById(R.id.artist);
            holder.icon = (ImageView) v.findViewById(R.id.cover);
            v.setTag(holder);
        } else
            holder = (DirectoryAdapter.DirectoryViewHolder) v.getTag();

        String holderText = "";
        Media m = mLibVLC.getPrimaryMediaList().getMedia(position);
        Log.d(TAG, "Loading media position " + position + " - " + m.getTitle());
        holder.title.setText(m.getTitle());
        holderText = m.getSubtitle();

        holder.text.setText(holderText);
        Bitmap b = AudioUtil.getCover(VLCApplication.getAppContext(), m, 64);
        if(b != null)
            holder.icon.setImageBitmap(b);
        else
            holder.icon.setImageResource(R.drawable.icon);

        return v;
    }

    /**
     * The media list changed.
     *
     * @param added Set to true if the media list was added to
     * @param uri The URI added/removed
     * @param index The index added/removed at
     */
    public void updateEvent(Boolean added, String uri, int index) {
        if(added) {
            Log.v(TAG, "Added index " + index + ": " + uri);
        } else {
            Log.v(TAG, "Removed index " + index + ": " + uri);
        }
        notifyDataSetChanged();
    }

    public void refresh() {
        this.notifyDataSetChanged();
    }

    /**
     *  Handle changes to the media list
     */
    private static class HistoryEventHandler extends WeakHandler<HistoryAdapter> {
        public HistoryEventHandler(HistoryAdapter owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            HistoryAdapter adapater = getOwner();
            if(adapater == null) return;

            String item_uri = msg.getData().getString("item_uri");
            int item_index = msg.getData().getInt("item_index");
            switch (msg.getData().getInt("event")) {
                case EventHandler.CustomMediaListItemAdded:
                    adapater.updateEvent(true, item_uri, item_index);
                    break;
                case EventHandler.CustomMediaListItemDeleted:
                    adapater.updateEvent(false, item_uri, item_index);
                    break;
            }
        }
    };
}
