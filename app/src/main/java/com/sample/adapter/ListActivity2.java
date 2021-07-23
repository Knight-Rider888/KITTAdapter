package com.sample.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.adapter.adapters.MyAdapter;

import java.util.ArrayList;
import java.util.List;

import knight.rider.kitt.adapter.attr.DividerStyle;
import knight.rider.kitt.adapter.attr.LoadState;
import knight.rider.kitt.adapter.impl.ItemDecorationImpl;
import knight.rider.kitt.adapter.listener.OnFooterErrorListener;
import knight.rider.kitt.adapter.listener.OnLoadMoreListener;

public class ListActivity2 extends AppCompatActivity {

    private MyAdapter adapter;

    private int index = 0;

    private boolean isCanLoadMore = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            List<String> list = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                index++;
                list.add("我是第" + index + "条数据");
            }

            adapter.addData(list);

            adapter.setLoadState(LoadState.LOAD_ERROR);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list2);

        RecyclerView rv = findViewById(R.id.rv2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new ItemDecorationImpl(DividerStyle.HORIZONTAL));

//        final ReboundListLayout rebound = findViewById(R.id.rebound);
//        rebound.setEnablePullUp(false);

        adapter = new MyAdapter(this, RecyclerView.HORIZONTAL);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                if (!isCanLoadMore) {
                    isCanLoadMore = true;
//                    rebound.setEnablePullUp(true);
                } else {
                    isCanLoadMore = false;
                    startActivity(new Intent(ListActivity2.this, MoreActivity.class));
                }
            }
        });
        rv.setAdapter(adapter);

        // 出错支持加载更多的监听
        adapter.setLoadMoreEnableByLoadError(true);

        adapter.setOnFooterErrorListener(new OnFooterErrorListener() {
            @Override
            public void onErrorClick() {
                startActivity(new Intent(ListActivity2.this, MoreActivity.class));
            }
        });

        // 模拟页面刚进入 显示加载
        adapter.setLoadState(LoadState.LOADING);


        addData();
    }

    private void addData() {

        handler.sendEmptyMessageDelayed(1, 5000);
    }
}