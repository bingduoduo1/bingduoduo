package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.termux.terminal.TerminalSession;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @cn-annotator butub
 * 处理配置文件,并且配置拓展键盘和字体大小
 */

final class TermuxPreferences {
    
    @IntDef({ BELL_VIBRATE, BELL_BEEP, BELL_IGNORE })
    @Retention(RetentionPolicy.SOURCE)
    @interface AsciiBellBehaviour {
    }
    
    static final class KeyboardShortcut {
        
        KeyboardShortcut(int codePoint, int shortcutAction) {
            this.codePoint = codePoint;
            this.shortcutAction = shortcutAction;
        }
        
        final int codePoint;
        final int shortcutAction;
    }
    
    static final int SHORTCUT_ACTION_CREATE_SESSION = 1; // 快捷键 ->打开新的回话
    static final int SHORTCUT_ACTION_NEXT_SESSION = 2; // ->下一个会话
    static final int SHORTCUT_ACTION_PREVIOUS_SESSION = 3;// -> 上一个会话
    static final int SHORTCUT_ACTION_RENAME_SESSION = 4; // 重命名Session
    
    static final int BELL_VIBRATE = 1;
    static final int BELL_BEEP = 2;
    static final int BELL_IGNORE = 3;
    
    private final int minFontsize;
    private static final int MAX_FONTSIZE = 256;
    
    private static final String SHOW_EXTRA_KEYS_KEY = "show_extra_keys";
    private static final String FONTSIZE_KEY = "fontsize";
    private static final String CURRENT_SESSION_KEY = "current_session"; // 这个key 是个 String....
    private static final String SCREEN_ALWAYS_ON_KEY = "screen_always_on";
    
    private boolean mscreenalwayson;
    private int mfontsize;
    
    @AsciiBellBehaviour
    int mbellbehaviour = BELL_VIBRATE;
    
    boolean mbackisescape;
    boolean mshowextrakeys;
    
    String[][] mextrakeys;
    
    final List<KeyboardShortcut> shortcuts = new ArrayList<>();// 快捷键列表
    
    /**
     * If value is not in the range [min, max], set it to either min or max.
     * clamp 夹紧?
     */
    static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
    
    TermuxPreferences(Context context) {
        reloadFromProperties(context);// 加载快捷键设置
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        float dipInPixels =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        
        // This is a bit arbitrary and sub-optimal. We want to give a sensible default for minimum font size
        // to prevent invisible text due to zoom be mistake:
        // 这有点武断和次优。我们想对最小字体大小给出一个合理的默认值。
        // 要防止因缩放而导致不可见文本，请执行以下操作：
        
        minFontsize = (int) (4f * dipInPixels);
        
        mshowextrakeys = prefs.getBoolean(SHOW_EXTRA_KEYS_KEY, true);// 读取配置
        mscreenalwayson = prefs.getBoolean(SCREEN_ALWAYS_ON_KEY, false);
        
        // 设置字体大小
        // http://www.google.com/design/spec/style/typography.html#typography-line-height
        int defaultFontSize = Math.round(12 * dipInPixels);
        // Make it divisible by 2 since that is the minimal adjustment step:
        if (defaultFontSize % 2 == 1)
        {
            defaultFontSize--;
        }
        
        try {
            mfontsize = Integer.parseInt(prefs.getString(FONTSIZE_KEY, Integer.toString(defaultFontSize)));
        } catch (NumberFormatException | ClassCastException e) {
            mfontsize = defaultFontSize;
        }
        mfontsize = clamp(mfontsize, minFontsize, MAX_FONTSIZE); // 最后调整区间
    }
    
    boolean toggleShowExtraKeys(Context context) {
        // 切换拓展键盘显示
        mshowextrakeys = !mshowextrakeys;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SHOW_EXTRA_KEYS_KEY, mshowextrakeys)
                .apply();
        return mshowextrakeys;
    }
    
    int getFontSize() {
        return mfontsize;
    }
    
    void changeFontSize(Context context, boolean increase) {
        // 改变字体大小
        mfontsize += (increase ? 1 : -1) * 2;
        mfontsize = Math.max(minFontsize, Math.min(mfontsize, MAX_FONTSIZE));
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(FONTSIZE_KEY, Integer.toString(mfontsize)).apply();
    }
    
    boolean isScreenAlwaysOn() {
        return mscreenalwayson;
    }
    
    void setScreenAlwaysOn(Context context, boolean newValue) {
        mscreenalwayson = newValue;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SCREEN_ALWAYS_ON_KEY, newValue)
                .apply();
    }
    
    static void storeCurrentSession(Context context, TerminalSession session) {
        // 保存当前Session
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(TermuxPreferences.CURRENT_SESSION_KEY, session.mhandle).apply();
    }
    
    static TerminalSession getCurrentSession(TermuxActivity context) {
        // 得到当前Session
        String sessionHandle = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(TermuxPreferences.CURRENT_SESSION_KEY, "");
        for (int i = 0, len = context.mtermservice.getSessions().size(); i < len; i++) {
            // 遍历寻找Session 然后返回
            TerminalSession session = context.mtermservice.getSessions().get(i);
            if (session.mhandle.equals(sessionHandle))
            {
                return session;
            }
        }
        return null;
    }
    
    void reloadFromProperties(Context context) {
        // 从properties中加载配置
        File propsFile = new File(TermuxService.HOME_PATH + "/.termux/termux.properties");
        if (!propsFile.exists())
        {
            propsFile = new File(TermuxService.HOME_PATH + "/.config/termux/termux.properties");
        }
        
        Properties props = new Properties();
        try {
            if (propsFile.isFile() && propsFile.canRead()) {
                try (FileInputStream in = new FileInputStream(propsFile)) {
                    props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            Toast.makeText(context, "Could not open properties file termux.properties.", Toast.LENGTH_LONG).show();
            Log.e("termux", "Error loading props", e);
        }
        
        switch (props.getProperty("bell-character", "vibrate")) {
            case "beep":
                mbellbehaviour = BELL_BEEP;
                break;
            case "ignore":
                mbellbehaviour = BELL_IGNORE;
                break;
            default: // "vibrate".
                mbellbehaviour = BELL_VIBRATE;
                break;
        }
        
        try {
            JSONArray arr = new JSONArray(
                    props.getProperty("extra-keys", "[['ESC', 'TAB', 'CTRL', 'ALT', '-', 'DOWN', 'UP']]"));
            
            mextrakeys = new String[arr.length()][];
            for (int i = 0; i < arr.length(); i++) {
                JSONArray line = arr.getJSONArray(i);
                mextrakeys[i] = new String[line.length()];
                for (int j = 0; j < line.length(); j++) {
                    mextrakeys[i][j] = line.getString(j);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(context, "Could not load the extra-keys property from the config: " + e.toString(),
                    Toast.LENGTH_LONG).show();
            Log.e("termux", "Error loading props", e);
            mextrakeys = new String[0][];
        }
        
        mbackisescape = "escape".equals(props.getProperty("back-key", "back"));
        
        shortcuts.clear();// 快捷键列表clear
        parseAction("shortcut.create-session", SHORTCUT_ACTION_CREATE_SESSION, props);
        parseAction("shortcut.next-session", SHORTCUT_ACTION_NEXT_SESSION, props);
        parseAction("shortcut.previous-session", SHORTCUT_ACTION_PREVIOUS_SESSION, props);
        parseAction("shortcut.rename-session", SHORTCUT_ACTION_RENAME_SESSION, props);
    }
    
    private void parseAction(String name, int shortcutAction, Properties props) {
        String value = props.getProperty(name);
        if (value == null)
        {
            return;
        }
        String[] parts = value.toLowerCase().trim().split("\\+");
        String input = parts.length == 2 ? parts[1].trim() : null;
        if (!(parts.length == 2 && parts[0].trim().equals("ctrl")) || input.isEmpty() || input.length() > 2) {
            Log.e("termux", "Keyboard shortcut '" + name + "' is not Ctrl+<something>");
            return;
        }
        
        char c = input.charAt(0);
        int codePoint = c;
        if (Character.isLowSurrogate(c)) {
            if (input.length() != 2 || Character.isHighSurrogate(input.charAt(1))) {
                Log.e("termux", "Keyboard shortcut '" + name + "' is not Ctrl+<something>");
                return;
            } else {
                codePoint = Character.toCodePoint(input.charAt(1), c);
            }
        }
        shortcuts.add(new KeyboardShortcut(codePoint, shortcutAction));
    }
    
}
