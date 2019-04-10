package com.ganba.xrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

public class XScrollView extends NestedScrollView {

    private boolean mRefreshEnabled;
    private boolean isLoadingData;
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    private IRefreshView mRefreshView;
    private LoadingListener mLoadingListener;
    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    public XScrollView(Context context) {
        this(context, null);
    }

    public XScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.XScrollView);
        mRefreshEnabled = ta.getBoolean(R.styleable.XScrollView_xRefreshEnabled, false);
        if (mRefreshEnabled){
            mRefreshView = initRefreshView();
        }
        ta.recycle();
    }

    private XRefreshView initRefreshView() {
        return new XRefreshView(getContext());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!mRefreshEnabled) return;
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(mRefreshView.getView());

        View view = getChildAt(0);
        removeAllViews();
        layout.addView(view);
        addView(layout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isLoadingData) return super.onTouchEvent(ev);

        if (mLastY == -1) mLastY = ev.getRawY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getScrollY() <= 0 && mRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshView == null) break;
                    mRefreshView.onMove(deltaY / DRAG_RATE);
                    if (mRefreshView.consumEvent()) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (getScrollY() <= 0 && mRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshView != null && mRefreshView.releaseAction()) {
                        if (mLoadingListener != null) {
                            isLoadingData = true;
                            mLoadingListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //解决和CollapsingToolbarLayout冲突的问题
        AppBarLayout appBarLayout = null;
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if (p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if (child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout) child;
                    break;
                }
            }
            if (appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public interface LoadingListener {
        void onRefresh();
    }

    public void setRefreshView(IRefreshView view) {
        mRefreshView = view;
    }

    public void setRefreshEnabled(boolean enabled) {
        mRefreshEnabled = enabled;
        if (mRefreshEnabled && mRefreshView == null) {
            mRefreshView = initRefreshView();
        }
    }

    public void setColorSchemeColors(int... colors) {
        if (mRefreshView != null && mRefreshView.getView() instanceof XRefreshView) {
            ((XRefreshView) mRefreshView).setColorSchemeColors(colors);
        }
    }

    public void setBackgroundColorRes(int colorRes) {
        if (mRefreshView != null && mRefreshView.getView() instanceof XRefreshView) {
            ((XRefreshView) mRefreshView).setBackgroundColorRes(colorRes);
        }
    }

    public void autoRefresh() {
        if (mRefreshEnabled && mLoadingListener != null && mRefreshView != null) {
            mRefreshView.autoRefresh(new OnAutoRefreshListener() {
                @Override
                public void onAutoRefresh() {
                    isLoadingData = true;
                    mLoadingListener.onRefresh();
                }
            });
        }
    }

    public void refreshComplete() {
        if (mRefreshView != null) {
            mRefreshView.refreshComplete();
        }
        isLoadingData = false;
    }
}
