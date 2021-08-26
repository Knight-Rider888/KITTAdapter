package com.sample.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.adapter.adapters.HeadAndFootAdapter;

import knight.rider.kitt.adapter.attr.DividerStyle;
import knight.rider.kitt.adapter.impl.ItemDecorationImpl;

public class ListHeaderAndFooterActivity extends AppCompatActivity {

    private HeadAndFootAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_header_and_footer);

        RecyclerView rv = findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);

        rv.addItemDecoration(new ItemDecorationImpl(DividerStyle.VERTICAL, Color.parseColor("#924569"), 20, 50, 100));

        adapter = new HeadAndFootAdapter(this);
        rv.setAdapter(adapter);


    }

    public void addHeader(View view) {
        adapter.addHeader();
    }

    public void addFooter(View view) {
        adapter.addFooter();
    }

    public void removeHeader(View view) {
        adapter.removeAllHeaders();
    }

    public void removeFooter(View view) {
        adapter.removeAllFooters();
    }

    public void removeHeader2(View view) {
        adapter.removeHeader(1);
    }

    public void removeFooter2(View view) {
        adapter.removeFooter(0);
    }
}