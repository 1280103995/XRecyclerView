package com.ganba.xrecyclerview.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ganba.xrecyclerview.R;
import com.ganba.xrecyclerview.XRecyclerView;
import com.ganba.xrecyclerview.adapter.MultiTypeRVAdapter;
import com.ganba.xrecyclerview.entity.MultiType;

import java.util.ArrayList;
import java.util.List;

public class MultiTypeActivity extends AppCompatActivity {

    XRecyclerView mRecyclerView;
    MultiTypeRVAdapter<Object> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_recycler_view);

        mRecyclerView = findViewById(R.id.recycler_view);
        TextView header = (TextView) LayoutInflater.from(this).inflate(R.layout.header_empty_footer, mRecyclerView, false);
        header.setText("这是头部");
        header.setBackgroundColor(Color.RED);
        mRecyclerView.addHeaderView(header);

        TextView empty = (TextView) LayoutInflater.from(this).inflate(R.layout.header_empty_footer, mRecyclerView, false);
        empty.setText("这是空页面");
        empty.setBackgroundColor(Color.GREEN);
        empty.setHeight(600);
        mRecyclerView.setEmptyView(empty);

        TextView footer = (TextView) LayoutInflater.from(this).inflate(R.layout.header_empty_footer, mRecyclerView, false);
        footer.setText("这是尾部");
        footer.setBackgroundColor(Color.BLUE);
        mRecyclerView.addFooterView(footer);

        mRecyclerView.setRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);

        mAdapter = new MultiTypeRVAdapter<Object>(this) {
            private int SUB_TYPE = 0;
            private int TYPE = 1;

            @Override
            protected void convert(ViewHolder vH, Object item, int position) {
                if (vH.getItemViewType() == SUB_TYPE) {
                    vH.setText(R.id.tv_type1, (String) item);
                } else {
                    MultiType type = (MultiType) item;
                    vH.setText(R.id.tv_type2, type.getName());
                }
            }

            @Override
            protected int getItemLayoutId(int viewType) {
                if (viewType == SUB_TYPE) {
                    return R.layout.item_type_1;
                } else {
                    return R.layout.item_type_2;
                }
            }

            @Override
            protected int getItemType(int position, Object item) {
                if (item instanceof String) {
                    return SUB_TYPE;
                } else {
                    return TYPE;
                }
            }
        };

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.addAll(getDataType());

        mAdapter.setOnMultiItemClickListener(new MultiTypeRVAdapter.OnMultiItemClickListener<Object>() {
            @Override
            public void onItemClick(View view, Object item, int position) {
                String name;
                if (item instanceof String){
                    name = (String) item;
                }else {
                    name = ((MultiType)item).getName();
                }
                Toast.makeText(MultiTypeActivity.this, name + ",  Multi Type Click " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mAdapter.setOnMultiItemLongClickListener(new MultiTypeRVAdapter.OnMultiItemLongClickListener<Object>() {
            @Override
            public void onItemLongClick(View view, Object item, int position) {
                String name;
                if (item instanceof String){
                    name = (String) item;
                }else {
                    name = ((MultiType)item).getName();
                }
                Toast.makeText(MultiTypeActivity.this, name + ",  Multi Type Long Click " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private List<Object> getDataType() {
        List<Object> list = new ArrayList<>();
        //1
        list.add("subTitle 1");
        for (int i = 0; i < 3; i++) {
            list.add(new MultiType("item "+i));
        }
        //2
        list.add("subTitle 2");
        for (int i = 0; i < 5; i++) {
            list.add(new MultiType("item "+i));
        }
        //3
        list.add("subTitle 3");
        for (int i = 0; i < 9; i++) {
            list.add(new MultiType("item "+i));
        }
        return list;
    }
}
