package com.sample.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void verticalState(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void horizontalState(View view) {
        Intent intent = new Intent(this, ListActivity2.class);
        startActivity(intent);
    }

    public void headerAndFooter(View view) {
        Intent intent = new Intent(this, ListHeaderAndFooterActivity.class);
        startActivity(intent);
    }
}