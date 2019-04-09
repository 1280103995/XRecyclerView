package com.ganba.xrecyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class XRefreshView extends LinearLayout implements IRefreshView {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_RELEASE_TO_REFRESH = 1;
    private static final int STATE_REFRESHING = 2;
    private CircleImageView mCircleImageView;
    private int mMeasuredHeight;
    private int mState = STATE_NORMAL;
    private LinearLayout mContainer;
    private int mStartDrawPoint;//箭头起点的位置（圆圈半径+ 圆圈paddingBottom | 圆圈marginBottom）

    public XRefreshView(Context context) {
        this(context, null);
    }

    public XRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.xr_refresh_view, null);
        mCircleImageView = mContainer.findViewById(R.id.circle_view);
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));

        measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
        mStartDrawPoint = mMeasuredHeight / 2;
    }

    @Override
    public View getView() {
        return this;
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                setVisibleHeight(value);
            }
        });
        animator.start();
    }

    private void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    private int getVisibleHeight() {
        return mContainer.getLayoutParams().height;
    }

    public void setColorSchemeColors(@NonNull int... colors) {
        mCircleImageView.setColorSchemeColors(colors);
    }

    public void setBackgroundColorRes(int colorRes) {
        mCircleImageView.setBackgroundColor(colorRes);
    }

    @Override
    public void autoRefresh(final OnAutoRefreshListener listener) {
        mState = STATE_REFRESHING;

        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), mMeasuredHeight);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                setVisibleHeight(value);
                mCircleImageView.pull(value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCircleImageView.refreshing();
                if (listener != null) listener.onAutoRefresh();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    @Override
    public void onMove(float delta) {
        int height = (int) delta + getVisibleHeight();
        if (getVisibleHeight() > height || delta > 0) {
            setVisibleHeight(height);
            //刷新控件显示的高度为mStartDrawPoint时，箭头才开始绘制
            if (height > mStartDrawPoint) {
                float progress = (height - mStartDrawPoint) * CircleImageView.MAX_PROGRESS_ANGLE
                        / (mMeasuredHeight - mStartDrawPoint);
                mCircleImageView.pull(progress);
            }

            if (mState <= STATE_RELEASE_TO_REFRESH) {
                if (getVisibleHeight() > mMeasuredHeight) {
                    mState = STATE_RELEASE_TO_REFRESH;
                } else {
                    mState = STATE_NORMAL;
                }
            }
        }
    }

    @Override
    public boolean consumEvent() {
        return getVisibleHeight() > 0 && mState < XRefreshView.STATE_REFRESHING;
    }

    @Override
    public boolean releaseAction() {
        boolean isOnRefresh = false;

        if (getVisibleHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            mState = STATE_REFRESHING;
            isOnRefresh = true;
            mCircleImageView.refreshing();
        }

        if (mState != STATE_REFRESHING) {
            smoothScrollTo(0);
        }

        if (mState == STATE_REFRESHING) {
            smoothScrollTo(mMeasuredHeight);
        }

        return isOnRefresh;
    }

    @Override
    public void refreshComplete() {
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mState = STATE_NORMAL;
                mCircleImageView.complete();
            }
        }, 300);
    }
}
