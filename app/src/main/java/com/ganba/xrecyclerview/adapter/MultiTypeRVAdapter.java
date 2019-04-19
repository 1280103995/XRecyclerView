package com.ganba.xrecyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public abstract class MultiTypeRVAdapter<T> extends RVAdapter<T>{

    private OnMultiItemClickListener<T> mOnMultiItemClickListener;
    private OnMultiItemLongClickListener<T> mOnMultiItemLongClickListener;

    public interface OnMultiItemClickListener<T>{
        void onItemClick(View view, T item, int position);
    }

    public interface OnMultiItemLongClickListener<T>{
        void onItemLongClick(View view, T item, int position);
    }

    public void setOnMultiItemClickListener(OnMultiItemClickListener<T> l){
        mOnMultiItemClickListener = l;
    }
    public void setOnMultiItemLongClickListener(OnMultiItemLongClickListener<T> l){
        mOnMultiItemLongClickListener = l;
    }

    public MultiTypeRVAdapter() {
        super(-1);
    }

    protected abstract int getItemLayoutId(int viewType);

    protected abstract int getItemType(int position, T item);

    @Override
    public int getItemViewType(int position) {
        if (getDataSize() > 0) return getItemType(position, getData(position));
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getViewHolder(parent, getItemLayoutId(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        initItemClickListener(holder, position);
    }

    private void initItemClickListener(final RecyclerView.ViewHolder holder, final int position) {
        if (mOnMultiItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnMultiItemClickListener.onItemClick(holder.itemView, getData(position), position);
                }
            });
        }
        if (mOnMultiItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnMultiItemLongClickListener.onItemLongClick(holder.itemView, getData(position), position);
                    return true;
                }
            });
        }
    }

}
