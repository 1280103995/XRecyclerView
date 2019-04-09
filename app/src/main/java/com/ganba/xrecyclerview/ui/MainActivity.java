package com.ganba.xrecyclerview.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ganba.xrecyclerview.R;
import com.ganba.xrecyclerview.ui.LayoutManagerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void normalUse(View view){
        startActivity(new Intent(this, LayoutManagerActivity.class));
    }

    public void multiType(View view){
        startActivity(new Intent(this, MultiTypeActivity.class));
    }
}
