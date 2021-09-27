package com.sample.adapter.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.sample.adapter.R;

import knight.rider.kitt.adapter.BaseListAdapter;
import knight.rider.kitt.adapter.holder.RecyclerViewHolder;

public class MyAdapter extends BaseListAdapter<String> {

    private int orientation;

    public MyAdapter(Context context, int orientation) {
        super(context);
        this.orientation = orientation;
    }

    @Override
    public int getItemViewsType(int position) {
        return orientation == RecyclerView.VERTICAL ? 1 : 0;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolders(ViewGroup parent, int viewType) {

        int resLayoutId;

        if (viewType == 1)
            resLayoutId = R.layout.item_vertical;
        else
            resLayoutId = R.layout.item_horizontal;

        return new RecyclerViewHolder(getInflater().inflate(resLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolders(RecyclerViewHolder holder, int viewType, String s, int position) {
        if (viewType == 1)
            holder.getTextView(R.id.tv).setText(s);
        else
            holder.getTextView(R.id.tv2).setText(s);
    }

}
