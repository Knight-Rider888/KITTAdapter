package com.sample.adapter;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.adapter.adapters.MyAdapter2;

import knight.rider.kitt.adapter.attr.DividerStyle;
import knight.rider.kitt.adapter.impl.ItemDecorationImpl;

public class ListActivity3 extends AppCompatActivity {

    private MyAdapter2 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list3);

        RecyclerView rv = findViewById(R.id.rv2);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(gridLayoutManager);

        rv.addItemDecoration(new ItemDecorationImpl(DividerStyle.GRID, Color.parseColor("#924569"), 20, 50, 100));

        adapter = new MyAdapter2(this);
        rv.setAdapter(adapter);
    }
}