package com.example.guotaige.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by guotaige on 16/6/30.
 */
public class PullToFreshListView  extends ListView {


    private View headerView;
    private View footerView;
    private int headerViewHeight;
    //三种状态
    private static final int STATE_PULL_TO_REFRESH = 0;
    //松开刷新
    private static final int STATE_RELEASE_REFRESH = 1;
    //正在刷新
    private static final int STATE_REFRESHING = 3;
    //记录当前状态的变量
    private int currentState = STATE_PULL_TO_REFRESH;
    //按下的坐标
    private float downY;
    private ImageView iv_arrow;
    private ProgressBar progress_bar;
    private TextView tv_state;
    private RotateAnimation upAmin;
    private RotateAnimation downAnim;
    boolean loadingMore = false;


    public OnRefreshingListener listener;
    public PullToFreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderView();
        //底部view
        initFootView();
    }


    private void initHeaderView() {
        headerView = View.inflate(getContext(),R.layout.header_view,null);
        iv_arrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
        progress_bar = (ProgressBar) headerView.findViewById(R.id.pregress_bar);
        tv_state = (TextView) headerView.findViewById(R.id.tv_state);
        //主动调用系统中的onMeasure()
        headerView.measure(0, 0);
        //隐藏头部view
        hideHeaderView();
        addHeaderView(headerView);
        upAmin = createRotateAnim(0f,-180f);
        downAnim = createRotateAnim(-180f,-360f);
        

    }

    private void initFootView() {
        footerView = View.inflate(getContext(),R.layout.foot_view,null);
        addFooterView(footerView);
        footerView.measure(0, 0);
        // 隐藏底部
        hideFooterView();

        super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                        && getLastVisiblePosition() == getCount() - 1
                        && loadingMore == false) {
                    showFooterView();
                    if (listener != null){
                        loadingMore = true;
                        listener.onLoadMore();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

    }

    private void showFooterView() {
        setFooterViewPaddingTop(0);
        setSelection(getCount() - 1);
    }

    private void hideFooterView() {
        int footerViewHeight = footerView.getMeasuredHeight();
        int paddingFoot = -footerViewHeight;
        setFooterViewPaddingTop(paddingFoot);

    }
    private void hideHeaderView(){
        headerViewHeight =  headerView.getMeasuredHeight();
        int paddingTop = -headerViewHeight;
        setHeaderViewPaddingTop(paddingTop);
    }
    /**
     * 创建旋转动画
     */
    private RotateAnimation createRotateAnim(float fromDegrees,float toDegress){
        int pivotXType = RotateAnimation.RELATIVE_TO_SELF;
        int pivotYType = RotateAnimation.RELATIVE_TO_SELF;
        float pivotXValue = 0.5f;
        float pivotYValue = 0.5f;
        RotateAnimation ra = new RotateAnimation(fromDegrees,toDegress,pivotXType,pivotXValue,pivotYType,pivotYValue);
        ra.setDuration(300);
        ra.setFillAfter(true);
        return ra;
    }


    private void setHeaderViewPaddingTop(int paddingTop) {
        headerView.setPadding(0, paddingTop, 0, 0);
    }
    private void setFooterViewPaddingTop(int paddingTop) {
        footerView.setPadding(0, paddingTop, 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int paddingTop;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == STATE_REFRESHING){
                    return super.onTouchEvent(ev);
                }
                //Y方向的移动距离
                int fingerY =(int)(ev.getY() - downY);
                //如果是向下滑动，并且可见的item是第一条是才执行下拉刷新的操作
                if (fingerY > 0 && getFirstVisiblePosition() == 0){
                    paddingTop = -headerViewHeight + fingerY;
                    setHeaderViewPaddingTop(paddingTop);
                    if (paddingTop < 0 && currentState != STATE_PULL_TO_REFRESH){
                        currentState = STATE_PULL_TO_REFRESH;
                        tv_state.setText("下拉刷新");
                        iv_arrow.startAnimation(downAnim);

                    }else if (paddingTop >= 0 && currentState != STATE_RELEASE_REFRESH){
                        //松开刷新
                        currentState = STATE_RELEASE_REFRESH;
                        tv_state.setText("松开刷新");
                        iv_arrow.startAnimation(upAmin);
                    }
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
                if (currentState == STATE_RELEASE_REFRESH){
                    //状态为松开刷新，抬起手是改为正在刷新
                    currentState = STATE_REFRESHING;
                    tv_state.setText("正在刷新");
                    //箭头变为圈
                    iv_arrow.setVisibility(View.GONE);
                    //清除动画
                    iv_arrow.clearAnimation();
                    progress_bar.setVisibility(View.VISIBLE);
                    paddingTop = 0;
                    setHeaderViewPaddingTop(paddingTop);

                    if(listener != null){
                        listener.onRefreshing();
                    }
                }else if (currentState == STATE_PULL_TO_REFRESH){
                    hideHeaderView();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setOnRefreshingListener(OnRefreshingListener listener){
        this.listener = listener;
    }

    public void onRefreshFinished() {
        hideHeaderView();
        progress_bar.setVisibility(View.GONE);
        iv_arrow.setVisibility(View.VISIBLE);
        currentState = STATE_PULL_TO_REFRESH;
    }

    public void onLoadMoreFinished() {
        hideFooterView();
        loadingMore = false;
    }

    public interface OnRefreshingListener{
        void onRefreshing();
        void onLoadMore();
    }
}
