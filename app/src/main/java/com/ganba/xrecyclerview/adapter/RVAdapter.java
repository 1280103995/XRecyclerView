package com.ganba.xrecyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class RVAdapter<T> extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private List<T> mData = new ArrayList<>();
    private int mItemLayoutId;
    private OnItemClickListener<T> mOnItemClickListener;
    private OnItemLongClickListener<T> mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener<T> mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T item, int position);
    }

    public interface OnItemLongClickListener<T> {
        void onItemLongClick(View view, T item, int position);
    }

    protected abstract void convert(ViewHolder vH, T item, int position);

    protected RVAdapter(int itemLayoutId) {
        mItemLayoutId = itemLayoutId;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getViewHolder(parent, mItemLayoutId);
    }

    protected ViewHolder getViewHolder(@NonNull ViewGroup parent, int layoutId) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        convert(holder, getData(position), position);
        initItemClickListener(holder, position);
    }

    private void initItemClickListener(final RecyclerView.ViewHolder holder, final int position) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView, getData(position), position);
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemLongClickListener.onItemLongClick(holder.itemView, getData(position), position);
                    return true;
                }
            });
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * 用来保存条目视图里面所有的控件
         */
        private SparseArray<View> mViews;

        /**
         * 构造函数
         *
         * @param itemView
         */
        public ViewHolder(View itemView) {
            super(itemView);
            mViews = new SparseArray<>();
        }

        /**
         * 根据控件id获取控件对象
         *
         * @param viewId
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T> T getView(int viewId) {

            // 从集合中根据这个id获取view视图对象
            View view = mViews.get(viewId);

            // 如果为空，说明是第一次获取，里面没有，那就在布局文件中找到这个控件，并且存进集合中
            if (view == null) {
                view = itemView.findViewById(viewId);
                mViews.put(viewId, view);
            }

            // 返回控件对象
            return (T) view;
        }

        /**
         * 为TextView设置文本,按钮也可以用这个方法,button是textView的子类
         *
         * @param textViewId
         * @param content
         */
        public void setText(int textViewId, String content) {
            ((TextView) getView(textViewId)).setText(content);
        }

        /**
         * 为ImageView设置图片
         *
         * @param iv
         * @param imageId
         */
        public void setImage(ImageView iv, int imageId) {
            iv.setImageResource(imageId);
        }

        /**
         * 为ImageView设置图片
         *
         * @param imgId
         * @param imageId
         */
        public void setImage(int imgId, int imageId) {
            ((ImageView) getView(imgId)).setImageResource(imageId);
        }

        /**
         * 添加点击事件
         *
         * @param viewId
         * @param listener
         * @return
         */
        public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
            View view = getView(viewId);
            view.setOnClickListener(listener);
            return this;
        }
    }

    public List<T> getData() {
        return mData;
    }

    public int getDataSize() {
        return mData.size();
    }

    public T getData(int index) {
        return mData.size() > index ? mData.get(index) : null;
    }

    public void add(T d) {
        int startPos = mData.size();
        mData.add(d);
        notifyItemInserted(startPos);
    }

    public void addAll(List<T> data) {
        int curSize = mData.size();
        mData.addAll(data);
        if (curSize == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(curSize, data.size());
        }
    }

    public void remove(T d) {
        if (mData.contains(d)) {
            int posIndex = mData.indexOf(d);
            mData.remove(d);
            notifyItemRemoved(posIndex);
        }
    }

    public void remove(int index) {
        if (mData.size() > index) {
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    public boolean contains(T d) {
        return mData.contains(d);
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public void replaceAll(List<T> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }
}
