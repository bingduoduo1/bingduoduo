

package com.bingduoduo.editor.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.termux.R;
import com.bingduoduo.editor.base.BaseApplication;
import com.bingduoduo.editor.base.BaseFragment;
import com.bingduoduo.editor.base.mvp.IMvpView;
import com.bingduoduo.editor.engine.PerformEditable;
import com.bingduoduo.editor.engine.PerformInputAfter;
import com.bingduoduo.editor.presenter.EditorFragmentPresenter;
import com.bingduoduo.editor.presenter.IEditorFragmentView;
import com.bingduoduo.editor.utils.SystemUtils;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import butterknife.Bind;
import de.mrapp.android.bottomsheet.BottomSheet;
import ren.qinc.edit.PerformEdit;

public class EditorFragment extends BaseFragment implements IEditorFragmentView, View.OnClickListener {
    public static final String FILE_PATH_KEY = "FILE_PATH_KEY";
    @Bind(R.id.title)
    protected EditText mName;
    @Bind(R.id.content)
    protected EditText mContent;

    private EditorFragmentPresenter mPresenter;

    private PerformEditable mPerformEditable;
    private PerformEdit mPerformEdit;
    private PerformEdit mPerformNameEdit;

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
        //创建新文章
        mPresenter = new EditorFragmentPresenter(file);
        mPresenter.attachView(this);

        //代码格式化或者插入操作
        mPerformEditable = new PerformEditable(mContent);

        //撤销和恢复初始化
        mPerformEdit = new PerformEdit(mContent) {
            @Override
            protected void onTextChanged(Editable s) {
                //文本改变
                mPresenter.textChange();
            }
        };

        mPerformNameEdit = new PerformEdit(mName) {
            @Override
            protected void onTextChanged(Editable s) {
                //文本改变
                mPresenter.textChange();
            }
        };

        //文本输入监听(用于自动输入)
        PerformInputAfter.start(mContent);

        //装置数据
        if (file.isFile())
            mPresenter.loadFile();
    }

    @Override
    public void initData() {

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null)
            mPresenter.detachView();//VP分离
        mPresenter = null;
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
        }
    }

    @Override
    public void onFailure(int errorCode, String message, int flag) {
        switch (flag) {
            case CALL_SAVE:
            case CALL_LOAOD_FILE:
                BaseApplication.showSnackbar(mContent, message);
                break;
            default:
                BaseApplication.showSnackbar(mContent, message);
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
        IMvpView iMvpView = (IMvpView) activity;
        iMvpView.showWait(message, canBack, flag);

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
        IMvpView iMvpView = (IMvpView) activity;
        iMvpView.hideWait(flag);
    }


    @Override
    public boolean hasMenu() {
        return true;
    }

    //菜单
    private MenuItem mActionSave;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor_frag, menu);
        mActionSave = menu.findItem(R.id.action_save);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo://撤销
                mPerformEdit.undo();
                return true;
            case R.id.action_redo://重做
                mPerformEdit.redo();
                return true;
            case R.id.action_save://保存
                mPresenter.save(mName.getText().toString().trim(), mContent.getText().toString().trim());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onReadSuccess(@NonNull String name, @NonNull String content) {
        mPerformNameEdit.setDefaultText(name);
        mPerformEdit.setDefaultText(content);
        if (content.length() > 0) {
        }
    }

    public void noSave() {
        if (mActionSave == null) return;
        mActionSave.setIcon(R.drawable.ic_action_unsave);
    }

    public void saved() {
        if (mActionSave == null) return;
        mActionSave.setIcon(R.drawable.ic_action_save);
    }




    @Override
    public boolean onBackPressed() {
        if (mPresenter.isSave()) {
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
            mPresenter.saveForExit(mName.getText().toString().trim(), mContent.getText().toString().trim(), true);

        }).show();
    }

    @Override
    public void onClick(View v) {

    }
}
