package com.ganba.xrecyclerview.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ganba.xrecyclerview.R;
import com.ganba.xrecyclerview.XRecyclerView;
import com.ganba.xrecyclerview.adapter.RVAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class LayoutManagerActivity extends AppCompatActivity {

    XRecyclerView mRecyclerView;
    RVAdapter<String> mAdapter;
    private Button btnInserted, btnChange, btnMove;
    private int pageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_layout_manager);
        initView();
        initAdapter();
        initListener();
        initData();
    }

    private void initView() {
        btnInserted = findViewById(R.id.btn_inserted);
        btnChange = findViewById(R.id.btn_change);
        btnMove = findViewById(R.id.btn_move);
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

    }

    private void initAdapter() {
//        mRecyclerView.setRefreshView();
//        mRecyclerView.setLoadMoreView();
//        mRecyclerView.setRefreshEnabled(false);
//        mRecyclerView.setLoadMoreEnabled(false);
//        mRecyclerView.setLimitNumberToCallLoadMore(3);
//        mRecyclerView.setHeadFootWithEmptyEnabled(true);
        mAdapter = new RVAdapter<String>(this, android.R.layout.test_list_item) {
            @Override
            protected void convert(ViewHolder vH, String item, int position) {
                vH.setText(android.R.id.text1, item);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initListener() {
        btnInserted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = "插入项";
                mAdapter.getData().add(2, item);
                mAdapter.notifyItemRangeInserted(2, 1);
            }
        });
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.getData().set(4, "更改项");
                mAdapter.notifyItemChanged(4);
            }
        });
        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.notifyItemMoved(6, 8);
            }
        });

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageNum = 1;
                        mAdapter.replaceAll(getData());
                        mRecyclerView.refreshComplete();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageNum++;
                        mAdapter.addAll(getData());
                        mRecyclerView.loadMoreComplete();
                        if (pageNum == 3) {
                            mRecyclerView.setNoMore(true);
                        }
                    }
                }, 2000);
            }
        });

        mAdapter.setOnItemClickListener(new RVAdapter.OnItemClickListener<String>() {
            @Override
            public void onItemClick(View view, String item, int position) {
                Toast.makeText(LayoutManagerActivity.this, item + "  点击" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mAdapter.setOnItemLongClickListener(new RVAdapter.OnItemLongClickListener<String>() {
            @Override
            public void onItemLongClick(View view, String item, int position) {
                Toast.makeText(LayoutManagerActivity.this, item + "  长按" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        mRecyclerView.autoRefresh();
    }

    private List<String> getData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add("item ;" + i);
        }
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mAdapter.remove(0);
                break;
            case R.id.action_linear:
                LinearLayoutManager linear = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(linear);
                break;
            case R.id.action_grid:
                GridLayoutManager grid = new GridLayoutManager(this, 2);
                mRecyclerView.setLayoutManager(grid);
                break;
            case R.id.action_staggeredGrid:
                StaggeredGridLayoutManager staggeredGrid =
                        new StaggeredGridLayoutManager(3, VERTICAL);
                mRecyclerView.setLayoutManager(staggeredGrid);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
