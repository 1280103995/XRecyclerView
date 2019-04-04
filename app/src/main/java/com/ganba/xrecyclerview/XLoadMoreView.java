package com.ganba.xrecyclerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class XLoadMoreView extends LinearLayout implements ILoadMoreView{

    private ProgressBar mProgressBar;
    private TextView tvStatus;
    private String mLoadingText, mNoMoreText;

    public XLoadMoreView(Context context) {
        this(context, null);
    }

    public XLoadMoreView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XLoadMoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.xr_load_more,null);
        mProgressBar = view.findViewById(R.id.progress_bar);
        tvStatus = view.findViewById(R.id.tv_load_more_status);
        addView(view,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mLoadingText = context.getResources().getString(R.string.loading_text);
        mNoMoreText = context.getResources().getString(R.string.no_more_text);
    }

    @Override
    public View getView() {
        return this;
    }

    public void setIndeterminateDrawable(Drawable d){
        mProgressBar.setIndeterminateDrawable(d);
    }

    public void setStateText(String loadingText, String nomoreText){
        mLoadingText = loadingText;
        mNoMoreText = nomoreText;
    }

    @Override
    public void onLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(mLoadingText);
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete() {
        setVisibility(View.GONE);
    }

    @Override
    public void onNoMore() {
        tvStatus.setText(mNoMoreText);
        mProgressBar.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);
    }
}
