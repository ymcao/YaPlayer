/*****************************************************************************
 * SearchResultAdapter.java
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

package com.yamin.kk.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yamin.kk.R;

public class SearchHistoryAdapter extends ArrayAdapter<String> {
	  private Context mcontext;
    public SearchHistoryAdapter(Context context) {
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

        String item = getItem(position);
        holder.text.setText(item);
        holder.text.setTextColor(mcontext.getResources().getColor(R.color.black));
        return view;
    }

    static class ViewHolder {
        TextView text;
    }
}
