package com.sample.adapter.adapters;


import android.content.Context;
import android.view.View;

import com.sample.adapter.R;

import java.util.Arrays;
import java.util.List;

import knight.rider.kitt.adapter.SimpleListAdapter;
import knight.rider.kitt.adapter.holder.RecyclerViewHolder;

public class MyAdapter2 extends SimpleListAdapter<String> {

    private static List<String> list;

    private int index = 0;
    private int index2 = 0;

    View headers;

    static {

        list = Arrays.asList(
                "我是第1条数据",
                "我是第2条数据",
                "我是第3条数据",
                "我是第4条数据",
                "我是第5条数据",
                "我是第6条数据",
                "我是第7条数据",
                "我是第8条数据",
                "我是第9条数据",
                "我是第n条数据"
        );
    }


    public MyAdapter2(Context context) {
        super(context, R.layout.item_vertical, false, list);

        headers = getInflater().inflate(R.layout.item_header_footer, null);
    }

    @Override
    public void onBindViewHolders(RecyclerViewHolder holder, int viewType, String s, int position) {
        holder.getTextView(R.id.tv).setText(s);
    }

}
