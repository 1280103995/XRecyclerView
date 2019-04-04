package com.ganba.xrecyclerview;

import android.view.View;

public interface IRefreshView {

    View getView();

    void autoRefresh(OnAutoRefreshListener listener);

    void onMove(float delta);

    boolean consumEvent();

    boolean releaseAction();

    void refreshComplete();
}
