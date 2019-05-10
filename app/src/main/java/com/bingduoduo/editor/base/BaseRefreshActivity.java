

package com.bingduoduo.editor.base;

//import androidx.core.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.termux.R;

import butterknife.Bind;


/**
 * 带有下拉刷新的activity
 */
public abstract class BaseRefreshActivity extends BaseToolbarActivity {

    @Bind(R.id.id_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void init() {
        super.init();
        initRefresh();
    }

    private void initRefresh() {
        if (mSwipeRefreshLayout == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + ":要使用BaseRefreshActivity，必须在布局里面增加id为‘id_refresh’的MaterialRefreshLayout");
        }
        mSwipeRefreshLayout.setColorSchemeColors(getColors());
        mSwipeRefreshLayout.setOnRefreshListener(() -> BaseRefreshActivity.this.onRefresh(mSwipeRefreshLayout));
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    protected int[] getColors() {
        int[] colors = {BaseApplication.color(R.color.colorPrimary)};
        return colors;
    }


    protected final boolean isRefresh() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    protected final boolean refresh() {
        if (isRefresh()) {
            return false;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh(mSwipeRefreshLayout);
        return true;
    }

    protected final boolean finishRefresh() {
        if (!isRefresh()) {
            return false;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        return true;
    }

    protected abstract void onRefresh(SwipeRefreshLayout swipeRefreshLayout);

}
