package com.ganba.xrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class XRecyclerView extends RecyclerView {

    private boolean isLoadingData = false;
    private boolean isNoMore = false;
    private WrapAdapter mWrapAdapter;
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    private LoadingListener mLoadingListener;
    private boolean mRefreshEnabled = true;
    private boolean mLoadMoreEnabled = true;
    private boolean isFirstLoad;
    private boolean mHeadFootWithEmptyEnabled = false;

    private IRefreshView mRefreshView;
    private View mHeaderView;
    private View mEmptyView;
    private View mFooterView;
    private ILoadMoreView mLoadMoreView;
    private final AdapterDataObserver mDataObserver = new DataObserver();
    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    // limit number to call load more
    private int limitNumberToCallLoadMore = 1;
    public static final int LAYOUT_MANAGER_TYPE_LINEAR = 0;
    public static final int LAYOUT_MANAGER_TYPE_GRID = 1;
    public static final int LAYOUT_MANAGER_TYPE_STAGGERED_GRID = 2;

    private static final int DEF_LAYOUT_MANAGER_TYPE = LAYOUT_MANAGER_TYPE_LINEAR;
    private static final int DEF_GRID_SPAN_COUNT = 2;
    private static final int DEF_LAYOUT_MANAGER_ORIENTATION = OrientationHelper.VERTICAL;

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.XRecyclerView);
        mRefreshEnabled = ta.getBoolean(R.styleable.XRecyclerView_xRefreshEnabled, true);
        mLoadMoreEnabled = ta.getBoolean(R.styleable.XRecyclerView_xLoadMoreEnabled, true);
        mHeadFootWithEmptyEnabled = ta.getBoolean(R.styleable.XRecyclerView_xHeadFootWithEmptyEnabled, false);
        if (ta.hasValue(R.styleable.XRecyclerView_xLayoutManager)) {
            int layoutManagerType = ta.getInt(R.styleable.XRecyclerView_xLayoutManager, DEF_LAYOUT_MANAGER_TYPE);
            int layoutManagerOrientation = ta.getInt(R.styleable.XRecyclerView_xLayoutManagerOrientation, DEF_LAYOUT_MANAGER_ORIENTATION);
            boolean isReverseLayout = ta.getBoolean(R.styleable.XRecyclerView_xIsReverseLayout, false);
            int gridSpanCount = ta.getInt(R.styleable.XRecyclerView_xSpanCount, DEF_GRID_SPAN_COUNT);

            switch (layoutManagerType) {
                case LAYOUT_MANAGER_TYPE_LINEAR:
                    setLayoutManager(new LinearLayoutManager(context, layoutManagerOrientation, isReverseLayout));
                    break;
                case LAYOUT_MANAGER_TYPE_GRID:
                    setLayoutManager(new GridLayoutManager(context, gridSpanCount, layoutManagerOrientation, isReverseLayout));
                    break;
                case LAYOUT_MANAGER_TYPE_STAGGERED_GRID:
                    setLayoutManager(new StaggeredGridLayoutManager(gridSpanCount, layoutManagerOrientation));
                    break;
            }
        }
        ta.recycle();

        if (mRefreshEnabled) {
            mRefreshView = initRefreshView();
        }
        if (mLoadMoreEnabled) {
            mLoadMoreView = initLoadMoreView();
        }
    }

    private XRefreshView initRefreshView() {
        return new XRefreshView(getContext());
    }

    private XLoadMoreView initLoadMoreView() {
        XLoadMoreView xLoadMore = new XLoadMoreView(getContext());
        xLoadMore.setVisibility(GONE);
        return xLoadMore;
    }

    public void setRefreshView(IRefreshView view) {
        mRefreshView = view;
    }

    public void addHeaderView(View view) {
        mHeaderView = view;
    }

    public void setEmptyView(View view) {
        mEmptyView = view;
    }

    public void addFooterView(View view) {
        mFooterView = view;
    }

    public void setLoadMoreView(ILoadMoreView view) {
        mLoadMoreView = view;
    }

    public void reset() {
        refreshComplete();
        loadMoreComplete();
    }

    public void refreshComplete() {
        if (mRefreshView != null) {
            mRefreshView.refreshComplete();
        }
        isLoadingData = false;
        isNoMore = false;
    }

    public void loadMoreComplete() {
        stopScroll();
        isLoadingData = false;
        if (mLoadMoreView != null) mLoadMoreView.onComplete();
    }

    public void setNoMore(boolean noMore) {
        isLoadingData = false;
        isNoMore = noMore;
        if (mLoadMoreView != null) mLoadMoreView.onNoMore();
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

    public void setLoadMoreIndeterminateDrawable(Drawable d) {
        if (mLoadMoreView != null && mLoadMoreView.getView() instanceof XLoadMoreView) {
            ((XLoadMoreView) mLoadMoreView).setIndeterminateDrawable(d);
        }
    }

    public void setLoadMoreText(String loadingText, String nomoreText) {
        if (mLoadMoreView != null && mLoadMoreView.getView() instanceof XLoadMoreView) {
            ((XLoadMoreView) mLoadMoreView).setStateText(loadingText, nomoreText);
        }
    }

    public void setRefreshEnabled(boolean enabled) {
        mRefreshEnabled = enabled;
        if (mRefreshEnabled && mRefreshView == null) {
            mRefreshView = initRefreshView();
        }
    }

    public boolean isRefreshEnabled() {
        return mRefreshEnabled;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        mLoadMoreEnabled = enabled;
        if (mLoadMoreEnabled && mLoadMoreView == null) {
            mLoadMoreView = initLoadMoreView();
        }
    }

    public boolean isLoadMoreEnabled() {
        return mLoadMoreEnabled;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter == null) return;
        mWrapAdapter = new WrapAdapter(adapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public Adapter getAdapter() {
        if (mWrapAdapter != null) return mWrapAdapter.adapter;
        else return null;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        if (null == layout) return;

        if (layout instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layout);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return mWrapAdapter.isSpecialItem(position) ? gridManager.getSpanCount() : 1;
                }
            });
        }
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
                if (isOnTop() && mRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (mRefreshView == null) break;
                    mRefreshView.onMove(deltaY / DRAG_RATE);
                    if (mRefreshView.consumEvent()) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && mRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
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

    private boolean isOnTop() {
        return mRefreshView != null && mRefreshView.getView().getParent() != null;
    }

    private int getHeaders_includingRefreshCount() {
        return mWrapAdapter.getRefreshHeaderCount() + mWrapAdapter.getHeaderCount();
    }

    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            mWrapAdapter.notifyDataSetChanged();
            isFirstLoad = true;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            int count = getHeaders_includingRefreshCount();
            mWrapAdapter.notifyItemRangeInserted(count + positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            int count = getHeaders_includingRefreshCount();
            mWrapAdapter.notifyItemRangeChanged(count + positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            int count = getHeaders_includingRefreshCount();
            mWrapAdapter.notifyItemRangeRemoved(count + positionStart, itemCount);
            if (mWrapAdapter.adapter.getItemCount() == 0)
                mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int count = getHeaders_includingRefreshCount();
            mWrapAdapter.notifyItemMoved(count + fromPosition, count + toPosition);
        }
    }

    private class WrapAdapter extends Adapter<ViewHolder> {

        private Adapter adapter;
        private static final int TYPE_REFRESH = 100000;
        private static final int TYPE_HEADER = 100001;
        private static final int TYPE_EMPTY = 100002;
        private static final int TYPE_FOOTER = 100003;
        private static final int TYPE_LOAD_MORE = 100004;

        private WrapAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        private int getRefreshHeaderCount() {
            return mRefreshEnabled && mRefreshView != null ? 1 : 0;
        }

        private int getHeaderCount() {
            return mHeaderView == null ? 0 : 1;
        }

        private int getEmptyCount() {
            return mEmptyView == null ? 0 : 1;
        }

        private int getFooterCount() {
            return mFooterView == null ? 0 : 1;
        }

        private int getLoadMoreViewCount() {
            return mLoadMoreEnabled && mLoadMoreView != null ? 1 : 0;
        }

        private boolean isRefreshHeader(int position) {
            return getRefreshHeaderCount() == 1 && position == 0;
        }

        private boolean isHeader(int position) {
            if (!mHeadFootWithEmptyEnabled && adapter.getItemCount() == 0) return false;
            return getHeaderCount() == 1 && position < getRefreshHeaderCount() + getHeaderCount();
        }

        private boolean isEmpty(int position) {
            return adapter.getItemCount() == 0
                    && getEmptyCount() == 1
                    && position == getRefreshHeaderCount() + (mHeadFootWithEmptyEnabled ? getHeaderCount() : 0);
        }

        private boolean isFooter(int position) {
            if (!mHeadFootWithEmptyEnabled && adapter.getItemCount() == 0) return false;
            return getFooterCount() == 1
                    && position == (adapter.getItemCount() == 0 ? getEmptyCount() : adapter.getItemCount())
                    + getRefreshHeaderCount()
                    + getHeaderCount();
        }

        private boolean isLoadMore(int position) {
            return getLoadMoreViewCount() == 1 && position == getItemCount() - 1;
        }

        @Override
        public int getItemCount() {
            if (isFirstLoad) {
                int count = adapter.getItemCount()
                        + getRefreshHeaderCount()
                        + getHeaderCount()
                        + getFooterCount()
                        + getLoadMoreViewCount();

                if (adapter.getItemCount() == 0) {
                    count = getRefreshHeaderCount() + getEmptyCount();
                    int temp = getHeaderCount() + getFooterCount();
                    if (mHeadFootWithEmptyEnabled) {
                        count += temp;
                    }
                }

                return count;
            } else {
                return getRefreshHeaderCount();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH;
            }

            if (isHeader(position)) {
                return TYPE_HEADER;
            }

            if (isEmpty(position)) {
                return TYPE_EMPTY;
            }

            if (isFooter(position)) {
                return TYPE_FOOTER;
            }

            if (isLoadMore(position)) {
                return TYPE_LOAD_MORE;
            }
            int adjPosition = position - getRefreshHeaderCount() - getHeaderCount();
            return adapter.getItemViewType(adjPosition);
        }

        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_REFRESH:
                    return new SimpleViewHolder(mRefreshView.getView());
                case TYPE_HEADER:
                    return new SimpleViewHolder(mHeaderView);
                case TYPE_EMPTY:
                    return new SimpleViewHolder(mEmptyView);
                case TYPE_FOOTER:
                    return new SimpleViewHolder(mFooterView);
                case TYPE_LOAD_MORE:
                    return new SimpleViewHolder(mLoadMoreView.getView());
                default:
                    return adapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case TYPE_REFRESH:
                    break;
                case TYPE_HEADER:
                    break;
                case TYPE_EMPTY:
                    break;
                case TYPE_FOOTER:
                    break;
                case TYPE_LOAD_MORE:
                    break;
                default:
                    int adjPosition = position - getRefreshHeaderCount() - getHeaderCount();
                    int adapterCount;
                    if (adapter != null) {
                        adapterCount = adapter.getItemCount();
                        if (adjPosition < adapterCount) {
                            adapter.onBindViewHolder(holder, adjPosition);
                        }
                    }
                    break;
            }
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeaderCount() + getRefreshHeaderCount()) {
                int adjPosition = position - (getHeaderCount() + getRefreshHeaderCount());
                if (adjPosition < adapter.getItemCount()) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        private boolean isSpecialItem(int position) {
            return isRefreshHeader(position)
                    || isHeader(position)
                    || isEmpty(position)
                    || isFooter(position)
                    || isLoadMore(position);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            if (null == adapter) return;

            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);

            if (null == adapter) return;

            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            int position = holder.getLayoutPosition();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams && isSpecialItem(position)) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }

            if (null == adapter || isSpecialItem(position)) return;

            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);

            int position = holder.getAdapterPosition();
            if (null == adapter || isSpecialItem(position)) return;

            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            super.onViewRecycled(holder);

            int position = holder.getAdapterPosition();
            if (null == adapter || isSpecialItem(position)) return;

            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        private class SimpleViewHolder extends ViewHolder {
            private SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public interface LoadingListener {
        void onRefresh();

        void onLoadMore();
    }

    @Override
    protected void onAttachedToWindow() {
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mWrapAdapter.adapter && mWrapAdapter.adapter.hasObservers()) {
            mWrapAdapter.adapter.unregisterAdapterDataObserver(mDataObserver);
        }

        mRefreshView = null;
        mHeaderView = null;
        mEmptyView = null;
        mFooterView = null;
        mLoadMoreView = null;
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);

    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (mLoadingListener != null && !isLoadingData && mLoadMoreEnabled && mLoadMoreView != null) {
            LayoutManager layoutManager = getLayoutManager();
            int firstVisibleItemPosition;
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
                firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = ((StaggeredGridLayoutManager) layoutManager);
                int[] intoFirst = new int[staggeredGridLayoutManager.getSpanCount()];
                int[] intoLast = new int[staggeredGridLayoutManager.getSpanCount()];
                staggeredGridLayoutManager.findFirstVisibleItemPositions(intoFirst);
                staggeredGridLayoutManager.findLastVisibleItemPositions(intoLast);
                firstVisibleItemPosition = findMinMaxValue(intoFirst, false);
                lastVisibleItemPosition = findMinMaxValue(intoLast, true);
            } else {
                LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) layoutManager);
                firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            }

            int adjAdapterItemCount = layoutManager.getItemCount()
                    + mWrapAdapter.getRefreshHeaderCount()
                    + mWrapAdapter.getHeaderCount()
                    + mWrapAdapter.getFooterCount()
                    + mWrapAdapter.getLoadMoreViewCount();

            if (!isNoMore && ((layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= adjAdapterItemCount - limitNumberToCallLoadMore
                    && adjAdapterItemCount >= layoutManager.getChildCount())
                    //数据不足一屏时，自动加载更多
                    || lastVisibleItemPosition + 1 == adjAdapterItemCount && firstVisibleItemPosition == 0)) {

                isLoadingData = true;
                mLoadMoreView.onLoading();
                mLoadingListener.onLoadMore();
            }
        }
    }

    private int findMinMaxValue(int[] lastPositions, boolean isMax) {
        int value = lastPositions[0];
        for (int temp : lastPositions) {
            if (isMax && temp > value) {
                value = temp;
            }
            if (!isMax && temp <= value) {
                value = temp;
            }
        }
        return value;
    }

    /**
     * 显示 EmptyView 的时候是否显示 HeaderView、FooterView
     *
     * @param enable
     */
    public void setHeadFootWithEmptyEnabled(boolean enable) {
        mHeadFootWithEmptyEnabled = enable;
    }

    public boolean isHeadAndEmptyEnabled() {
        return mHeadFootWithEmptyEnabled;
    }

    // set the number to control call load more
    public void setLimitNumberToCallLoadMore(int limitNumberToCallLoadMore) {
        this.limitNumberToCallLoadMore = limitNumberToCallLoadMore;
    }

}