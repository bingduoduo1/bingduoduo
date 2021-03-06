
package com.bingduoduo.editor.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
//import android.support.design.widget.TextInputLayout;

//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentManager;
//import androidx.core.app.FragmentPagerAdapter;
import androidx.core.view.ViewCompat;
//import androidx.core.view.ViewPager;
//import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;

import com.github.clans.fab.FloatingActionButton;
import com.termux.R;
import com.bingduoduo.editor.base.BaseApplication;
import com.bingduoduo.editor.base.BaseToolbarActivity;
import com.bingduoduo.editor.presenter.IEditorActivityView;
import com.bingduoduo.editor.utils.FileUtils;
import com.bingduoduo.editor.utils.SystemBarUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.Bind;

import com.iflytek.cloud.SpeechUtility;
import com.iflytek.voicedemo.SpeechRecognitionIat;

import static android.content.ContentValues.TAG;

public class EditorActivity extends BaseToolbarActivity
    implements IEditorActivityView, View.OnClickListener {
    public static final String SHARED_ELEMENT_NAME = "SHARED_ELEMENT_NAME";
    private static final String SCHEME_FILE = "file";

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
                float downRawX=0, downRawY=0;
                float dX=0, dY=0;

                ImageButton img = (ImageButton)v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // 按住事件发生后执行代码的区域
                        // mReconition.cancelRecognize();
                        img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice2));
                        Log.d(TAG, "upup31312 : "+System.currentTimeMillis());
                        mReconition.startRecognize();
                        // han.sendEmptyMessageDelayed(0,1000);

                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        dX = v.getX() - downRawX;
                        dY = v.getY() - downRawY;

                        break;
                        // 开始识别
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // 移动事件发生后执行代码的区域
                        int viewWidth = v.getWidth();
                        int viewHeight = v.getHeight();

                        View viewParent = (View)v.getParent();
                        int parentWidth = viewParent.getWidth();
                        int parentHeight = viewParent.getHeight();

                        float newX = event.getRawX() + dX;
                        newX = Math.max(0, newX);                                 // Don't allow the FAB past the left hand side of the parent
                        newX = Math.min(parentWidth - viewWidth, newX);           // Don't allow the FAB past the right hand side of the parent

                        float newY = event.getRawY() + dY;
                        newY = Math.max(0, newY);                                 // Don't allow the FAB past the top of the parent
                        newY = Math.min(parentHeight - viewHeight, newY);         // Don't allow the FAB past the bottom of the parent

                        v.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start();

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        // 松开事件发生后执行代码的区域
                        img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                        Message message = new Message();
                        message.what=0;
                        han.sendMessageDelayed(message, 800);
                        //float upRawX = event.getRawX();
                        //float upRawY = event.getRawY();
                        //float upDX = upRawX - downRawX;
                        //float upDY = upRawY - downRawY;

                        break;
                    }
                    default:
                        break;
                }
                return false;// 设置成false,使得btn的onEvent方法能够被调用
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
