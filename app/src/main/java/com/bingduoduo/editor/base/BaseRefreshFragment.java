
package com.bingduoduo.editor.base;

import android.os.Bundle;
//import androidx.core.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.termux.R;

import butterknife.Bind;

/**
 * 带下拉刷新的Fragment
 */
public abstract class BaseRefreshFragment extends BaseFragment {
    
    @Bind(R.id.id_refresh)
    protected SwipeRefreshLayout swipeRefreshLayout;
    
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }
    
    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        initRefresh();
    }
    
    private void initRefresh() {
        if (swipeRefreshLayout == null) {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + ":要使用BaseRefreshFragment，必须在布局里面增加id为‘id_refresh’的MaterialRefreshLayout");
        }
        swipeRefreshLayout.setColorSchemeColors(getColors());
        swipeRefreshLayout.setOnRefreshListener(() -> BaseRefreshFragment.this.onRefresh(swipeRefreshLayout));
    }
    
    protected int[] getColors() {
        int[] colors = { BaseApplication.color(R.color.colorPrimary) };
        return colors;
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
    
    protected final boolean isRefresh() {
        return swipeRefreshLayout.isRefreshing();
    }
    
    /**
     * On refresh.刷新回调
     *
     * @param swipeRefreshLayout the swipe refresh layout
     */
    protected abstract void onRefresh(SwipeRefreshLayout swipeRefreshLayout);
    
}
