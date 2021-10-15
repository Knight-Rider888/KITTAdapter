package com.sample.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

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

public class ListActivity extends AppCompatActivity {

    private MyAdapter adapter;

    private int index = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (index == 0) {
                Toast.makeText(ListActivity.this, "首次为空数据展示，再次点击加载数据", Toast.LENGTH_SHORT).show();
                adapter.setLoadState(LoadState.LOAD_NO_DATA);
                index++;
                return;
            }

            if (index == 16) {
                adapter.setLoadState(LoadState.LOAD_ERROR);
                index++;
                return;
            }


            if (adapter.getAttachDataSize() > 50) {
                // 显示加载到底
                adapter.setLoadState(LoadState.LOAD_END);
                return;
            }

            List<String> list = new ArrayList<>();

            for (int i = 0; i < 15; i++) {
                index++;
                list.add("我是第" + index + "条数据");
            }

            adapter.addData(list);

            // 显示加载完成
            adapter.setLoadState(LoadState.LOAD_COMPLETE);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        RecyclerView rv = findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new ItemDecorationImpl(DividerStyle.VERTICAL));

        adapter = new MyAdapter(this, RecyclerView.VERTICAL);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                addData();
            }
        });
        rv.setAdapter(adapter);

        adapter.setOnFooterErrorListener(new OnFooterErrorListener() {
            @Override
            public void onErrorClick() {
                addData();
            }
        });

        // 模拟页面刚进入 显示加载
        adapter.setLoadState(LoadState.LOADING);
    }

    public void add(View view) {
        addData();
    }

    private void addData() {

        // 显示加载中
        adapter.setLoadState(LoadState.LOADING);

        handler.sendEmptyMessageDelayed(1, 2000);
    }
}