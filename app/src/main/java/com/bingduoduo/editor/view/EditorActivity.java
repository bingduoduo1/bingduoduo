
package com.bingduoduo.editor.view;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
//import android.support.design.widget.TextInputLayout;

//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentManager;
//import androidx.core.app.FragmentPagerAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
//import androidx.core.view.ViewPager;
//import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.util.Log;

import com.github.clans.fab.FloatingActionButton;
import com.termux.R;
import com.bingduoduo.editor.base.BaseApplication;
import com.bingduoduo.editor.base.BaseToolbarActivity;
import com.bingduoduo.editor.presenter.IEditorActivityView;
import com.bingduoduo.editor.utils.Check;
import com.bingduoduo.editor.utils.FileUtils;
import com.bingduoduo.editor.utils.SystemBarUtils;
import com.bingduoduo.editor.utils.Toast;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.Bind;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.speech.util.FucUtil;
import com.iflytek.voicedemo.SpeechRecognitionIat;
import com.termux.terminal.TerminalSession;

import static android.content.ContentValues.TAG;

public class EditorActivity extends BaseToolbarActivity implements IEditorActivityView, View.OnClickListener {
    public static final String SHARED_ELEMENT_NAME = "SHARED_ELEMENT_NAME";
//    public static final String SHARED_ELEMENT_COLOR_NAME = "SHARED_ELEMENT_COLOR_NAME";
    private static final String SCHEME_FILE = "file";
//    private static final String SCHEME_Folder = "folder";

    private EditorFragment mEditorFragment;

    private String mName;
    private String currentFilePath;

    // 语音识别相关
    private SpeechRecognitionIat mReconition;
    private static StringBuffer mret= new StringBuffer();
    private Handler han = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mret.append(mReconition.getAction());

            doAction();
            mReconition.stopRecognize();
        }

        private void doAction() {
            int pos = mEditorFragment.mContent.getSelectionStart();
            Editable e = mEditorFragment.mContent.getText();
            if(mret.length()==0){
                //pass 无Action
                // or \n?
                e.insert(pos, "\n");
            }else{

                switch(mret.toString()){
                    case "backspace":// 参考 TermuxView:row:682
                        e.insert(pos, " ");
                        break;
                    default:
                        e.insert(pos, mret.toString());

                }
                mret.setLength(0);

            }
        }
    };


    @Bind(R.id.pager)
    protected ViewPager mViewPager;
    // private TabIconView mTabIconView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_editor;
    }

    @Override
    public void onCreateAfter(Bundle savedInstanceState) {
        ViewCompat.setTransitionName(mViewPager, SHARED_ELEMENT_NAME);
//        ViewCompat.setTransitionName(mViewPager, SHARED_ELEMENT_COLOR_NAME);
//        mExpandLayout = (ExpandableLinearLayout) getLayoutInflater().inflate(R.layout.view_edit_operate, getAppBar(), false);
//        getAppBar().addView(mExpandLayout);

        getIntentData();
        mEditorFragment = EditorFragment.getInstance(currentFilePath);

        initViewPager();
        SpeechUtility.createUtility(this, "appid=5c9cc920");// appid需要和sdk的id相匹配
        mReconition = new SpeechRecognitionIat( EditorActivity.this,"userwords");

        FloatingActionButton btn_voice = (FloatingActionButton) findViewById(R.id.fab_voice_editor);
        btn_voice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // 按住事件发生后执行代码的区域
                        // mReconition.cancelRecognize();
                        Log.d(TAG, "upup31312 : "+System.currentTimeMillis());
                        mReconition.startRecognize();
                        // han.sendEmptyMessageDelayed(0,1000);

                        break;
                        // 开始识别
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // 移动事件发生后执行代码的区域
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        // 松开事件发生后执行代码的区域
//                        String ret = mReconition.getAction();
//                        Log.d(TAG, "return_message:"+ret);
                        Message message = new Message();
                        message.what=0;
                        han.sendMessageDelayed(message, 800);

                        break;
                    }
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void initData() {

    }

    private void initViewPager() {
        mViewPager.setAdapter(new EditFragmentAdapter(getSupportFragmentManager()));
    }

    @Override
    protected void initStatusBar() {
        SystemBarUtils.tintStatusBar(this, getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void otherSuccess(int flag) {
    }

    @Override
    public void onFailure(int errorCode, String message, int flag) {
        switch (flag) {
            default:
                BaseApplication.showSnackbar(getWindow().getDecorView(), message);
                break;
        }
    }


    @Override
    public void showWait(String message, boolean canBack, int flag) {
        super.showWaitDialog(message, canBack);
    }

    @Override
    public void hideWait(int flag) {
        super.hideWaitDialog();
    }

    @Override
    public void onNameChange(@NonNull String name) {
        this.mName = name;
    }

    private final int SYSTEM_GALLERY = 1;

    @Override
    public void onClick(View v) {

    }



    private class EditFragmentAdapter extends FragmentPagerAdapter {

        public EditFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mEditorFragment;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }



    private void getIntentData() {
        Intent intent = this.getIntent();
        int flags = intent.getFlags();
        if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (intent.getAction() != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                if (SCHEME_FILE.equals(intent.getScheme())) {
                    // 文件
                    String type = getIntent().getType();
                    // mImportingUri=file:///storage/emulated/0/Vlog.xml
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri uri = intent.getData();

                    if (uri != null && SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
                        // 这是一个文件
                        currentFilePath = FileUtils.uri2FilePath(getBaseContext(), uri);
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    protected String getTitleString() {
        return "";
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    private MenuItem mActionOtherOperate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_act, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mEditorFragment.onBackPressed()) {
                    return true;
                }
                break;
            case R.id.action_edit:// 编辑
                mViewPager.setCurrentItem(0, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
