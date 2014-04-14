package com.yamin.kk.adapter;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yamin.kk.R;
import com.yamin.kk.vlc.Media;

public class SearchResultAdapter extends ArrayAdapter<Media>
        implements Comparator<Media> {
    private Context mcontext;
    public SearchResultAdapter(Context context) {
        super(context, 0);
        mcontext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_1, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.list_item1_textview);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        Media item = getItem(position);
        holder.text.setText(item.getTitle());

        return view;
    }

    @Override
    public int compare(Media object1, Media object2) {
        return object1.getTitle().compareToIgnoreCase(object2.getTitle());
    }

    public void sort() {
        super.sort(this);
    }

    static class ViewHolder {
        TextView text;
    }
}
