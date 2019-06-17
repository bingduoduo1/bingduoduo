
package com.bingduoduo.editor.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bingduoduo.editor.adapter.FileListAdapter;
import com.bingduoduo.editor.adapter.OnItemClickLitener;
import com.bingduoduo.editor.base.BaseApplication;
import com.bingduoduo.editor.base.BaseRefreshFragment;
import com.bingduoduo.editor.engine.ActionModeCallback;
import com.bingduoduo.editor.entity.FileBean;
import com.bingduoduo.editor.presenter.FolderManagerPresenter;
import com.bingduoduo.editor.presenter.IFolderManagerView;
import com.bingduoduo.editor.utils.Check;
import com.bingduoduo.editor.utils.ViewUtils;
import com.bingduoduo.editor.widget.TabView;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.termux.R;
import com.termux.app.TermuxActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.TextInputLayout;
//import androidx.core.widget.SwipeRefreshLayout;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.view.ActionMode;
//import android.support.v7.widget.AppCompatAutoCompleteTextView;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.SearchView;

/**
 * 文件管理界面
 */
public class FolderManagerFragment extends BaseRefreshFragment
        implements IFolderManagerView, View.OnClickListener, OnItemClickLitener {
    
    @Bind(R.id.content_view)
    protected RecyclerView mfileList;
    @Bind(R.id.tab_view)
    protected TabView tabView;
    @Bind(R.id.noContent)
    protected View noContent;
    @Bind(R.id.menu2_add) // default is fab;
    protected FloatingActionButton actionButton;
    
    private FolderManagerPresenter presenter;
    
    private List<FileBean> files = new ArrayList<>();
    private FileListAdapter adapter;
    
    private ActionMode actionMode;
    
    // 文件粘贴模式ActionMode
    private ActionMode.Callback pasteModeCallback;
    // 文件编辑模式ActionMode
    private ActionMode.Callback editModeCallback;
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();// VP分离
        presenter = null;
    }
    
    @Override
    protected void onRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        presenter.refreshCurrentPath();
        presenter.closeEditMode();
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.fragment_folder_manager;
    }
    
    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        super.onCreateAfter(savedInstanceState);
        initActionMode();
        // 初始化Presenter
        presenter = new FolderManagerPresenter(files);
        presenter.attachView(this);
        
        // 初始化recycleView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mcontext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mfileList.setLayoutManager(linearLayoutManager);
        mfileList.setAdapter(adapter = new FileListAdapter(mcontext, files));
        mfileList.setItemAnimator(new DefaultItemAnimator());
        mfileList.setLongClickable(true);
        adapter.setOnItemClickLitener(this);
        // 如果是ListView 多选可以用MultiChoiceModeListener会方便很多
    }
    
    @Override
    public void initData() {
        presenter.initRoot(mcontext);
    }
    
    /**
     * 错误回调
     * On failure.
     */
    @Override
    public void onFailure(int errorCode, String message, int flag) {
        switch (flag) {
            case CALL_GET_FILES:// 停止刷新，并提示失败原因
                finishRefresh();
                break;
            default:
                BaseApplication.showSnackbar(getSwipeRefreshLayout(), message);
                break;
        }
        
    }
    
    @Override
    public void showWait(String message, boolean canBack, int flag) {
        switch (flag) {
            case CALL_GET_FILES:// 获取文件列表
                getSwipeRefreshLayout().setRefreshing(true);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void hideWait(int flag) {
        switch (flag) {
            case CALL_GET_FILES:// 获取文件列表
                finishRefresh();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void getFileListSuccess(List<FileBean> files) {
        adapter.notifyDataSetChanged();
        
        if (files.isEmpty()) {
            noContent.setVisibility(View.VISIBLE);
        } else {
            noContent.setVisibility(View.GONE);
        }
        finishRefresh();
    }
    
    @Override
    public void otherSuccess(int flag) {
        switch (flag) {
            case CALL_COPY_PASTE:// 复制粘贴回调
            case CALL_CUT_PASTE:// 剪切粘贴回调
                // getSwipeRefreshLayout().postDelayed(()->refresh(),50);
                break;
            case CALL_PASTE_MODE:// 进入粘贴模式
                pasteMode();
                break;
            case CALL_CLOSE_PASTE_MODE:// 关闭粘贴模式粘贴模式
                closeActionMode();
                break;
            case CALL_EDIT_MODE:// 进入编辑模式
                openEditMode();
                break;
            case CALL_CLOSE_EDIT_MODE:// 关闭编辑模式
                closeEditMode();
                break;
            case CALL_REMOVE_TAB:// 移除标题
                removeTab();
                break;
            default:
                break;
        }
    }
    
    private void closeEditMode() {
        adapter.setEditMode(false);
        adapter.notifyDataSetChanged();
        closeActionMode();
    }
    
    private void openEditMode() {
        adapter.setEditMode(true);
        // 打开编辑模式的ActionMode
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(editModeCallback);
        actionMode.setTitle(String.valueOf(presenter.getSelectCount()));
    }
    
    private void closeActionMode() {
        // adapter.notifyDataSetChanged();
        if (actionMode != null)
        {
            actionMode.finish();
        }
        actionMode = null;
    }
    
    private void pasteMode() {
        // 打开粘贴模式的ActionMode
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(pasteModeCallback);
        actionMode.setTitle("请选择粘贴位置");
    }
    
    @Override
    public void addTab(String title) {
        tabView.addTab(title, this);
    }
    
    private boolean removeTab() {
        return tabView.removeTab();
    }
    
    @Override
    public void updatePosition(int position, FileBean bean) {
        adapter.notifyItemChanged(position);
    }
    
    @Override
    public void addFilePosition(int position, FileBean bean) {
        adapter.addData(position, bean);
    }
    
    @Override
    public void onItemClick(View view, int position) {
        FileBean fileBean = files.get(position);
        
        // 编辑模式下，这选择文件
        if (presenter.isEditMode() && actionMode != null) {
            fileBean.isSelect = !fileBean.isSelect;
            adapter.notifyItemChanged(position);
            // 算出当前选择数量，赋值到标题
            int selectCount = presenter.getSelectCount();
            // 如果数量等于1，这显示重命名菜单，否则隐藏
            // 如果数量为0，这关闭编辑模式
            if (selectCount == 0) {
                actionMode.setTitle("");
                presenter.closeEditMode();
            } else if (selectCount == 1) {
                actionMode.setTitle(String.valueOf(selectCount));
                actionMode.getMenu().findItem(R.id.action_edit).setVisible(true);
            } else {
                actionMode.setTitle(String.valueOf(selectCount));
                actionMode.getMenu().findItem(R.id.action_edit).setVisible(false);
            }
            return;
        }
        
        // 非编辑模式下
        if (fileBean.isDirectory) {
            // 文件夹
            presenter.enterFolder(fileBean.absPath);
        } else {
            // 文件
            Intent intent = new Intent(mcontext, EditorActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            // 设置数据URI与数据类型匹配
            intent.setDataAndType(Uri.fromFile(new File(fileBean.absPath)), "file");
            ViewUtils.startActivity(intent, getActivity(), view, EditorActivity.SHARED_ELEMENT_NAME);
        }
        
    }
    
    @Override
    public void onItemLongClick(View view, int position) {
        if (presenter.isEditMode())
        {
            return;
        }
        FileBean fileBean = files.get(position);
        fileBean.isSelect = !fileBean.isSelect;
        adapter.notifyItemChanged(position);
        view.postDelayed(() -> presenter.openEditMode(), 5);
        
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_name:
                Object tag = v.getTag(R.id.tag);
                if (tag != null && tag instanceof Integer) {
                    // 点击顶部导航
                    int index = ((Integer) tag).intValue();
                    presenter.backFolder(index);
                }
                break;
            default:
                break;
        }
        
    }
    
    @Override
    public boolean hasMenu() {
        return true;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folder_manager, menu);
        
        initSearchView(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    /**
     * 初始化SearchView
     * Init search view.
     */
    private SearchView searchView;
    boolean searchViewIsShow;
    
    private void initSearchView(Menu menu) {
        // SearchView相关
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                BaseApplication.showSnackbar(getSwipeRefreshLayout(), "" + s);
                if (!Check.isEmpty(s)) {
                    presenter.searchCurrentPath(s);
                }
                searchView.setIconified(false);
                return true;
            }
            
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0)
                {
                    presenter.refreshCurrentPath();
                }
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((view, queryTextFocused) -> {
            if (queryTextFocused)
            {
                searchViewIsShow = true;
            }
        });
        
        AppCompatAutoCompleteTextView editText = (AppCompatAutoCompleteTextView) searchView
                .findViewById(com.google.android.material.R.id.search_src_text);
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_folder:
                createFolder();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 新建文件（文章）
     * Create file.
     */
    private void createNote() {
        Intent intent = new Intent(mcontext, EditorActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        // 设置数据URI与数据类型匹配
        String path = new File(presenter.currentPath()).getPath();
        intent.setDataAndType(Uri.fromFile(new File(path)), "file");
        mcontext.startActivity(intent);
        
    }
    
    @OnClick(R.id.menu2_add) // default is R.id.fab
    public void newNote(View v) {
        Intent intent = new Intent(mcontext, EditorActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        // 设置数据URI与数据类型匹配
        String path = new File(presenter.currentPath()).getPath();
        intent.setDataAndType(Uri.fromFile(new File(path)), "file");
        // ViewUtils.startActivity(intent, getActivity(), v, EditorActivity.SHARED_ELEMENT_COLOR_NAME);
        startActivity(intent);
    }
    
    @OnClick(R.id.menu2_fab_switch) // default is R.id.fab
    public void OnClick(View view) {
        // NavigationView navigation = (NavigationView) getActivity().findViewById(R.id.id_navigation_view);
        
        // Menu menu = navigation.getMenu();
        // Switch nightSwitcher=(Switch) menu.findItem(R.id.night_pattern_switch);
        
        Switch nightSwitcher = (Switch) getActivity().findViewById(R.id.switcher);
        boolean isNgiht = nightSwitcher.isChecked();
        // Toast.show(mcontext,isNgiht?"night":"day", Toast.LENGTH_SHORT);
        Intent intent = new Intent(mcontext, TermuxActivity.class);
        intent.putExtra("isNightMode", isNgiht);
        startActivity(intent);
    }
    
    /**
     * 新建文件夹
     * Create folder.
     */
    public void createFolder() {
        
        // 显示重命名对话框
        View rootView = LayoutInflater.from(mcontext).inflate(R.layout.view_common_input_view, null);
        
        AlertDialog alertDialog = new AlertDialog.Builder(mcontext).setTitle("新建文件夹").setView(rootView).show();
        
        TextInputLayout textInputLayout = (TextInputLayout) rootView.findViewById(R.id.inputHint);
        EditText text = (EditText) rootView.findViewById(R.id.text);
        textInputLayout.setHint("请输入文件夹名");
        rootView.findViewById(R.id.sure).setOnClickListener(v -> {
            String result = text.getText().toString().trim();
            
            if (Check.isEmpty(result)) {
                textInputLayout.setError("不能为空");
                return;
            }
            if (presenter.createFoloderIsExists(result)) {
                textInputLayout.setError("文件已经存在");
                return;
            }
            presenter.createFolder(result);
            alertDialog.dismiss();
        });
        rootView.findViewById(R.id.cancel).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
    }
    
    @Override
    public boolean onBackPressed() {
        // 返回按钮
        if (searchView != null && searchView.isShown() && searchViewIsShow) {
            // 搜索菜单打开了
            searchView.onActionViewCollapsed(); // 关闭ActionView(SearchView)
            searchView.setQuery("", false); // 清空输入框
            getActivity().supportInvalidateOptionsMenu();// 恢复
            searchViewIsShow = false;
            return true;
        } else {
            return presenter.backFolder();
        }
    }
    
    /**
     * 初始化ActionMode的CallBack
     * Init action mode.
     */
    
    private void initActionMode() {
        pasteModeCallback = new ActionModeCallback(getActivity(), R.color.colorPrimary) {
            @Override
            public void onDestroyActionModeCustom(ActionMode mode) {
                actionMode = null;
            }
            
            @Override
            public boolean onCreateActionModeCustom(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                mode.setTitle("1");
                inflater.inflate(R.menu.menu_action_paste, menu);
                return true;
            }
            
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean flag = false;
                switch (item.getItemId()) {
                    case R.id.action_paste:
                        presenter.paste();
                        flag = true;
                        break;
                    case R.id.action_create_folder:
                        createFolder();
                        flag = true;
                        break;
                    default:
                        break;
                }
                return flag;
            }
            
        };
        editModeCallback = new ActionModeCallback(getActivity(), R.color.colorPrimary) {
            @Override
            public void onDestroyActionModeCustom(ActionMode mode) {
                presenter.closeEditMode();
                actionMode = null;
            }
            
            @Override
            public boolean onCreateActionModeCustom(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                // mode.setTitle("1");
                inflater.inflate(R.menu.menu_action_folder, menu);
                menu.findItem(R.id.action_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.action_cut).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }
            
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean ret = false;
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        rename();
                        break;
                    case R.id.action_delete:
                        deleteFiles();
                        break;
                    case R.id.action_copy:
                        coptFiles();
                        break;
                    case R.id.action_cut:
                        cutFiles();
                        break;
                    default:
                        break;
                }
                return ret;
            }
            
        };
    }
    
    /**
     * Re name.
     * 重命名文件、文件夹
     */
    private void rename() {
        FileBean selectBean = presenter.getSelectBean();
        if (selectBean == null) {
            return;
        }
        
        // 显示重命名对话框
        View rootView = LayoutInflater.from(mcontext).inflate(R.layout.view_common_input_view, null);
        
        AlertDialog dialog;
        dialog = new AlertDialog.Builder(mcontext).setTitle("重命名").setView(rootView).show();
        
        TextInputLayout textInputLayout = (TextInputLayout) rootView.findViewById(R.id.inputHint);
        EditText text = (EditText) rootView.findViewById(R.id.text);
        text.setText(selectBean.name);
        text.setSelection(0, selectBean.isDirectory ? selectBean.name.length() : selectBean.name.lastIndexOf("."));
        textInputLayout.setHint("请输入" + (selectBean.isDirectory ? "文件夹名" : "文件名"));
        rootView.findViewById(R.id.sure).setOnClickListener(v -> {
            String result = text.getText().toString().trim();
            if (Check.isEmpty(result)) {
                textInputLayout.setError("不能为空");
                return;
            }
            if (!selectBean.isDirectory && presenter.fileIsExists(result)) {
                textInputLayout.setError("文件已经存在");
                return;
            }
            if (selectBean.isDirectory && presenter.createFoloderIsExists(result)) {
                textInputLayout.setError("文件夹已经存在");
                return;
            }
            if (!presenter.rename(selectBean, result)) {
                textInputLayout.setError("重命名失败");
                return;
            }
            
            textInputLayout.setErrorEnabled(false);
            
            if (actionMode != null) {
                actionMode.finish();
            }
            dialog.dismiss();
        });
        
        rootView.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        dialog.show();
        
    }
    
    /**
     * 删除
     * Delete file.
     */
    private void deleteFiles() {
        int selectCount = presenter.getSelectCount();
        if (selectCount <= 0) {
            BaseApplication.showSnackbar(getSwipeRefreshLayout(), "请选择文件");
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        builder.setMessage(String.format("确定删除选择的%d项？", selectCount)).setNegativeButton("不删", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("删除", (dialog1, which) -> {
            if (presenter.delete()) {
                BaseApplication.showSnackbar(getSwipeRefreshLayout(), "已经删除");
                // adapter.removeData();
                refresh();
            }
            if (actionMode != null) {
                actionMode.finish();
            }
            dialog1.dismiss();
        }).show();
        
    }
    
    /**
     * 复制
     * Copt files.
     */
    private void coptFiles() {
        int selectCount = presenter.getSelectCount();
        if (selectCount <= 0) {
            BaseApplication.showSnackbar(getSwipeRefreshLayout(), "请选择文件");
            return;
        }
        
        presenter.copy();
        
    }
    
    /**
     * 剪切
     * Cut files.
     */
    private void cutFiles() {
        int selectCount = presenter.getSelectCount();
        if (selectCount <= 0) {
            BaseApplication.showSnackbar(getSwipeRefreshLayout(), "请选择文件");
            return;
        }
        presenter.cut();
        
    }
}
