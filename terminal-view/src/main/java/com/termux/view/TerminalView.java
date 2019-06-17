package com.termux.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Scroller;

import com.termux.terminal.EmulatorDebug;
import com.termux.terminal.KeyHandler;
import com.termux.terminal.TerminalBuffer;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;

import static android.content.ContentValues.TAG;

/** View displaying and interacting with a {@link TerminalSession}. */

/**
 * @cn-annotator butub
 * 用于显示的view,和对应的TerminalSession交互
 */

public final class TerminalView extends View {
    
    /** Log view key and IME events. */
    private static final boolean LOG_KEY_EVENTS = false;
    
    /** The currently displayed terminal session, whose emulator is {@link #emulator}. */
    TerminalSession terminalSession;// 当前 Terminal Session public??
    /** Our terminal emulator whose session is {@link #terminalSession}. */
    TerminalEmulator emulator; // 当前Session 对应的 Emulator
    
    TerminalRenderer renderer; // emulator -> canvas
    
    TerminalViewClient client; // view client
    
    /** The top row of text to display. Ranges from -activeTranscriptRows to 0. */
    int mtoprow; // 显示文本的top行
    
    // 选择文本用
    boolean misselectingtext = false;
    boolean misdraggingleftselection;
    boolean minitialtextselection;
    int mselx1 = -1;
    int mselx2 = -1;
    int msely1 = -1;
    int msely2 = -1;
    float mselectiondownx;
    float mselectiondowny;
    private ActionMode actionMode;
    private BitmapDrawable mleftselectionhandle;
    private BitmapDrawable mrightselectionhandle;
    
    float mscalefactor = 1.f;
    final GestureAndScaleRecognizer mgesturerecognizer; // GestureAndScaleRecognizer
    
    // 跟踪鼠标
    /** Keep track of where mouse touch event started which we report as mouse scroll. */
    private int mmousescrollstartx = -1;
    private int mmousescrollstarty = -1;
    /** Keep track of the time when a touch event leading to sending mouse scroll events started. */
    private long mmousestartdowntime = -1;
    
    final Scroller mscroller;
    
    /** What was left in from scrolling movement. */
    float mscrollremainder;
    
    /** If non-zero, this is the last unicode code point received if that was a combining character. */
    // 如果非零，这是最后一个接收到的Unicode码位（如果是组合字符）。
    int mcombiningaccent;
    
    private boolean maccessibilityenabled;
    
    public TerminalView(Context context, AttributeSet attributes) { // NO_UCD (unused code)
        super(context, attributes);// View
        // 开手势识别器
        mgesturerecognizer = new GestureAndScaleRecognizer(context, new GestureAndScaleRecognizer.Listener() {
            
            boolean scrolledWithFinger;
            
            @Override
            public boolean onUp(MotionEvent e) {
                mscrollremainder = 0.0f;
                if (emulator != null && emulator.isMouseTrackingActive() && !misselectingtext
                        && !scrolledWithFinger) {
                    // Quick event processing when mouse tracking is active - do not wait for check of double tapping
                    // for zooming.
                    sendMouseEventCode(e, TerminalEmulator.MOUSE_LEFT_BUTTON, true);
                    sendMouseEventCode(e, TerminalEmulator.MOUSE_LEFT_BUTTON, false);
                    return true;
                }
                scrolledWithFinger = false;
                return false;
            }
            
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (emulator == null)
                {
                    return true;
                }
                if (misselectingtext) {
                    toggleSelectingText(null);
                    return true;
                }
                requestFocus();
                if (!emulator.isMouseTrackingActive()) {
                    if (!e.isFromSource(InputDevice.SOURCE_MOUSE)) {
                        client.onSingleTapUp(e);
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public boolean onScroll(MotionEvent e, float distanceX, float distanceY) {
                if (emulator == null || misselectingtext)
                {
                    return true;
                }
                if (emulator.isMouseTrackingActive() && e.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    // If moving with mouse pointer while pressing button, report that instead of scroll.
                    // This means that we never report moving with button press-events for touch input,
                    // since we cannot just start sending these events without a starting press event,
                    // which we do not do for touch input, only mouse in onTouchEvent().
                    sendMouseEventCode(e, TerminalEmulator.MOUSE_LEFT_BUTTON_MOVED, true);
                } else {
                    scrolledWithFinger = true;
                    distanceY += mscrollremainder;
                    int deltaRows = (int) (distanceY / renderer.mfontlinespacing);
                    mscrollremainder = distanceY - deltaRows * renderer.mfontlinespacing;
                    doScroll(e, deltaRows);
                }
                return true;
            }
            
            @Override
            public boolean onScale(float focusX, float focusY, float scale) {
                if (emulator == null || misselectingtext)
                {
                    return true;
                }
                mscalefactor *= scale;
                mscalefactor = client.onScale(mscalefactor);
                return true;
            }
            
            @Override
            public boolean onFling(final MotionEvent e1, final MotionEvent e2, float velocityX, float velocityY) {
                if (emulator == null || misselectingtext)
                {
                    return true;
                }
                // Do not start scrolling until last fling has been taken care of:
                if (!mscroller.isFinished())
                {
                    return true;
                }
                
                Log.d(TAG, "onFling: +++++++++++++++++++++++++++");
                // Intent intent = new Intent(getContext(), MainActivity.class);
                
                final boolean mouseTrackingAtStartOfFling = emulator.isMouseTrackingActive();
                float scale = 0.25f;
                if (mouseTrackingAtStartOfFling) {
                    mscroller.fling(0, 0, 0, -(int) (velocityY * scale), 0, 0, -emulator.mrows / 2,
                            emulator.mrows / 2);
                } else {
                    mscroller.fling(0, mtoprow, 0, -(int) (velocityY * scale), 0, 0,
                            -emulator.getScreen().getActiveTranscriptRows(), 0);
                }
                
                post(new Runnable() {
                    private int mlasty = 0;
                    
                    @Override
                    public void run() {
                        if (mouseTrackingAtStartOfFling != emulator.isMouseTrackingActive()) {
                            mscroller.abortAnimation();
                            return;
                        }
                        if (mscroller.isFinished())
                        {
                            return;
                        }
                        boolean more = mscroller.computeScrollOffset();
                        int newY = mscroller.getCurrY();
                        int diff = mouseTrackingAtStartOfFling ? (newY - mlasty) : (newY - mtoprow);
                        doScroll(e2, diff);
                        mlasty = newY;
                        if (more)
                        {
                            post(this);
                        }
                    }
                });
                
                return true;
            }
            
            @Override
            public boolean onDown(float x, float y) {
                return false;
            }
            
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Do not treat is as a single confirmed tap - it may be followed by zoom.
                return false;
            }
            
            @Override
            public void onLongPress(MotionEvent e) {
                if (mgesturerecognizer.isInProgress())
                {
                    return;
                }
                if (client.onLongPress(e))
                {
                    return;
                }
                if (!misselectingtext) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    toggleSelectingText(e);
                }
            }
        });
        mscroller = new Scroller(context);
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        maccessibilityenabled = am.isEnabled();
    }
    
    /**
     * on key listener 监听各种key，　IME: Input Method Editor 和硬件的输入
     */
    /**
     * @param onKeyListener Listener for all kinds of key events, both hardware and IME (which makes it different from that
     *                      available with {@link View#setOnKeyListener(OnKeyListener)}.
     */
    public void setOnKeyListener(TerminalViewClient onKeyListener) {
        this.client = onKeyListener;
    }
    
    /**
     * Attach a {@link TerminalSession} to this view.
     *
     * @param session The {@link TerminalSession} this view will be displaying.
     */
    public boolean attachSession(TerminalSession session) {
        if (session == terminalSession)
        {
            return false;
        }
        mtoprow = 0;
        
        terminalSession = session;
        emulator = null;
        mcombiningaccent = 0;
        
        updateSize();
        
        // Wait with enabling the scrollbar until we have a terminal to get scroll position from.
        setVerticalScrollBarEnabled(true);
        
        return true;
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // Using InputType.NULL is the most correct input type and avoids issues with other hacks.
        //
        // Previous keyboard issues:
        // https://github.com/termux/termux-packages/issues/25
        // https://github.com/termux/termux-app/issues/87.
        // https://github.com/termux/termux-app/issues/126.
        // https://github.com/termux/termux-app/issues/137 (japanese chars and TYPE_NULL).
        outAttrs.inputType = InputType.TYPE_NULL;
        
        // Note that IME_ACTION_NONE cannot be used as that makes it impossible to input newlines using the on-screen
        // keyboard on Android TV (see https://github.com/termux/termux-app/issues/221).
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        
        return new BaseInputConnection(this, true) {
            
            @Override
            public boolean finishComposingText() {
                if (LOG_KEY_EVENTS)
                {
                    Log.i(EmulatorDebug.LOG_TAG, "IME: finishComposingText()");
                }
                super.finishComposingText();
                
                sendTextToTerminal(getEditable());
                getEditable().clear();
                return true;
            }
            
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (LOG_KEY_EVENTS) {
                    Log.i(EmulatorDebug.LOG_TAG, "IME: commitText(\"" + text + "\", " + newCursorPosition + ")");
                }
                super.commitText(text, newCursorPosition);
                
                if (emulator == null)
                {
                    return true;
                }
                
                Editable content = getEditable();
                sendTextToTerminal(content);
                content.clear();
                return true;
            }
            
            @Override
            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                if (LOG_KEY_EVENTS) {
                    Log.i(EmulatorDebug.LOG_TAG, "IME: deleteSurroundingText(" + leftLength + ", " + rightLength + ")");
                }
                // The stock Samsung keyboard with 'Auto check spelling' enabled sends leftLength > 1.
                KeyEvent deleteKey = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                for (int i = 0; i < leftLength; i++)
                {
                    sendKeyEvent(deleteKey);
                }
                return super.deleteSurroundingText(leftLength, rightLength);
            }
            
            void sendTextToTerminal(CharSequence text) {
                final int textLengthInChars = text.length();
                for (int i = 0; i < textLengthInChars; i++) {
                    char firstChar = text.charAt(i);
                    int codePoint;
                    if (Character.isHighSurrogate(firstChar)) {
                        if (++i < textLengthInChars) {
                            codePoint = Character.toCodePoint(firstChar, text.charAt(i));
                        } else {
                            // At end of string, with no low surrogate following the high:
                            codePoint = TerminalEmulator.UNICODE_REPLACEMENT_CHAR;
                        }
                    } else {
                        codePoint = firstChar;
                    }
                    
                    boolean ctrlHeld = false;
                    if (codePoint <= 31 && codePoint != 27) {
                        if (codePoint == '\n') {
                            // The AOSP keyboard and descendants seems to send \n as text when the enter key is pressed,
                            // instead of a key event like most other keyboard apps. A terminal expects \r for the enter
                            // key (although when icrnl is enabled this doesn't make a difference - run 'stty -icrnl' to
                            // check the behaviour).
                            codePoint = '\r';
                        }
                        
                        // E.g. penti keyboard for ctrl input.
                        ctrlHeld = true;
                        switch (codePoint) {
                            case 31:
                                codePoint = '_';
                                break;
                            case 30:
                                codePoint = '^';
                                break;
                            case 29:
                                codePoint = ']';
                                break;
                            case 28:
                                codePoint = '\\';
                                break;
                            default:
                                codePoint += 96;
                                break;
                        }
                    }
                    
                    inputCodePoint(codePoint, ctrlHeld, false);
                }
            }
            
        };
    }
    
    @Override
    protected int computeVerticalScrollRange() {
        return emulator == null ? 1 : emulator.getScreen().getActiveRows();
    }
    
    @Override
    protected int computeVerticalScrollExtent() {
        return emulator == null ? 1 : emulator.mrows;
    }
    
    @Override
    protected int computeVerticalScrollOffset() {
        return emulator == null ? 1 : emulator.getScreen().getActiveRows() + mtoprow - emulator.mrows;
    }
    
    public void onScreenUpdated() {
        if (emulator == null)
        {
            return;
        }
        
        int rowsInHistory = emulator.getScreen().getActiveTranscriptRows();
        if (mtoprow < -rowsInHistory)
        {
            mtoprow = -rowsInHistory;
        }
        
        boolean skipScrolling = false;
        if (misselectingtext) {
            // Do not scroll when selecting text.
            int rowShift = emulator.getScrollCounter();
            if (-mtoprow + rowShift > rowsInHistory) {
                // .. unless we're hitting the end of history transcript, in which
                // case we abort text selection and scroll to end.
                toggleSelectingText(null);
            } else {
                skipScrolling = true;
                mtoprow -= rowShift;
                msely1 -= rowShift;
                msely2 -= rowShift;
            }
        }
        
        if (!skipScrolling && mtoprow != 0) {
            // Scroll down if not already there.
            if (mtoprow < -3) {
                // Awaken scroll bars only if scrolling a noticeable amount
                // - we do not want visible scroll bars during normal typing
                // of one row at a time.
                awakenScrollBars();
            }
            mtoprow = 0;
        }
        // terminalSession.write("hello");
        
        emulator.clearScrollCounter();
        
        invalidate();
        if (maccessibilityenabled)
        {
            setContentDescription(getText());
        }
        
    }
    
    /**
     * Sets the text size, which in turn sets the number of rows and columns.
     *
     * @param textSize the new font size, in density-independent pixels.
     */
    public void setTextSize(int textSize) {
        renderer = new TerminalRenderer(textSize, renderer == null ? Typeface.MONOSPACE : renderer.mtypeface);
        updateSize();
    }
    
    public void setTypeface(Typeface newTypeface) {
        renderer = new TerminalRenderer(renderer.mtextsize, newTypeface);
        updateSize();
        invalidate();
    }
    
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
    
    @Override
    public boolean isOpaque() {
        return true;
    }
    
    /** Send a single mouse event code to the terminal. */
    void sendMouseEventCode(MotionEvent e, int button, boolean pressed) {
        int x = (int) (e.getX() / renderer.mfontwidth) + 1;
        int y = (int) ((e.getY() - renderer.mfontlinespacingandascent) / renderer.mfontlinespacing) + 1;
        if (pressed && (button == TerminalEmulator.MOUSE_WHEELDOWN_BUTTON
                || button == TerminalEmulator.MOUSE_WHEELUP_BUTTON)) {
            if (mmousestartdowntime == e.getDownTime()) {
                x = mmousescrollstartx;
                y = mmousescrollstarty;
            } else {
                mmousestartdowntime = e.getDownTime();
                mmousescrollstartx = x;
                mmousescrollstarty = y;
            }
        }
        emulator.sendMouseEvent(button, x, y, pressed);
    }
    
    /** Perform a scroll, either from dragging the screen or by scrolling a mouse wheel. */
    void doScroll(MotionEvent event, int rowsDown) {
        boolean up = rowsDown < 0;
        int amount = Math.abs(rowsDown);
        for (int i = 0; i < amount; i++) {
            if (emulator.isMouseTrackingActive()) {
                sendMouseEventCode(event,
                        up ? TerminalEmulator.MOUSE_WHEELUP_BUTTON : TerminalEmulator.MOUSE_WHEELDOWN_BUTTON, true);
            } else if (emulator.isAlternateBufferActive()) {
                // Send up and down key events for scrolling, which is what some terminals do to make scroll work in
                // e.g. less, which shifts to the alt screen without mouse handling.
                handleKeyCode(up ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN, 0);
            } else {
                mtoprow = Math.min(0,
                        Math.max(-(emulator.getScreen().getActiveTranscriptRows()), mtoprow + (up ? -1 : 1)));
                if (!awakenScrollBars())
                {
                    invalidate();
                }
            }
        }
    }
    
    /** Overriding {@link View#onGenericMotionEvent(MotionEvent)}. */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (emulator != null && event.isFromSource(InputDevice.SOURCE_MOUSE)
                && event.getAction() == MotionEvent.ACTION_SCROLL) {
            // Handle mouse wheel scrolling.
            boolean up = event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f;
            doScroll(event, up ? -3 : 3);
            return true;
        }
        return false;
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    @TargetApi(23)
    public boolean onTouchEvent(MotionEvent ev) {
        if (emulator == null)
        {
            return true;
        }
        final int action = ev.getAction();
        
        if (misselectingtext) {
            int cy = (int) (ev.getY() / renderer.mfontlinespacing) + mtoprow;
            int cx = (int) (ev.getX() / renderer.mfontwidth);
            
            switch (action) {
                case MotionEvent.ACTION_UP:
                    minitialtextselection = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                    int distanceFromSel1 = Math.abs(cx - mselx1) + Math.abs(cy - msely1);
                    int distanceFromSel2 = Math.abs(cx - mselx2) + Math.abs(cy - msely2);
                    misdraggingleftselection = distanceFromSel1 <= distanceFromSel2;
                    mselectiondownx = ev.getX();
                    mselectiondowny = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (minitialtextselection)
                    {
                        break;
                    }
                    float deltaX = ev.getX() - mselectiondownx;
                    float deltaY = ev.getY() - mselectiondowny;
                    int deltaCols = (int) Math.ceil(deltaX / renderer.mfontwidth);
                    int deltaRows = (int) Math.ceil(deltaY / renderer.mfontlinespacing);
                    mselectiondownx += deltaCols * renderer.mfontwidth;
                    mselectiondowny += deltaRows * renderer.mfontlinespacing;
                    if (misdraggingleftselection) {
                        mselx1 += deltaCols;
                        msely1 += deltaRows;
                    } else {
                        mselx2 += deltaCols;
                        msely2 += deltaRows;
                    }
                    
                    mselx1 = Math.min(emulator.mcolumns, Math.max(0, mselx1));
                    mselx2 = Math.min(emulator.mcolumns, Math.max(0, mselx2));
                    
                    if (msely1 == msely2 && mselx1 > mselx2 || msely1 > msely2) {
                        // Switch handles.
                        misdraggingleftselection = !misdraggingleftselection;
                        int tmpX1;
                        tmpX1 = mselx1;
                        int tmpY1;
                        tmpY1 = msely1;

                        mselx1 = mselx2;
                        msely1 = msely2;
                        mselx2 = tmpX1;
                        msely2 = tmpY1;
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        actionMode.invalidateContentRect();
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
            mgesturerecognizer.onTouchEvent(ev);
            return true;
        } else if (ev.isFromSource(InputDevice.SOURCE_MOUSE)) {
            if (ev.isButtonPressed(MotionEvent.BUTTON_SECONDARY)) {
                if (action == MotionEvent.ACTION_DOWN)
                {
                    showContextMenu();
                }
                return true;
            } else if (ev.isButtonPressed(MotionEvent.BUTTON_TERTIARY)) {
                ClipboardManager clipboard =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null) {
                    CharSequence paste = clipData.getItemAt(0).coerceToText(getContext());
                    if (!TextUtils.isEmpty(paste))
                    {
                        emulator.paste(paste.toString());
                    }
                }
            } else if (emulator.isMouseTrackingActive()) { // BUTTON_PRIMARY.
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        sendMouseEventCode(ev, TerminalEmulator.MOUSE_LEFT_BUTTON,
                                ev.getAction() == MotionEvent.ACTION_DOWN);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        sendMouseEventCode(ev, TerminalEmulator.MOUSE_LEFT_BUTTON_MOVED, true);
                        break;
                    default:
                        break;
                }
                return true;
            }
        }
        
        mgesturerecognizer.onTouchEvent(ev);
        return true;
    }
    
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS)
        {
            Log.i(EmulatorDebug.LOG_TAG, "onKeyPreIme(keyCode=" + keyCode + ", event=" + event + ")");
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (misselectingtext) {
                toggleSelectingText(null);
                return true;
            } else if (client.shouldBackButtonBeMappedToEscape()) {
                // Intercept back button to treat it as escape:
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        return onKeyDown(keyCode, event);
                    case KeyEvent.ACTION_UP:
                        return onKeyUp(keyCode, event);
                    default:
                        break;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS)
        {
            Log.i(EmulatorDebug.LOG_TAG,
                "onKeyDown(keyCode=" + keyCode + ", isSystem()=" + event.isSystem() + ", event=" + event + ")");
        }
        if (emulator == null)
        {
            return true;
        }
        
        if (client.onKeyDown(keyCode, event, terminalSession)) {
            invalidate();
            return true;
        } else if (event.isSystem()
                && (!client.shouldBackButtonBeMappedToEscape() || keyCode != KeyEvent.KEYCODE_BACK)) {
            return super.onKeyDown(keyCode, event);
        } else if (event.getAction() == KeyEvent.ACTION_MULTIPLE && keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            terminalSession.write(event.getCharacters());
            return true;
        }
        
        final int metaState = event.getMetaState();
        final boolean controlDownFromEvent = event.isCtrlPressed();
        final boolean leftAltDownFromEvent = (metaState & KeyEvent.META_ALT_LEFT_ON) != 0;
        final boolean rightAltDownFromEvent = (metaState & KeyEvent.META_ALT_RIGHT_ON) != 0;
        
        int keyMod = 0;
        if (controlDownFromEvent)
        {
            keyMod |= KeyHandler.KEYMOD_CTRL;
        }
        if (event.isAltPressed())
        {
            keyMod |= KeyHandler.KEYMOD_ALT;
        }
        if (event.isShiftPressed())
        {
            keyMod |= KeyHandler.KEYMOD_SHIFT;
        }
        if (!event.isFunctionPressed() && handleKeyCode(keyCode, keyMod)) {
            if (LOG_KEY_EVENTS)
            {
                Log.i(EmulatorDebug.LOG_TAG, "handleKeyCode() took key event");
            }
            return true;
        }
        
        // Clear Ctrl since we handle that ourselves:
        int bitsToClear = KeyEvent.META_CTRL_MASK;
        if (rightAltDownFromEvent) {
            // Let right Alt/Alt Gr be used to compose characters.
        } else {
            // Use left alt to send to terminal (e.g. Left Alt+B to jump back a word), so remove:
            bitsToClear |= KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
        }
        int effectiveMetaState = event.getMetaState() & ~bitsToClear;
        
        int result = event.getUnicodeChar(effectiveMetaState);
        if (LOG_KEY_EVENTS)
        {
            Log.i(EmulatorDebug.LOG_TAG, "KeyEvent#getUnicodeChar(" + effectiveMetaState + ") returned: " + result);
        }
        if (result == 0) {
            return false;
        }
        
        int oldCombiningAccent = mcombiningaccent;
        if ((result & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            // If entered combining accent previously, write it out:
            if (mcombiningaccent != 0)
            {
                inputCodePoint(mcombiningaccent, controlDownFromEvent, leftAltDownFromEvent);
            }
            mcombiningaccent = result & KeyCharacterMap.COMBINING_ACCENT_MASK;
        } else {
            if (mcombiningaccent != 0) {
                int combinedChar = KeyCharacterMap.getDeadChar(mcombiningaccent, result);
                if (combinedChar > 0)
                {
                    result = combinedChar;
                }
                mcombiningaccent = 0;
            }
            inputCodePoint(result, controlDownFromEvent, leftAltDownFromEvent);
        }
        
        if (mcombiningaccent != oldCombiningAccent)
        {
            invalidate();
        }
        
        return true;
    }
    
    /**
     * @butub
     *注意这里定义的输入字符和别的io操作的内部编码
     */
    void inputCodePoint(int codePoint, boolean controlDownFromEvent, boolean leftAltDownFromEvent) {
        if (LOG_KEY_EVENTS) {
            Log.i(EmulatorDebug.LOG_TAG, "inputCodePoint(codePoint=" + codePoint + ", controlDownFromEvent="
                    + controlDownFromEvent + ", leftAltDownFromEvent=" + leftAltDownFromEvent + ")");
        }
        
        if (terminalSession == null)
        {
            return;
        }
        
        final boolean controlDown = controlDownFromEvent || client.readControlKey();
        final boolean altDown = leftAltDownFromEvent || client.readAltKey();
        
        if (client.onCodePoint(codePoint, controlDown, terminalSession))
        {
            return;
        }
        
        if (controlDown) {
            if (codePoint >= 'a' && codePoint <= 'z') {
                codePoint = codePoint - 'a' + 1;
            } else if (codePoint >= 'A' && codePoint <= 'Z') {
                codePoint = codePoint - 'A' + 1;
            } else if (codePoint == ' ' || codePoint == '2') {
                codePoint = 0;
            } else if (codePoint == '[' || codePoint == '3') {
                codePoint = 27; // ^[ (Esc)
            } else if (codePoint == '\\' || codePoint == '4') {
                codePoint = 28;
            } else if (codePoint == ']' || codePoint == '5') {
                codePoint = 29;
            } else if (codePoint == '^' || codePoint == '6') {
                codePoint = 30; // control-^
            } else if (codePoint == '_' || codePoint == '7' || codePoint == '/') {
                // "Ctrl-/ sends 0x1f which is equivalent of Ctrl-_ since the days of VT102"
                // -
                // http://apple.stackexchange.com/questions/24261/how-do-i-send-c-that-is-control-slash-to-the-terminal
                codePoint = 31;
            } else if (codePoint == '8') {
                codePoint = 127; // DEL
            }
        }
        
        if (codePoint > -1) {
            // Work around bluetooth keyboards sending funny unicode characters instead
            // of the more normal ones from ASCII that terminal programs expect - the
            // desire to input the original characters should be low.
            switch (codePoint) {
                case 0x02DC: // SMALL TILDE.
                    codePoint = 0x007E; // TILDE (~).
                    break;
                case 0x02CB: // MODIFIER LETTER GRAVE ACCENT.
                    codePoint = 0x0060; // GRAVE ACCENT (`).
                    break;
                case 0x02C6: // MODIFIER LETTER CIRCUMFLEX ACCENT.
                    codePoint = 0x005E; // CIRCUMFLEX ACCENT (^).
                    break;
                default:
                    break;
            }
            
            // If left alt, send escape before the code point to make e.g. Alt+B and Alt+F work in readline:
            terminalSession.writeCodePoint(altDown, codePoint);
        }
    }
    
    /** Input the specified keyCode if applicable and return if the input was consumed. */
    public boolean handleKeyCode(int keyCode, int keyMod) {
        TerminalEmulator term = terminalSession.getEmulator();
        String code =
                KeyHandler.getCode(keyCode, keyMod, term.isCursorKeysApplicationMode(), term.isKeypadApplicationMode());
        if (code == null)
        {
            return false;
        }
        terminalSession.write(code);
        return true;
    }
    
    /**
     * Called when a key is released in the view.
     *
     * @param keyCode The keycode of the key which was released.
     * @param event   A {@link KeyEvent} describing the event.
     * @return Whether the event was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS)
        {
            Log.i(EmulatorDebug.LOG_TAG, "onKeyUp(keyCode=" + keyCode + ", event=" + event + ")");
        }
        if (emulator == null)
        {
            return true;
        }
        
        if (client.onKeyUp(keyCode, event)) {
            invalidate();
            return true;
        } else if (event.isSystem()) {
            // Let system key events through.
            return super.onKeyUp(keyCode, event);
        }
        
        return true;
    }
    
    /**
     * This is called during layout when the size of this view has changed. If you were just added to the view
     * hierarchy, you're called with the old values of 0.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateSize();
    }
    
    /** Check if the terminal size in rows and columns should be updated. */
    public void updateSize() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0 || terminalSession == null)
        {
            return;
        }
        
        // Set to 80 and 24 if you want to enable vttest.
        int newColumns = Math.max(4, (int) (viewWidth / renderer.mfontwidth));
        int newRows = Math.max(4, (viewHeight - renderer.mfontlinespacingandascent) / renderer.mfontlinespacing);
        
        if (emulator == null || (newColumns != emulator.mcolumns || newRows != emulator.mrows)) {
            terminalSession.updateSize(newColumns, newRows);
            emulator = terminalSession.getEmulator();
            
            mtoprow = 0;
            scrollTo(0, 0);
            invalidate();
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (emulator == null) {
            canvas.drawColor(0XFF000000);
        } else {
            renderer.render(emulator, canvas, mtoprow, msely1, msely2, mselx1, mselx2);
            
            if (misselectingtext) {
                final int gripHandleWidth = mleftselectionhandle.getIntrinsicWidth();
                final int gripHandleMargin = gripHandleWidth / 4; // See the png.
                
                int right = Math.round((mselx1) * renderer.mfontwidth) + gripHandleMargin;
                int top = (msely1 + 1 - mtoprow) * renderer.mfontlinespacing + renderer.mfontlinespacingandascent;
                mleftselectionhandle.setBounds(right - gripHandleWidth, top, right,
                        top + mleftselectionhandle.getIntrinsicHeight());
                mleftselectionhandle.draw(canvas);
                
                int left = Math.round((mselx2 + 1) * renderer.mfontwidth) - gripHandleMargin;
                top = (msely2 + 1 - mtoprow) * renderer.mfontlinespacing + renderer.mfontlinespacingandascent;
                mrightselectionhandle.setBounds(left, top, left + gripHandleWidth,
                        top + mrightselectionhandle.getIntrinsicHeight());
                mrightselectionhandle.draw(canvas);
            }
        }
    }
    
    /** Toggle text selection mode in the view. */
    @TargetApi(23)
    public void toggleSelectingText(MotionEvent ev) {
        misselectingtext = !misselectingtext;
        client.copyModeChanged(misselectingtext);
        
        if (misselectingtext) {
            if (mleftselectionhandle == null) {
                mleftselectionhandle =
                        (BitmapDrawable) getContext().getDrawable(R.drawable.text_select_handle_left_material);
                mrightselectionhandle =
                        (BitmapDrawable) getContext().getDrawable(R.drawable.text_select_handle_right_material);
            }
            
            int cx = (int) (ev.getX() / renderer.mfontwidth);
            final boolean eventFromMouse = ev.isFromSource(InputDevice.SOURCE_MOUSE);
            // Offset for finger:
            final int selectTextOffsetY = eventFromMouse ? 0 : -40;
            int cy = (int) ((ev.getY() + selectTextOffsetY) / renderer.mfontlinespacing) + mtoprow;
            
            mselx1 = mselx2 = cx;
            msely1 = msely2 = cy;
            
            TerminalBuffer screen = emulator.getScreen();
            if (!" ".equals(screen.getSelectedText(mselx1, msely1, mselx1, msely1))) {
                // Selecting something other than whitespace. Expand to word.
                while (mselx1 > 0 && !"".equals(screen.getSelectedText(mselx1 - 1, msely1, mselx1 - 1, msely1))) {
                    mselx1--;
                }
                while (mselx2 < emulator.mcolumns - 1
                        && !"".equals(screen.getSelectedText(mselx2 + 1, msely1, mselx2 + 1, msely1))) {
                    mselx2++;
                }
            }
            
            minitialtextselection = true;
            misdraggingleftselection = true;
            mselectiondownx = ev.getX();
            mselectiondowny = ev.getY();
            
            final ActionMode.Callback callback = new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    int show = MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT;
                    
                    ClipboardManager clipboard =
                            (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    menu.add(Menu.NONE, 1, Menu.NONE, R.string.copy_text).setShowAsAction(show);
                    menu.add(Menu.NONE, 2, Menu.NONE, R.string.paste_text).setEnabled(clipboard.hasPrimaryClip())
                            .setShowAsAction(show);
                    menu.add(Menu.NONE, 3, Menu.NONE, R.string.text_selection_more);
                    return true;
                }
                
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
                
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (!misselectingtext) {
                        // Fix issue where the dialog is pressed while being dismissed.
                        return true;
                    }
                    switch (item.getItemId()) {
                        case 1:
                            String selectedText = emulator.getSelectedText(mselx1, msely1, mselx2, msely2).trim();
                            terminalSession.clipboardText(selectedText);
                            break;
                        case 2:
                            ClipboardManager clipboard =
                                    (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = clipboard.getPrimaryClip();
                            if (clipData != null) {
                                CharSequence paste = clipData.getItemAt(0).coerceToText(getContext());
                                if (!TextUtils.isEmpty(paste))
                                {
                                    emulator.paste(paste.toString());
                                }
                            }
                            break;
                        case 3:
                            showContextMenu();
                            break;
                        default:
                            break;
                    }
                    toggleSelectingText(null);
                    return true;
                }
                
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
                
            };
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                actionMode = startActionMode(new ActionMode.Callback2() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return callback.onCreateActionMode(mode, menu);
                    }
                    
                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }
                    
                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return callback.onActionItemClicked(mode, item);
                    }
                    
                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        // Ignore.
                    }
                    
                    @Override
                    public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                        int x1 = Math.round(mselx1 * renderer.mfontwidth);
                        int x2 = Math.round(mselx2 * renderer.mfontwidth);
                        int y1 = Math.round((msely1 - mtoprow) * renderer.mfontlinespacing);
                        int y2 = Math.round((msely2 + 1 - mtoprow) * renderer.mfontlinespacing);
                        outRect.set(Math.min(x1, x2), y1, Math.max(x1, x2), y2);
                    }
                }, ActionMode.TYPE_FLOATING);
            } else {
                actionMode = startActionMode(callback);
            }
            
            invalidate();
        } else {
            actionMode.finish();
            mselx1 = msely1 = mselx2 = msely2 = -1;
            invalidate();
        }
    }
    
    public TerminalSession getCurrentSession() {
        return terminalSession;
    }
    
    private CharSequence getText() {
        return emulator.getScreen().getSelectedText(0, mtoprow, emulator.mcolumns, mtoprow + emulator.mrows);
    }
    
}
