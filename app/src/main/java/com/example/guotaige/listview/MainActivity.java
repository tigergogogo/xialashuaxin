package com.example.guotaige.listview;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PullToFreshListView listView;
    private ArrayList<String> datas;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (PullToFreshListView) findViewById(R.id.listview);
        //模拟数据
        datas = new ArrayList<String>();
        for (int i = 0;i < 20;i++){
            datas.add("哈哈哈，我捡到钱啦 "+i);
        }
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datas);
        listView.setAdapter(adapter);

        listView.setOnRefreshingListener(new PullToFreshListView.OnRefreshingListener() {
            @Override
            public void onRefreshing() {
                reLoadData();
            }
                                             @Override
                                             public void onLoadMore() {
                                                 loadMore();
                                             }
                                         }

        );

    }

    /**
     * 重新联网获取数据
     */
    private void reLoadData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                datas.add(0, "我是刷新出来的数据");
                adapter.notifyDataSetChanged();
                //隐藏头部view
                listView.onRefreshFinished();
            }
        }, 3000);
    }

    private void loadMore(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                datas.add("我是加载更多的数据");
                adapter.notifyDataSetChanged();
                //隐藏头部view
                listView.onLoadMoreFinished();
            }
        },3000);
    }


}
