
package com.bingduoduo.editor.base;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.termux.R;

import butterknife.Bind;

/**
 * 带有下拉刷新的activity
 */
public abstract class BaseRefreshActivity extends BaseToolbarActivity {
    
    @Bind(R.id.id_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    
    @Override
    protected void init() {
        super.init();
        initRefresh();
    }
    
    private void initRefresh() {
        if (swipeRefreshLayout == null) {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + ":要使用BaseRefreshActivity，必须在布局里面增加id为‘id_refresh’的MaterialRefreshLayout");
        }
        swipeRefreshLayout.setColorSchemeColors(getColors());
        swipeRefreshLayout.setOnRefreshListener(() -> BaseRefreshActivity.this.onRefresh(swipeRefreshLayout));
    }
    
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
    
    protected int[] getColors() {
        int[] colors = { BaseApplication.color(R.color.colorPrimary) };
        return colors;
    }
    
    protected final boolean isRefresh() {
        return swipeRefreshLayout.isRefreshing();
    }
    
    protected final boolean refresh() {
        if (isRefresh()) {
            return false;
        }
        swipeRefreshLayout.setRefreshing(true);
        onRefresh(swipeRefreshLayout);
        return true;
    }
    
    protected final boolean finishRefresh() {
        if (!isRefresh()) {
            return false;
        }
        swipeRefreshLayout.setRefreshing(false);
        return true;
    }
    
    protected abstract void onRefresh(SwipeRefreshLayout swipeRefreshLayout);
    
}
