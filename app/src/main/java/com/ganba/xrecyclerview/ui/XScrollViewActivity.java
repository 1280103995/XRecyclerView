package com.ganba.xrecyclerview.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ganba.xrecyclerview.R;
import com.ganba.xrecyclerview.XScrollView;

public class XScrollViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xscrollview);
        final XScrollView scrollView = findViewById(R.id.scrollView);

        scrollView.setLoadingListener(new XScrollView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.refreshComplete();
                    }
                }, 2000);
            }
        });

        scrollView.autoRefresh();
    }
}
