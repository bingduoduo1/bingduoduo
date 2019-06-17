package com.termux.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bingduoduo.editor.view.MainActivity;
import com.github.clans.fab.FloatingActionButton;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.voicedemo.SpeechRecognitionIat;
import com.termux.R;
import com.termux.terminal.EmulatorDebug;
import com.termux.terminal.TerminalColors;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSession.SessionChangedCallback;
import com.termux.terminal.TextStyle;
import com.termux.view.TerminalView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.application.autotrain.wrapper.BaseWarpperInterface;
import model.application.autotrain.wrapper.PytorchTrainWrapper;

import static android.content.ContentValues.TAG;

/**
 * A terminal emulator activity.
 * <p/>
 * See
 * <ul>
 * <li>http://www.mongrel-phones.com.au/default/how_to_make_a_local_service_and_bind_to_it_in_android</li>
 * <li>https://code.google.com/p/android/issues/detail?id=6426</li>
 * </ul>
 * about memory leaks.
 */

/**
 * @cn-annotator butub
 * important!
 * 这个活动控制terminal 模拟器
 */
public final class TermuxActivity extends AppCompatActivity implements ServiceConnection {
    
    // 长按视图弹出上下文菜单
    private static final int CONTEXTMENU_SELECT_URL_ID = 0; // select url id
    private static final int CONTEXTMENU_SHARE_TRANSCRIPT_ID = 1; // share transcript id
    private static final int CONTEXTMENU_PASTE_ID = 3; // paste id
    private static final int CONTEXTMENU_KILL_PROCESS_ID = 4; // kill process id
    private static final int CONTEXTMENU_RESET_TERMINAL_ID = 5; // reset terminal id
    private static final int CONTEXTMENU_STYLING_ID = 6; // styling id
    private static final int CONTEXTMENU_HELP_ID = 8; // help id
    private static final int CONTEXTMENU_TOGGLE_KEEP_SCREEN_ON = 9; // toggle keep screen on
    
    private static final int MAX_SESSIONS = 8; // max sessions
    
    private static final int REQUESTCODE_PERMISSION_STORAGE = 1234;// request code permission storage
    
    private static final String RELOAD_STYLE_ACTION = "com.termux.app.reload_style";// reload style action
    
    private static StringBuffer mret = new StringBuffer();
    final SoundPool mbellsoundpool = new SoundPool.Builder().setMaxStreams(1)
            .setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build())
            .build();
    /**
     * The main view of the activity showing the terminal. Initialized in onCreate().
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    TerminalView terminalView;
    ExtraKeysView extraKeysView;
    /*
     * The connection to the {@link TermuxService}. Requested in {@link #onCreate(Bundle)} with a call to
     * {@link #bindService(Intent, ServiceConnection, int)}, and obtained and stored in
     * {@link #onServiceConnected(ComponentName, IBinder)}.
     */
    TermuxPreferences msettings;
    /**
     * 连接Termux Service。
     */
    TermuxService mtermservice;
    /**
     * Initialized in {@link #onServiceConnected(ComponentName, IBinder)}.
     */
    ArrayAdapter<TerminalSession> mlistviewadapter;
    /**
     * The last toast shown, used cancel current toast before showing new in {@link #showToast(String, boolean)}.
     */
    Toast mlasttoast;
    /**
     * If between onResume() and onStop(). Note that only one session is in the foreground of the terminal view at the
     * time, so if the session causing a change is not in the foreground it should probably be treated as background.
     */
    boolean misvisible;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (misvisible) { // 如果在前台可视
                String whatToReload = intent.getStringExtra(RELOAD_STYLE_ACTION);
                if ("storage".equals(whatToReload)) {
                    if (ensureStoragePermissionGranted())
                    {
                        TermuxInstaller.setupStorageSymlinks(TermuxActivity.this);
                    }
                    return;
                }
                checkForFontAndColors();
                msettings.reloadFromProperties(TermuxActivity.this);// 加载配置属性
                if (extraKeysView != null) {
                    extraKeysView.reload(msettings.mextrakeys, ExtraKeysView.defaultCharDisplay);
                }
            }
        }
    };
    int mbellsoundid;
    private SpeechRecognitionIat recognitionIat;
    private BaseWarpperInterface mautotrainwrapper = PytorchTrainWrapper.createWrapper();
    private SpeechCallMode mspeechmode = SpeechCallMode.NORMAL;
    private Handler han = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            mret.append(recognitionIat.getAction());
            Log.d(TAG, "handleMessage: " + mret);
            doAction();
            recognitionIat.stopRecognize();
            
        }
        
        private void doAction() {
            TerminalSession ts = getCurrentTermSession();
            if (mret.length() == 0) {
                // pass 无Action
                // or \n?
                ts.write("\n");
            } else {
                
                switch (mret.toString()) {
                    case "backspace":// 参考 TermuxView:row:682
                        ts.writeCodePoint(false, 127);
                        break;
                    default:
                        ts.write(mret.toString());
                        
                }
                mret.setLength(0);
                recognitionIat.stopRecognize();
                terminalView.onScreenUpdated();
            }
        }
    };
    
    // 冰多多
    private Handler handlerAutoTrain = new HandlerAutoTrain();
    
    static LinkedHashSet<CharSequence> extractUrls(String text) {
        // Pattern for recognizing a URL, based off RFC 3986
        // http://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
        final Pattern urlPattern = Pattern.compile(
                "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)" + "(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        LinkedHashSet<CharSequence> urlSet = new LinkedHashSet<>();
        Matcher matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String url = text.substring(matchStart, matchEnd);
            urlSet.add(url);
        }
        return urlSet;
    }
    
    void checkForFontAndColors() {
        try {
            @SuppressLint("SdCardPath")
            File fontFile;
            fontFile = new File("/data/data/com.bingduoduo/files/home/.termux/font.ttf");// defalut is
            @SuppressLint("SdCardPath")
            File colorsFile = new File("/data/data/com.bingduoduo/files/home/.termux/colors.properties");// default is

            final Properties props = new Properties();
            if (colorsFile.isFile()) {
                try (InputStream in = new FileInputStream(colorsFile)) {
                    props.load(in);
                }
            }
            
            TerminalColors.COLOR_SCHEME.updateWith(props);
            TerminalSession session = getCurrentTermSession();
            if (session != null && session.getEmulator() != null) {
                session.getEmulator().mcolors.reset();
            }
            updateBackgroundColor();
            
            final Typeface newTypeface =
                    (fontFile.exists() && fontFile.length() > 0) ? Typeface.createFromFile(fontFile)
                            : Typeface.MONOSPACE;
            terminalView.setTypeface(newTypeface);
        } catch (Exception e) {
            Log.e(EmulatorDebug.LOG_TAG, "Error in checkForFontAndColors()", e);
        }
    }
    
    void updateBackgroundColor() {
        TerminalSession session = getCurrentTermSession();
        if (session != null && session.getEmulator() != null) {
            getWindow().getDecorView()
                    .setBackgroundColor(session.getEmulator().mcolors.mcurrentcolors[TextStyle.COLOR_INDEX_BACKGROUND]);
        }
    }
    
    /**
     * For processes to access shared internal storage (/sdcard) we need this permission.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public boolean ensureStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        REQUESTCODE_PERMISSION_STORAGE);
                return false;
            }
        } else {
            // Always granted before Android 6.0.
            return true;
        }
    }
    
    @Override
    public void onCreate(Bundle bundle) {
        // todo
        super.onCreate(bundle);
        
        msettings = new TermuxPreferences(this);
        
        setContentView(R.layout.drawer_layout);
        terminalView = findViewById(R.id.terminal_view);// 这个view 在drawer_layout 中
        terminalView.setOnKeyListener(new TermuxViewClient(this));// TerminalView,设定mClient
        
        terminalView.setTextSize(msettings.getFontSize());
        terminalView.setKeepScreenOn(msettings.isScreenAlwaysOn());
        terminalView.requestFocus();
        
        // 默认设置为日间模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // 禁用输入法
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        // WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        
        // 分页
        final ViewPager viewPager = findViewById(R.id.viewpager);// androidx.viewpager.widget.ViewPager
        if (msettings.mshowextrakeys)
        {
            viewPager.setVisibility(View.VISIBLE);
        }
        
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.height = layoutParams.height * msettings.mextrakeys.length;
        viewPager.setLayoutParams(layoutParams);
        
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }
            
            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
            
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup collection, int position) {
                // 实例化, positon
                LayoutInflater inflater = LayoutInflater.from(TermuxActivity.this); // 布局填充
                View layout;
                if (position == 0) {
                    layout = extraKeysView =
                            (ExtraKeysView) inflater.inflate(R.layout.extra_keys_main, collection, false);
                    extraKeysView.reload(msettings.mextrakeys, ExtraKeysView.defaultCharDisplay);
                } else {
                    layout = inflater.inflate(R.layout.extra_keys_right, collection, false);
                    final EditText editText = layout.findViewById(R.id.text_input);// text_input,输入的缓存,这是extra_key右边的那一条缓存用的text
                    editText.setOnEditorActionListener((v, actionId, event) -> {
                        TerminalSession session = getCurrentTermSession();
                        if (session != null) {
                            if (session.isRunning()) {
                                String textToSend = editText.getText().toString();
                                if (textToSend.length() == 0)
                                {
                                    textToSend = "\r";
                                }
                                session.write(textToSend);// 写入 !!!KEY
                            } else {
                                removeFinishedSession(session);// remove没在running的currentTermSession
                            }
                            editText.setText("");// 刷新
                        }
                        return true;
                    });
                }
                collection.addView(layout);// ViewGroup collection
                return layout;
            }
            
            @Override
            public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
                collection.removeView((View) view);
            }
        });// end view-adapter
        
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    terminalView.requestFocus();
                } else {
                    final EditText editText = viewPager.findViewById(R.id.text_input);
                    if (editText != null)
                    {
                        editText.requestFocus();
                    }
                }
            }
        });
        
        View newSessionButton = findViewById(R.id.new_session_button);
        newSessionButton.setOnClickListener(v -> addNewSession(false, null));
        newSessionButton.setOnLongClickListener(v -> {
            // 长按设定Session名字并创建
            DialogUtils.textInput(TermuxActivity.this, R.string.session_new_named_title, null,
                    R.string.session_new_named_positive_button, text -> addNewSession(false, text),
                    R.string.new_session_failsafe, text -> addNewSession(true, text), -1, null, null);
            return true;
        });
        
        // todo 这个IMM 可能需要被替换 或者 关闭
        
        findViewById(R.id.toggle_keyboard_button).setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            getDrawer().closeDrawers();
        });
        
        findViewById(R.id.toggle_keyboard_button).setOnLongClickListener(v -> {
            toggleShowExtraKeys();
            return true;
        });
        
        registerForContextMenu(terminalView);// 设置上下文菜单
        
        Intent serviceIntent = new Intent(this, TermuxService.class);// 启动服务
        // Start the service and make it run regardless of who is bound to it:
        // 启动服务，不论绑定什么对象
        startService(serviceIntent);
        if (!bindService(serviceIntent, this, 0))
        {
            throw new RuntimeException("bindService() failed");
        }
        
        checkForFontAndColors();
        
        mbellsoundid = mbellsoundpool.load(this, R.raw.bell, 1);
        
        // todo main
        SpeechUtility.createUtility(this, "appid=5c9cc920");// appid需要和sdk的id相匹配
        this.requestPermissions();
        // if(null == mRecognizer){
        // Log.e("TermuxActivity", "onCreate: Null!!!");
        // }
        // mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        
        // this.initSpeechRecognizer();
        // this.SetParam();
        // mAsr = SpeechRecognizer.createRecognizer(TermuxActivity.this, mInitListener);
        // mCloudGrammar = FucUtil.readFile(this,"grammar_sample.abnf","utf-8");
        
        // mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        
        recognitionIat = new SpeechRecognitionIat(TermuxActivity.this, "userwords");
        
        FloatingActionButton btnVoice = findViewById(R.id.menu_fab_voice);
        // Button btn_voice = (Button) findViewById(R.id.btn_voice);
        
        FloatingActionButton fabSwitch = findViewById(R.id.menu_fab_switch);
        FloatingActionButton fabAutoTrain = findViewById(R.id.menu_fab_auto_train);
        
        btnVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageButton img = (ImageButton) v;
                switch (mspeechmode) {
                    case NORMAL: {
                        Log.d(TAG, "onTouch: normal mode");
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                // mReconition.cancelRecognize();
                                Log.d(TAG, "upup31312 : " + System.currentTimeMillis());
                                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice2));
                                recognitionIat.startRecognize();
                                break;
                                // 开始识别
                            }
                            case MotionEvent.ACTION_MOVE: {
                                // 移动事件发生后执行代码的区域
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                                Message message = new Message();
                                message.what = 0;
                                han.sendMessageDelayed(message, 800);
                                break;
                            }
                            default:
                                break;
                        }
                        return false;// 设置成false的话, btn对象自己的onEvent方法就能够被调用,触感也就有了
                    }
                    case AUTO_TRAIN: {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice2));
                                recognitionIat.startRecognize();
                                break;
                            }
                            case MotionEvent.ACTION_MOVE: {
                                // 移动事件发生后执行代码的区域
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                                Message message = new Message();
                                message.what = 0;
                                handlerAutoTrain.sendMessageDelayed(message, 800);
                                break;
                            }
                            default:
                                break;
                        }
                        return false;
                    }
                    default: {
                        Log.e(TAG, "WTF ");
                        return false;
                    }
                }
            }
        });
        
        fabSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TermuxActivity.this, MainActivity.class);
                // intent.putExtra("isNightMode",mIsNightMode);
                startActivity(intent);
            }
        });
        
        fabAutoTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                if (mspeechmode == SpeechCallMode.AUTO_TRAIN) {
                    mspeechmode = SpeechCallMode.NORMAL;
                    img.setImageDrawable(getResources().getDrawable(R.drawable.auto_train_em_gray));
                } else {
                    mspeechmode = SpeechCallMode.AUTO_TRAIN;
                    img.setImageDrawable(getResources().getDrawable(R.drawable.auto_train_gray));
                }
                
            }
        });
        
        // Log.d(TAG, "onCreate: " + getFilesDir()+"---------------------------");
        
        // Create an object of our Custom Gesture Detector Class
        // CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        // Create a GestureDetector
        // mGestureDetector = new GestureDetector(this, customGestureDetector);
        // Attach listeners that'll be called for double-tap and related gestures
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    // end onCreate
    
    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS },
                            0x0010);
                }
                
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION }, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void toggleShowExtraKeys() {
        final ViewPager viewPager = findViewById(R.id.viewpager);
        final boolean showNow = msettings.toggleShowExtraKeys(TermuxActivity.this);
        viewPager.setVisibility(showNow ? View.VISIBLE : View.GONE);
        if (showNow && viewPager.getCurrentItem() == 1) {
            // Focus the text input view if just revealed.
            findViewById(R.id.text_input).requestFocus();
        }
    }
    
    /**
     * Part of the {@link ServiceConnection} interface. The service is bound with
     * {@link #bindService(Intent, ServiceConnection, int)} in {@link #onCreate(Bundle)} which will cause a call to this
     * callback method.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        // todo
        mtermservice = ((TermuxService.LocalBinder) service).service;
        
        mtermservice.sessionChangedCallback = new SessionChangedCallback() {
            @Override
            public void onTextChanged(TerminalSession changedSession) {
                if (!misvisible)
                {
                    return;
                }
                if (getCurrentTermSession() == changedSession)
                {
                    terminalView.onScreenUpdated();
                }
            }
            
            @Override
            public void onTitleChanged(TerminalSession updatedSession) {
                if (!misvisible)
                {
                    return;
                }
                if (updatedSession != getCurrentTermSession()) {
                    // Only show toast for other sessions than the current one, since the user
                    // probably consciously caused the title change to change in the current session
                    // and don't want an annoying toast for that.
                    showToast(toToastTitle(updatedSession), false);
                }
                mlistviewadapter.notifyDataSetChanged();
            }
            
            @Override
            public void onSessionFinished(final TerminalSession finishedSession) {
                if (mtermservice.mwantstostop) {
                    // The service wants to stop as soon as possible.
                    finish();
                    return;
                }
                if (misvisible && finishedSession != getCurrentTermSession()) {
                    // Show toast for non-current sessions that exit.
                    int indexOfSession = mtermservice.getSessions().indexOf(finishedSession);
                    // Verify that session was not removed before we got told about it finishing:
                    if (indexOfSession >= 0)
                    {
                        showToast(toToastTitle(finishedSession) + " - exited", true);
                    }
                }
                
                if (mtermservice.getSessions().size() > 1) {
                    removeFinishedSession(finishedSession);
                }
                
                mlistviewadapter.notifyDataSetChanged();
            }
            
            @Override
            public void onClipboardText(TerminalSession session, String text) {
                if (!misvisible)
                {
                    return;
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(new ClipData(null, new String[] { "text/plain" }, new ClipData.Item(text)));
            }
            
            @Override
            public void onBell(TerminalSession session) {
                if (!misvisible)
                {
                    return;
                }
                
                switch (msettings.mbellbehaviour) {
                    case TermuxPreferences.BELL_BEEP:
                        mbellsoundpool.play(mbellsoundid, 1.f, 1.f, 1, 0, 1.f);
                        break;
                    case TermuxPreferences.BELL_VIBRATE:
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
                        break;
                    case TermuxPreferences.BELL_IGNORE:
                        // Ignore the bell character.
                        break;
                    default:
                        break;
                }
                
            }
            
            @Override
            public void onColorsChanged(TerminalSession changedSession) {
                if (getCurrentTermSession() == changedSession)
                {
                    updateBackgroundColor();
                }
            }
        };
        
        ListView listView = findViewById(R.id.left_drawer_list);
        
        // 管理所有session的视图
        mlistviewadapter = new ArrayAdapter<TerminalSession>(getApplicationContext(), R.layout.line_in_drawer,
                mtermservice.getSessions()) {
            final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            final StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
            
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // 配置Ｌist样式
                View row = convertView;
                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(R.layout.line_in_drawer, parent, false);
                }
                
                TerminalSession sessionAtRow = getItem(position);
                boolean sessionRunning;
                sessionRunning = sessionAtRow.isRunning();
                
                TextView firstLineView = row.findViewById(R.id.row_line);
                
                String name = sessionAtRow.msessionname;
                String sessionTitle = sessionAtRow.getTitle();
                
                String numberPart = "[" + (position + 1) + "] ";
                String sessionNamePart = (TextUtils.isEmpty(name) ? "" : name);
                String sessionTitlePart = (TextUtils.isEmpty(sessionTitle) ? ""
                        : ((sessionNamePart.isEmpty() ? "" : "\n") + sessionTitle));
                
                String text = numberPart + sessionNamePart + sessionTitlePart;
                SpannableString styledText = new SpannableString(text);
                styledText.setSpan(boldSpan, 0, numberPart.length() + sessionNamePart.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styledText.setSpan(italicSpan, numberPart.length() + sessionNamePart.length(), text.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                firstLineView.setText(styledText);
                
                if (sessionRunning) {
                    firstLineView.setPaintFlags(firstLineView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    firstLineView.setPaintFlags(firstLineView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                int color = sessionRunning || sessionAtRow.getExitStatus() == 0 ? Color.BLACK : Color.RED;
                firstLineView.setTextColor(color);
                return row;
            }
        };// end mlistviewadapter
        // 配置ListAdapter上的动作
        listView.setAdapter(mlistviewadapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 切换Session
            TerminalSession clickedSession = mlistviewadapter.getItem(position);
            switchToSession(clickedSession);
            getDrawer().closeDrawers();
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            // 重命名Session
            final TerminalSession selectedSession = mlistviewadapter.getItem(position);
            renameSession(selectedSession);
            return true;
        });
        
        if (mtermservice.getSessions().isEmpty()) {
            // 如果当前的Ｓession列表是空的就添加新的Session
            if (misvisible) {
                TermuxInstaller.setupIfNeeded(TermuxActivity.this, () -> {
                    if (mtermservice == null)
                    {
                        return; // Activity might have been destroyed.
                    }
                    try {
                        clearTemporaryDirectory();// todo-debug
                        addNewSession(false, null);
                    } catch (WindowManager.BadTokenException e) {
                        // Activity finished - ignore.
                    }
                });
            } else {
                // The service connected while not in foreground - just bail out.
                finish();
            }
        } else {
            Intent i = getIntent();
            if (i != null && Intent.ACTION_RUN.equals(i.getAction())) {
                // Android 7.1 app shortcut from res/xml/shortcuts.xml.
                clearTemporaryDirectory();
                addNewSession(false, null);
            } else {
                switchToSession(getStoredCurrentSessionOrLast());
            }
        }
    }
    // end onServiceConnected
    
    public void switchToSession(boolean forward) {
        TerminalSession currentSession = getCurrentTermSession();
        int index = mtermservice.getSessions().indexOf(currentSession);
        if (forward) {
            if (++index >= mtermservice.getSessions().size())
            {
                index = 0;
            }
        } else {
            if (--index < 0)
            {
                index = mtermservice.getSessions().size() - 1;
            }
        }
        switchToSession(mtermservice.getSessions().get(index));
    }
    
    /**
     * Try switching to session and note about it, but do nothing if already displaying the session.
     */
    void switchToSession(TerminalSession session) {
        if (terminalView.attachSession(session)) {
            noteSessionInfo();
            updateBackgroundColor();
        }
    }
    
    /**
     * 重写ServiceConnecction接口的方法onServiceConnected, onCreate中绑定服务后会启动这个回调方法
     */
    
    @SuppressLint("InflateParams")
    void renameSession(final TerminalSession sessionToRename) {
        DialogUtils.textInput(this, R.string.session_rename_title, sessionToRename.msessionname,
                R.string.session_rename_positive_button, text -> {
                sessionToRename.msessionname = text;
                mlistviewadapter.notifyDataSetChanged();
            }, -1, null, -1, null, null);
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        // Respect being stopped from the TermuxService notification action.
        finish();
    }
    
    @Nullable
    TerminalSession getCurrentTermSession() {
        return terminalView.getCurrentSession();
    }
    
    @Override
    public void onStart() {
        // todo
        super.onStart();
        misvisible = true;
        
        if (mtermservice != null) {
            // The service has connected, but data may have changed since we were last in the foreground.
            switchToSession(getStoredCurrentSessionOrLast());
            mlistviewadapter.notifyDataSetChanged();
        }
        registerReceiver(broadcastReceiver, new IntentFilter(RELOAD_STYLE_ACTION));
        
        // The current terminal session may have changed while being away, force
        // a refresh of the displayed terminal:
        terminalView.onScreenUpdated();// 可以在onStart中添加一些需要监听的动作
        if (terminalView != null && getCurrentTermSession() != null) {
            // 没有用
            TerminalSession ss = getCurrentTermSession();
            // Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        misvisible = false;
        TerminalSession currentSession = getCurrentTermSession();
        if (currentSession != null)
        {
            TermuxPreferences.storeCurrentSession(this, currentSession);
        }
        unregisterReceiver(broadcastReceiver);
        getDrawer().closeDrawers();
    }
    
    @Override
    public void onBackPressed() {
        if (getDrawer().isDrawerOpen(Gravity.LEFT)) {
            getDrawer().closeDrawers();
        } else {
            finish();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mtermservice != null) {
            // Do not leave service with references to activity.
            mtermservice.sessionChangedCallback = null;
            mtermservice = null;
        }
        unbindService(this);
    }
    
    DrawerLayout getDrawer() {
        // 左侧滑动栏
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }
    
    void addNewSession(boolean failSafe, String sessionName) {
        
        if (mtermservice.getSessions().size() >= MAX_SESSIONS) {
            new AlertDialog.Builder(this).setTitle(R.string.max_terminals_reached_title)
                    .setMessage(R.string.max_terminals_reached_message).setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            String executablePath = (failSafe ? "/system/bin/sh" : null);
            Log.d(TAG, "addNewSession: " + (executablePath == null ? "null" : executablePath)
                    + "*********************************");
            TerminalSession newSession = mtermservice.createTermSession(executablePath, null, null, failSafe);
            if (sessionName != null) {
                Log.d(TAG, "addNewSession: name:" + sessionName + "*****************************");
                newSession.msessionname = sessionName;
            }
            switchToSession(newSession);
            getDrawer().closeDrawers();
        }
    }
    
    String toToastTitle(TerminalSession session) {
        final int indexOfSession = mtermservice.getSessions().indexOf(session);
        StringBuilder toastTitle = new StringBuilder("[" + (indexOfSession + 1) + "]");
        if (!TextUtils.isEmpty(session.msessionname)) {
            toastTitle.append(" ").append(session.msessionname);
        }
        String title = session.getTitle();
        if (!TextUtils.isEmpty(title)) {
            // Space to "[${NR}] or newline after session name:
            toastTitle.append(session.msessionname == null ? " " : "\n");
            toastTitle.append(title);
        }
        return toastTitle.toString();
    }
    
    void noteSessionInfo() {
        // 切换的时候显示Session的信息
        if (!misvisible) {
            return;
        }
        TerminalSession session = getCurrentTermSession();
        final int indexOfSession = mtermservice.getSessions().indexOf(session);
        showToast(toToastTitle(session), false);
        mlistviewadapter.notifyDataSetChanged();
        final ListView lv = findViewById(R.id.left_drawer_list);
        lv.setItemChecked(indexOfSession, true);
        lv.smoothScrollToPosition(indexOfSession);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        // 上下文菜单，长按触发，Override
        TerminalSession currentSession = getCurrentTermSession();
        if (currentSession == null) {
            return;
        }
        
        menu.add(Menu.NONE, CONTEXTMENU_SELECT_URL_ID, Menu.NONE, R.string.select_url);
        menu.add(Menu.NONE, CONTEXTMENU_SHARE_TRANSCRIPT_ID, Menu.NONE, R.string.select_all_and_share);
        menu.add(Menu.NONE, CONTEXTMENU_RESET_TERMINAL_ID, Menu.NONE, R.string.reset_terminal);
        menu.add(Menu.NONE, CONTEXTMENU_KILL_PROCESS_ID, Menu.NONE,
                getResources().getString(R.string.kill_process, getCurrentTermSession().getPid()))
                .setEnabled(currentSession.isRunning());
        menu.add(Menu.NONE, CONTEXTMENU_STYLING_ID, Menu.NONE, R.string.style_terminal);
        menu.add(Menu.NONE, CONTEXTMENU_TOGGLE_KEEP_SCREEN_ON, Menu.NONE, R.string.toggle_keep_screen_on)
                .setCheckable(true).setChecked(msettings.isScreenAlwaysOn());
        menu.add(Menu.NONE, CONTEXTMENU_HELP_ID, Menu.NONE, R.string.help);
    }
    
    /**
     * Hook system menu to show context menu instead.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        terminalView.showContextMenu();
        return false;
    }
    
    void showUrlSelection() {
        String text = getCurrentTermSession().getEmulator().getScreen().getTranscriptText();
        LinkedHashSet<CharSequence> urlSet = extractUrls(text);
        if (urlSet.isEmpty()) {
            new AlertDialog.Builder(this).setMessage(R.string.select_url_no_found).show();
            return;
        }
        
        final CharSequence[] urls = urlSet.toArray(new CharSequence[0]);
        Collections.reverse(Arrays.asList(urls)); // Latest first.
        
        // Click to copy url to clipboard:
        final AlertDialog dialog = new AlertDialog.Builder(TermuxActivity.this).setItems(urls, (di, which) -> {
            String url = (String) urls[which];
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(new ClipData(null, new String[] { "text/plain" }, new ClipData.Item(url)));
            Toast.makeText(TermuxActivity.this, R.string.select_url_copied_to_clipboard, Toast.LENGTH_LONG).show();
        }).setTitle(R.string.select_url_dialog_title).create();
        
        // Long press to open URL:
        dialog.setOnShowListener(di -> {
            ListView lv = dialog.getListView(); // this is a ListView with your "buds" in it
            lv.setOnItemLongClickListener((parent, view, position, id) -> {
                dialog.dismiss();
                String url = (String) urls[position];
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(i, null);
                } catch (ActivityNotFoundException e) {
                    // If no applications match, Android displays a system message.
                    startActivity(Intent.createChooser(i, null));
                }
                return true;
            });
        });
        
        dialog.show();
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 上下文选项对应的具体动作
        TerminalSession session = getCurrentTermSession();
        
        switch (item.getItemId()) {
            case CONTEXTMENU_SELECT_URL_ID:
                showUrlSelection();
                return true;
            case CONTEXTMENU_SHARE_TRANSCRIPT_ID:
                if (session != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, session.getEmulator().getScreen().getTranscriptText().trim());
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_transcript_title));
                    startActivity(Intent.createChooser(intent, getString(R.string.share_transcript_chooser_title)));
                }
                return true;
            case CONTEXTMENU_PASTE_ID:
                doPaste();
                return true;
            case CONTEXTMENU_KILL_PROCESS_ID:
                final AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage(R.string.confirm_kill_process);
                b.setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    dialog.dismiss();
                    getCurrentTermSession().finishIfRunning();
                });
                b.setNegativeButton(android.R.string.no, null);
                b.show();
                return true;
            case CONTEXTMENU_RESET_TERMINAL_ID: {
                if (session != null) {
                    session.reset();
                    showToast(getResources().getString(R.string.reset_toast_notification), true);
                }
                return true;
            }
            case CONTEXTMENU_STYLING_ID: {
                Intent stylingIntent = new Intent();
                stylingIntent.setClassName("com.termux.styling", "com.termux.styling.TermuxStyleActivity");
                try {
                    startActivity(stylingIntent);
                } catch (ActivityNotFoundException | IllegalArgumentException e) {
                    // The startActivity() call is not documented to throw IllegalArgumentException.
                    // However, crash reporting shows that it sometimes does, so catch it here.
                    new AlertDialog.Builder(this).setMessage(R.string.styling_not_installed).setPositiveButton(
                            R.string.styling_install,
                        (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=com.termux.styling"))))
                            .setNegativeButton(android.R.string.cancel, null).show();
                }
                return true;
            }
            case CONTEXTMENU_HELP_ID:
                startActivity(new Intent(this, TermuxHelpActivity.class));
                return true;
            case CONTEXTMENU_TOGGLE_KEEP_SCREEN_ON: {
                if (terminalView.getKeepScreenOn()) {
                    terminalView.setKeepScreenOn(false);
                    msettings.setScreenAlwaysOn(this, false);
                } else {
                    terminalView.setKeepScreenOn(true);
                    msettings.setScreenAlwaysOn(this, true);
                }
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUESTCODE_PERMISSION_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            TermuxInstaller.setupStorageSymlinks(this);
        }
    }
    
    void changeFontSize(boolean increase) {
        msettings.changeFontSize(this, increase);
        terminalView.setTextSize(msettings.getFontSize());
    }
    
    void doPaste() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null) {
            return;
        }
        CharSequence paste = clipData.getItemAt(0).coerceToText(this);
        if (!TextUtils.isEmpty(paste)) {
            getCurrentTermSession().getEmulator().paste(paste.toString());
        }
    }
    
    /**
     * The current session as stored or the last one if that does not exist.
     */
    public TerminalSession getStoredCurrentSessionOrLast() {
        TerminalSession stored = TermuxPreferences.getCurrentSession(this);
        if (stored != null) {
            return stored;
        }
        List<TerminalSession> sessions = mtermservice.getSessions();
        return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
    }
    
    /**
     * Show a toast and dismiss the last one if still visible.
     */
    void showToast(String text, boolean longDuration) {
        if (mlasttoast != null) {
            mlasttoast.cancel();
        }
        mlasttoast = Toast.makeText(TermuxActivity.this, text, longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        mlasttoast.setGravity(Gravity.TOP, 0, 0);
        mlasttoast.show();
    }
    
    public void removeFinishedSession(TerminalSession finishedSession) {
        // Return pressed with finished session - remove it.
        TermuxService service = mtermservice;
        
        int index = service.removeTermSession(finishedSession);
        mlistviewadapter.notifyDataSetChanged();
        if (mtermservice.getSessions().isEmpty()) {
            // There are no sessions to show, so finish the activity.
            finish();
        } else {
            if (index >= service.getSessions().size()) {
                index = service.getSessions().size() - 1;
            }
            switchToSession(service.getSessions().get(index));
        }
    }
    
    private void clearTemporaryDirectory() {
        // 清空tmp目录，删除再创建
        if (mtermservice.getSessions().size() == 0 && !mtermservice.isWakelockEnabled()) {
            File termuxTmpDir = new File(TermuxService.PREFIX_PATH + "/tmp");
            if (termuxTmpDir.exists()) {
                try {
                    TermuxInstaller.deleteFolder(termuxTmpDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                termuxTmpDir.mkdirs();
            }
        }
    }
    
    // 冰多多
    // private GestureDetector mGestureDetector;
    private enum SpeechCallMode {
        NORMAL, AUTO_TRAIN
    }
    
    private class HandlerAutoTrain extends Handler {
        private boolean initFlag = true;
        private final String mkeyoptim = "optim";
        private final String mkeylr = "learning_rate";
        
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "get action handle message");
            mret.append(recognitionIat.getAction());
            if (initFlag) {
                mautotrainwrapper.init();
                initFlag = false;
            }
            Log.d(TAG, "get action handle message:" + mret.toString());
            recognitionIat.stopRecognize();
            doAction();
        }
        
        private void doAction() {
            Log.d(TAG, "do action auto train in");
            TerminalSession ts = getCurrentTermSession();
            if (mret.length() == 0) {
                ts.write("\n");
            } else {
                String actionContent;
                Log.d(TAG, "mret str" + mret.toString());
                switch (mret.toString()) {
                    
                    case "backspace":
                        ts.writeCodePoint(false, 127);
                        break;
                    case "show": {
                        actionContent = mautotrainwrapper.getShowConfigInfo();
                        ts.write(actionContent);
                        break;
                    }
                    case "check": {
                        actionContent = mautotrainwrapper.checkConfig();
                        // ts.write("apt-get update\n");
                        // ts.write("apt install openssh\nY\nY\n\n");
                        // ts.write(("apt install sshpass\nY\nY\n"));
                        ts.write(actionContent);
                        break;
                    }
                    case "send": {
                        String cmd = mautotrainwrapper.getSendConfigCmd();
                        ts.write(cmd);
                        break;
                    }
                    case "receive": {
                        actionContent = mautotrainwrapper.getReceiveConfigCmd();
                        ts.write(actionContent);
                        break;
                    }
                    default: {
                        String content = mret.toString();
                        // Log.d(TAG, "modify:default"+content);
                        if (content.startsWith(mkeyoptim)) {
                            int loc = content.indexOf("=");
                            String value = content.substring(loc + 1);
                            if (value.equals("invalid")) {
                                // todo
                            } else {
                                String result = mautotrainwrapper.update("optim_algorithm", value);
                                Log.d(TAG, "modify1:" + result);
                            }
                        } else if (content.startsWith(mkeylr)) {
                            int loc = content.indexOf("=");
                            String value = content.substring(loc + 1);
                            if (value.equals("invalid")) {
                                // todo
                            } else {
                                String result = mautotrainwrapper.update("learning_rate", value);
                                Log.d(TAG, "modify2:" + result);
                            }
                        } else {
                            ts.write(mret.toString());
                        }
                    }
                    
                }
                mret.setLength(0);
                recognitionIat.stopRecognize();
                terminalView.onScreenUpdated();
            }
        }
    }
    
    // bingduoduo custom gesture detector, useless tmp
    class CustomGestureDetector implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            // mGestureText.setText("onDown");
            return true;
        }
        
        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress");
            // mGestureText.setText("onShowPress");
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp");
            // mGestureText.setText("onSingleTapUp");
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll");
            // mGestureText.setText("onScroll");
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            // mGestureText.setText("onLongPress");
            Log.d(TAG, "onLongPress");
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // mGestureText.setText("onFling");
            // 判断竖直方向移动的大小
            Log.d(TAG, "onFling:迅速滑动，并松开");
            if (Math.abs(e1.getRawY() - e2.getRawY()) > 100) {
                // Toast.makeText(getApplicationContext(), "动作不合法", 0).show();
                return true;
            }
            if (Math.abs(velocityX) < 150) {
                // Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
                return true;
            }
            
            if ((e1.getRawX() - e2.getRawX()) > 200) {
                // 表示 向右滑动表示下一页
                // 显示下一页
                Log.d("MainActivity", "onFling: 右$$$$$$$$$$$$$$$$$$$");
                Intent intent = new Intent(TermuxActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            
            if ((e2.getRawX() - e1.getRawX()) > 200) { // 向左滑动 表示 上一页
                // 显示上一页
                Log.d("MainActivity", "onFling: 左$$$$$$$$$$$$$$$$$$$");
                return true;// 消费掉当前事件 不让当前事件继续向下传递
            }
            return true;
        }
    }
}
