package com.ganba.xrecyclerview;

import android.view.View;

public interface ILoadMoreView {

    View getView();

    void onLoading();

    void onComplete();

    void onNoMore();
}
