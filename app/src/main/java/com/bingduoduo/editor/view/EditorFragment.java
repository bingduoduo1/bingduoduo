
package com.bingduoduo.editor.view;

import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.bingduoduo.editor.base.BaseApplication;
import com.bingduoduo.editor.base.BaseFragment;
import com.bingduoduo.editor.base.mvp.IMvpView;
import com.bingduoduo.editor.engine.PerformEditable;
import com.bingduoduo.editor.engine.PerformInputAfter;
import com.bingduoduo.editor.presenter.EditorFragmentPresenter;
import com.bingduoduo.editor.presenter.IEditorFragmentView;
import com.termux.R;

import java.io.File;

import butterknife.Bind;
import ren.qinc.edit.PerformEdit;

public class EditorFragment extends BaseFragment implements IEditorFragmentView, View.OnClickListener {
    public static final String FILE_PATH_KEY = "FILE_PATH_KEY";
    @Bind(R.id.title)
    protected EditText mname;
    @Bind(R.id.content)
    protected EditText mcontent;
    
    private EditorFragmentPresenter mpresenter;
    
    private PerformEditable mperformeditable;
    private PerformEdit mperformedit;
    private PerformEdit mperformnameedit;
    
    public EditorFragment() {
    }
    
    public static EditorFragment getInstance(String filePath) {
        EditorFragment editorFragment = new EditorFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FILE_PATH_KEY, filePath);
        editorFragment.setArguments(bundle);
        return editorFragment;
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.fragment_editor;
    }
    
    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        
        Bundle arguments = getArguments();
        String fileTemp = arguments.getString(FILE_PATH_KEY);
        if (fileTemp == null) {
            Toast.makeText(BaseApplication.context(), "路径参数有误！", Toast.LENGTH_SHORT).show();
            return;
        }
        
        File file = new File(fileTemp);
        // 创建新文章
        mpresenter = new EditorFragmentPresenter(file);
        mpresenter.attachView(this);
        
        // 代码格式化或者插入操作
        mperformeditable = new PerformEditable(mcontent);
        
        // 撤销和恢复初始化
        mperformedit = new PerformEdit(mcontent) {
            @Override
            protected void onTextChanged(Editable s) {
                // 文本改变
                mpresenter.textChange();
            }
        };
        
        mperformnameedit = new PerformEdit(mname) {
            @Override
            protected void onTextChanged(Editable s) {
                // 文本改变
                mpresenter.textChange();
            }
        };
        
        // 文本输入监听(用于自动输入)
        PerformInputAfter.start(mcontent);
        
        // 装置数据
        if (file.isFile())
        {
            mpresenter.loadFile();
        }
    }
    
    @Override
    public void initData() {
        
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mpresenter != null)
        {
            mpresenter.detachView();// VP分离
        }
        mpresenter = null;
    }
    
    @Override
    public void otherSuccess(int flag) {
        switch (flag) {
            case CALL_EXIT:
                getActivity().finish();
                break;
            case CALL_NO_SAVE:
                noSave();
                break;
            case CALL_SAVE:
                saved();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onFailure(int errorCode, String message, int flag) {
        switch (flag) {
            case CALL_SAVE:
            case CALL_LOAOD_FILE:
                BaseApplication.showSnackbar(mcontent, message);
                break;
            default:
                BaseApplication.showSnackbar(mcontent, message);
                break;
        }
    }
    
    @Override
    public void showWait(String message, boolean canBack, int flag) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (!(activity instanceof IMvpView)) {
            return;
        }
        IMvpView imvpview = (IMvpView) activity;
        imvpview.showWait(message, canBack, flag);
        
    }
    
    @Override
    public void hideWait(int flag) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (!(activity instanceof IMvpView)) {
            return;
        }
        IMvpView imvpview = (IMvpView) activity;
        imvpview.hideWait(flag);
    }
    
    @Override
    public boolean hasMenu() {
        return true;
    }
    
    // 菜单
    private MenuItem mactionsave;
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor_frag, menu);
        mactionsave = menu.findItem(R.id.action_save);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:// 撤销
                mperformedit.undo();
                return true;
            case R.id.action_redo:// 重做
                mperformedit.redo();
                return true;
            case R.id.action_save:// 保存
                mpresenter.save(mname.getText().toString().trim(), mcontent.getText().toString().trim());
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onReadSuccess(@NonNull String name, @NonNull String content) {
        mperformnameedit.setDefaultText(name);
        mperformedit.setDefaultText(content);
        if (content.length() > 0) {
            //todo
        }
    }
    
    public void noSave() {
        if (mactionsave == null)
        {
            return;
        }
        mactionsave.setIcon(R.drawable.ic_action_unsave);
    }
    
    public void saved() {
        if (mactionsave == null)
        {
            return;
        }
        mactionsave.setIcon(R.drawable.ic_action_save);
    }
    
    @Override
    public boolean onBackPressed() {
        if (mpresenter.isSave()) {
            return false;
        }
        onNoSave();
        return true;
    }
    
    private void onNoSave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        builder.setMessage("当前文件未保存，是否退出?");
        builder.setNegativeButton("不保存", (dialog, which) -> {
            getActivity().finish();
            
        }).setNeutralButton("取消", (dialog, which) -> {
            dialog.dismiss();
            
        }).setPositiveButton("保存", (dialog, which) -> {
            mpresenter.saveForExit(mname.getText().toString().trim(), mcontent.getText().toString().trim(), true);
            
        }).show();
    }
    
    @Override
    public void onClick(View v) {
        
    }
}
