package com.bingduoduo.editor.base;

import android.content.Context;
import android.os.Bundle;
//import androidx.core.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;

/**
 * fragment基类
 */
public abstract class BaseFragment extends Fragment implements BaseViewInterface {
    protected Context mcontext;
    protected View rootView;
    protected BaseApplication application;

    private Bundle savedState;

    private static final String SAVE_KEY = "SAVE_KEY_131231231239";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mcontext = getActivity();
        application = (BaseApplication) mcontext.getApplicationContext();
        if (rootView == null) {
            rootView = View.inflate(getActivity(), getLayoutId(), null);
        }
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstFocused) {
            isFirstFocused = false;
            initData();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!restoreStateFromArguments()) {
            onFirstLaunched();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 保存状态
        saveStateToArguments();
        ButterKnife.unbind(rootView);
        mcontext = null;
        rootView = null;
    }

    protected void onFirstLaunched() {
        //包含菜单到所在Activity
        setHasOptionsMenu(hasMenu());
        onCreateAfter(null);
    }

    private boolean isFirstFocused = true;

    public boolean hasMenu() {
        return false;
    }
    /**
     * 返回键，预留给所在activity调用
     * On back pressed boolean.
     *
     * @return the boolean
     */

    public boolean onBackPressed() {
        return false;
    }

    /**
     * 需要重写hasMenu() 返回True，才会创建菜单
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 需要重写hasMenu() 返回True，才会回调
     * On options item selected boolean.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存状态
        saveStateToArguments();
    }

    private void saveStateToArguments() {
        if (getView() != null)
        {
            savedState = new Bundle();
        }
        if (savedState != null) {
            Bundle b = getArguments();
            if (b != null) {
                b.putBundle(SAVE_KEY, savedState);
            }

        }
    }

    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();
        if (b == null) {
            return false;
        }
        savedState = b.getBundle(SAVE_KEY);
        if (savedState == null) {
            return false;
        }
        return true;
    }

    protected void onSaveState(Bundle outState) {
    }
}
