package com.termux.terminal;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

/**
 * Renders text into a screen. Contains all the terminal-specific knowledge and state. Emulates a subset of the X Window
 * System xterm terminal, which in turn is an emulator for a subset of the Digital Equipment Corporation vt100 terminal.
 * <p>
 * References:
 * <ul>
 * <li>http://invisible-island.net/xterm/ctlseqs/ctlseqs.html</li>
 * <li>http://en.wikipedia.org/wiki/ANSI_escape_code</li>
 * <li>http://man.he.net/man4/console_codes</li>
 * <li>http://bazaar.launchpad.net/~leonerd/libvterm/trunk/view/head:/src/state.c</li>
 * <li>http://www.columbia.edu/~kermit/k95manual/iso2022.html</li>
 * <li>http://www.vt100.net/docs/vt510-rm/chapter4</li>
 * <li>http://en.wikipedia.org/wiki/ISO/IEC_2022 - for 7-bit and 8-bit GL GR explanation</li>
 * <li>http://bjh21.me.uk/all-escapes/all-escapes.txt - extensive!</li>
 * <li>http://woldlab.caltech.edu/~diane/kde4.10/workingdir/kubuntu/konsole/doc/developer/old-documents/VT100/techref.
 * html - document for konsole - accessible!</li>
 * </ul>
 */

/**
 * @butub
 * 这个用于把文本显示到屏幕上,　包含了每个独立终端的信息,　模拟一个X-Window　System xterm terminal的子集
 */
public final class TerminalEmulator {
    
    /** Log unknown or unimplemented escape sequences received from the shell process. */
    private static final boolean LOG_ESCAPE_SEQUENCES = false;
    
    public static final int MOUSE_LEFT_BUTTON = 0;
    
    /** Mouse moving while having left mouse button pressed. */
    public static final int MOUSE_LEFT_BUTTON_MOVED = 32;
    public static final int MOUSE_WHEELUP_BUTTON = 64;
    public static final int MOUSE_WHEELDOWN_BUTTON = 65;
    
    public static final int CURSOR_STYLE_BLOCK = 0;
    public static final int CURSOR_STYLE_UNDERLINE = 1;
    public static final int CURSOR_STYLE_BAR = 2;
    
    /** Used for invalid data - http://en.wikipedia.org/wiki/Replacement_character#Replacement_character */
    public static final int UNICODE_REPLACEMENT_CHAR = 0xFFFD;
    
    /** Escape processing: Not currently in an escape sequence. */
    private static final int ESC_NONE = 0;
    /** Escape processing: Have seen an ESC character - proceed to {@link #doEsc(int)} */
    private static final int ESC = 1;
    /** Escape processing: Have seen ESC POUND */
    private static final int ESC_POUND = 2;
    /** Escape processing: Have seen ESC and a character-set-select ( char */
    private static final int ESC_SELECT_LEFT_PAREN = 3;
    /** Escape processing: Have seen ESC and a character-set-select ) char */
    private static final int ESC_SELECT_RIGHT_PAREN = 4;
    /** Escape processing: "ESC [" or CSI (Control Sequence Introducer). */
    private static final int ESC_CSI = 6;
    /** Escape processing: ESC [ ? */
    private static final int ESC_CSI_QUESTIONMARK = 7;
    /** Escape processing: ESC [ $ */
    private static final int ESC_CSI_DOLLAR = 8;
    /** Escape processing: ESC % */
    private static final int ESC_PERCENT = 9;
    /** Escape processing: ESC ] (AKA OSC - Operating System Controls) */
    private static final int ESC_OSC = 10;
    /** Escape processing: ESC ] (AKA OSC - Operating System Controls) ESC */
    private static final int ESC_OSC_ESC = 11;
    /** Escape processing: ESC [ > */
    private static final int ESC_CSI_BIGGERTHAN = 12;
    /** Escape procession: "ESC P" or Device Control String (DCS) */
    private static final int ESC_P = 13;
    /** Escape processing: CSI > */
    private static final int ESC_CSI_QUESTIONMARK_ARG_DOLLAR = 14;
    /** Escape processing: CSI $ARGS ' ' */
    private static final int ESC_CSI_ARGS_SPACE = 15;
    /** Escape processing: CSI $ARGS '*' */
    private static final int ESC_CSI_ARGS_ASTERIX = 16;
    /** Escape processing: CSI " */
    private static final int ESC_CSI_DOUBLE_QUOTE = 17;
    /** Escape processing: CSI ' */
    private static final int ESC_CSI_SINGLE_QUOTE = 18;
    /** Escape processing: CSI ! */
    private static final int ESC_CSI_EXCLAMATION = 19;
    
    /** The number of parameter arguments. This name comes from the ANSI standard for terminal escape codes. */
    private static final int MAX_ESCAPE_PARAMETERS = 16;
    
    /** Needs to be large enough to contain reasonable OSC 52 pastes. */
    private static final int MAX_OSC_STRING_LENGTH = 8192;
    
    /** DECSET 1 - application cursor keys. */
    private static final int DECSET_BIT_APPLICATION_CURSOR_KEYS = 1;
    private static final int DECSET_BIT_REVERSE_VIDEO = 1 << 1;
    /**
     * http://www.vt100.net/docs/vt510-rm/DECOM: "When DECOM is set, the home cursor position is at the upper-left
     * corner of the screen, within the margins. The starting point for line numbers depends on the current top margin
     * setting. The cursor cannot move outside of the margins. When DECOM is reset, the home cursor position is at the
     * upper-left corner of the screen. The starting point for line numbers is independent of the margins. The cursor
     * can move outside of the margins."
     */
    private static final int DECSET_BIT_ORIGIN_MODE = 1 << 2;
    /**
     * http://www.vt100.net/docs/vt510-rm/DECAWM: "If the DECAWM function is set, then graphic characters received when
     * the cursor is at the right border of the page appear at the beginning of the next line. Any text on the page
     * scrolls up if the cursor is at the end of the scrolling region. If the DECAWM function is reset, then graphic
     * characters received when the cursor is at the right border of the page replace characters already on the page."
     */
    private static final int DECSET_BIT_AUTOWRAP = 1 << 3;
    /** DECSET 25 - if the cursor should be visible, {@link #isShowingCursor()}. */
    private static final int DECSET_BIT_SHOWING_CURSOR = 1 << 4;
    private static final int DECSET_BIT_APPLICATION_KEYPAD = 1 << 5;
    /** DECSET 1000 - if to report mouse press&release events. */
    private static final int DECSET_BIT_MOUSE_TRACKING_PRESS_RELEASE = 1 << 6;
    /** DECSET 1002 - like 1000, but report moving mouse while pressed. */
    private static final int DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT = 1 << 7;
    /** DECSET 1004 - NOT implemented. */
    private static final int DECSET_BIT_SEND_FOCUS_EVENTS = 1 << 8;
    /** DECSET 1006 - SGR-like mouse protocol (the modern sane choice). */
    private static final int DECSET_BIT_MOUSE_PROTOCOL_SGR = 1 << 9;
    /** DECSET 2004 - see {@link #paste(String)} */
    private static final int DECSET_BIT_BRACKETED_PASTE_MODE = 1 << 10;
    /** Toggled with DECLRMM - http://www.vt100.net/docs/vt510-rm/DECLRMM */
    private static final int DECSET_BIT_LEFTRIGHT_MARGIN_MODE = 1 << 11;
    /** Not really DECSET bit... - http://www.vt100.net/docs/vt510-rm/DECSACE */
    private static final int DECSET_BIT_RECTANGULAR_CHANGEATTRIBUTE = 1 << 12;
    
    private String mtitle;
    private final Stack<String> mtitlestack = new Stack<>();
    
    /** The cursor position. Between (0,0) and (mrows-1, mcolumns-1). */
    private int mcursorrow;
    private int mcursorcol;
    
    private int mcursorstyle = CURSOR_STYLE_BLOCK;
    
    /** The number of character rows and columns in the terminal screen. */
    public int mrows;
    public int mcolumns;
    
    /** The normal screen buffer. Stores the characters that appear on the screen of the emulated terminal. */
    private final TerminalBuffer mmainbuffer;
    /**
     * The alternate screen buffer, exactly as large as the display and contains no additional saved lines (so that when
     * the alternate screen buffer is active, you cannot scroll back to view saved lines).
     * <p>
     * See http://www.xfree86.org/current/ctlseqs.html#The%20Alternate%20Screen%20Buffer
     */
    final TerminalBuffer maltbuffer;
    /** The current screen buffer, pointing at either {@link #mmainbuffer} or {@link #maltbuffer}. */
    private TerminalBuffer mscreen;
    
    /** The terminal session this emulator is bound to. */
    private final TerminalOutput msession;
    
    /** Keeps track of the current argument of the current escape sequence. Ranges from 0 to MAX_ESCAPE_PARAMETERS-1. */
    private int margindex;
    /** Holds the arguments of the current escape sequence. */
    private final int[] margs = new int[MAX_ESCAPE_PARAMETERS];
    
    /** Holds OSC and device control arguments, which can be strings. */
    private final StringBuilder moscordevicecontrolargs = new StringBuilder();
    
    /**
     * True if the current escape sequence should continue, false if the current escape sequence should be terminated.
     * Used when parsing a single character.
     */
    private boolean mcontinuesequence;
    
    /** The current state of the escape sequence state machine. One of the ESC_* constants. */
    private int mescapestate;
    
    private final SavedScreenState msavedstatemain = new SavedScreenState();
    private final SavedScreenState msavedstatealt = new SavedScreenState();
    
    /** http://www.vt100.net/docs/vt102-ug/table5-15.html */
    private boolean muselinedrawingg0;
    private boolean muselinedrawingg1;
    private boolean muselinedrawingusesg0 = true;
    
    /**
     * @see TerminalEmulator#mapDecSetBitToInternalBit(int)
     */
    private int mcurrentdecsetflags;
    private int msaveddecsetflags;
    
    /**
     * If insert mode (as opposed to replace mode) is active. In insert mode new characters are inserted, pushing
     * existing text to the right. Characters moved past the right margin are lost.
     */
    private boolean minsertmode;
    
    /** An array of tab stops. mtabstop[i] is true if there is a tab stop set for column i. */
    private boolean[] mtabstop;
    
    /**
     * Top margin of screen for scrolling ranges from 0 to mrows-2. Bottom margin ranges from mtopmargin + 2 to mrows
     * (Defines the first row after the scrolling region). Left/right margin in [0, mcolumns].
     */
    private int mtopmargin;
    private int mbottommargin;
    private int mleftmargin;
    private int mrightmargin;
    
    /**
     * If the next character to be emitted will be automatically wrapped to the next line. Used to disambiguate the case
     * where the cursor is positioned on the last column (mcolumns-1). When standing there, a written character will be
     * output in the last column, the cursor not moving but this flag will be set. When outputting another character
     * this will move to the next line.
     */
    private boolean mabouttoautowrap;
    
    /**
     * Current foreground and background colors. Can either be a color index in [0,259] or a truecolor (24-bit) value.
     * For a 24-bit value the top byte (0xff000000) is set.
     *
     * @see TextStyle
     */
    int mforecolor;
    int mbackcolor;
    
    /** Current {@link TextStyle} effect. */
    private int meffect;
    
    /**
     * The number of scrolled lines since last calling {@link #clearScrollCounter()}. Used for moving selection up along
     * with the scrolling text.
     */
    private int mscrollcounter = 0;
    
    private byte mutf8Tofollow;
    private byte mutf8Index;
    private final byte[] mutf8Inputbuffer = new byte[4];
    private int mlastemittedcodepoint = -1;
    
    public final TerminalColors mcolors = new TerminalColors();
    
    private boolean isDecsetInternalBitSet(int bit) {
        return (mcurrentdecsetflags & bit) != 0;
    }
    
    private void setDecsetinternalBit(int internalBit, boolean set) {
        if (set) {
            // The mouse modes are mutually exclusive.
            if (internalBit == DECSET_BIT_MOUSE_TRACKING_PRESS_RELEASE) {
                setDecsetinternalBit(DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT, false);
            } else if (internalBit == DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT) {
                setDecsetinternalBit(DECSET_BIT_MOUSE_TRACKING_PRESS_RELEASE, false);
            }
        }
        if (set) {
            mcurrentdecsetflags |= internalBit;
        } else {
            mcurrentdecsetflags &= ~internalBit;
        }
    }
    
    static int mapDecSetBitToInternalBit(int decsetBit) {
        switch (decsetBit) {
            case 1:
                return DECSET_BIT_APPLICATION_CURSOR_KEYS;
            case 5:
                return DECSET_BIT_REVERSE_VIDEO;
            case 6:
                return DECSET_BIT_ORIGIN_MODE;
            case 7:
                return DECSET_BIT_AUTOWRAP;
            case 25:
                return DECSET_BIT_SHOWING_CURSOR;
            case 66:
                return DECSET_BIT_APPLICATION_KEYPAD;
            case 69:
                return DECSET_BIT_LEFTRIGHT_MARGIN_MODE;
            case 1000:
                return DECSET_BIT_MOUSE_TRACKING_PRESS_RELEASE;
            case 1002:
                return DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT;
            case 1004:
                return DECSET_BIT_SEND_FOCUS_EVENTS;
            case 1006:
                return DECSET_BIT_MOUSE_PROTOCOL_SGR;
            case 2004:
                return DECSET_BIT_BRACKETED_PASTE_MODE;
            default:
                return -1;
            // throw new IllegalArgumentException("Unsupported decset: " + decsetBit);
        }
    }
    
    public TerminalEmulator(TerminalOutput session, int columns, int rows, int transcriptRows) {
        msession = session;
        mscreen = mmainbuffer = new TerminalBuffer(columns, transcriptRows, rows);
        maltbuffer = new TerminalBuffer(columns, rows, rows);
        mrows = rows;
        mcolumns = columns;
        mtabstop = new boolean[mcolumns];
        reset();
    }
    
    public TerminalBuffer getScreen() {
        return mscreen;
    }
    
    public boolean isAlternateBufferActive() {
        return mscreen == maltbuffer;
    }
    
    /**
     * @param mouseButton one of the MOUSE_* constants of this class.
     */
    public void sendMouseEvent(int mouseButton, int column, int row, boolean pressed) {
        if (column < 1)
        {
            column = 1;
        }
        if (column > mcolumns)
        {
            column = mcolumns;
        }
        if (row < 1)
        {
            row = 1;
        }
        if (row > mrows)
        {
            row = mrows;
        }
        
        if (mouseButton == MOUSE_LEFT_BUTTON_MOVED && !isDecsetInternalBitSet(DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT)) {
            // Do not send tracking.
        } else if (isDecsetInternalBitSet(DECSET_BIT_MOUSE_PROTOCOL_SGR)) {
            msession.write(String.format("\033[<%d;%d;%d" + (pressed ? 'M' : 'm'), mouseButton, column, row));
        } else {
            mouseButton = pressed ? mouseButton : 3; // 3 for release of all buttons.
            // Clip to screen, and clip to the limits of 8-bit data.
            boolean outOfBounds = column > 255 - 32 || row > 255 - 32;
            if (!outOfBounds) {
                byte[] data = { '\033', '[', 'M', (byte) (32 + mouseButton), (byte) (32 + column), (byte) (32 + row) };
                msession.write(data, 0, data.length);
            }
        }
    }
    
    public void resize(int columns, int rows) {
        if (mrows == rows && mcolumns == columns) {
            return;
        } else if (columns < 2 || rows < 2) {
            throw new IllegalArgumentException("rows=" + rows + ", columns=" + columns);
        }
        
        if (mrows != rows) {
            mrows = rows;
            mtopmargin = 0;
            mbottommargin = mrows;
        }
        if (mcolumns != columns) {
            int oldColumns;
            oldColumns = mcolumns;
            mcolumns = columns;
            boolean[] oldTabStop = mtabstop;
            mtabstop = new boolean[mcolumns];
            setDefaultTabStops();
            int toTransfer = Math.min(oldColumns, columns);
            System.arraycopy(oldTabStop, 0, mtabstop, 0, toTransfer);
            mleftmargin = 0;
            mrightmargin = mcolumns;
        }
        
        resizeScreen();
    }
    
    private void resizeScreen() {
        final int[] cursor = {mcursorcol, mcursorrow};
        int newTotalRows = (mscreen == maltbuffer) ? mrows : mmainbuffer.mtotalrows;
        mscreen.resize(mcolumns, mrows, newTotalRows, cursor, getStyle(), isAlternateBufferActive());
        mcursorcol = cursor[0];
        mcursorrow = cursor[1];
    }
    
    public int getCursorRow() {
        return mcursorrow;
    }
    
    public int getCursorCol() {
        return mcursorcol;
    }
    
    /** {@link #CURSOR_STYLE_BAR}, {@link #CURSOR_STYLE_BLOCK} or {@link #CURSOR_STYLE_UNDERLINE} */
    public int getCursorStyle() {
        return mcursorstyle;
    }
    
    public boolean isReverseVideo() {
        return isDecsetInternalBitSet(DECSET_BIT_REVERSE_VIDEO);
    }
    
    public boolean isShowingCursor() {
        return isDecsetInternalBitSet(DECSET_BIT_SHOWING_CURSOR);
    }
    
    public boolean isKeypadApplicationMode() {
        return isDecsetInternalBitSet(DECSET_BIT_APPLICATION_KEYPAD);
    }
    
    public boolean isCursorKeysApplicationMode() {
        return isDecsetInternalBitSet(DECSET_BIT_APPLICATION_CURSOR_KEYS);
    }
    
    /** If mouse events are being sent as escape codes to the terminal. */
    public boolean isMouseTrackingActive() {
        return isDecsetInternalBitSet(DECSET_BIT_MOUSE_TRACKING_PRESS_RELEASE)
                || isDecsetInternalBitSet(DECSET_BIT_MOUSE_TRACKING_BUTTON_EVENT);
    }
    
    private void setDefaultTabStops() {
        for (int i = 0; i < mcolumns; i++)
        {
            mtabstop[i] = (i & 7) == 0 && i != 0;
        }
    }
    
    /**
     * Accept bytes (typically from the pseudo-teletype) and process them.
     *接受并处理需要显示在屏幕上的序列
     * @param buffer a byte array containing the bytes to be processed
     * @param length the number of bytes in the array to process
     */
    public void append(byte[] buffer, int length) {
        // todo
        for (int i = 0; i < length; i++)
        {
            processByte(buffer[i]);
        }
    }
    
    private void processByte(byte byteToProcess) {
        if (mutf8Tofollow > 0) {
            if ((byteToProcess & 0b11000000) == 0b10000000) {
                // 10xxxxxx, a continuation byte.
                mutf8Inputbuffer[mutf8Index++] = byteToProcess;
                if (--mutf8Tofollow == 0) {
                    byte firstByteMask =
                            (byte) (mutf8Index == 2 ? 0b00011111 : (mutf8Index == 3 ? 0b00001111 : 0b00000111));
                    int codePoint = (mutf8Inputbuffer[0] & firstByteMask);
                    for (int i = 1; i < mutf8Index; i++)
                    {
                        codePoint = ((codePoint << 6) | (mutf8Inputbuffer[i] & 0b00111111));
                    }
                    if (((codePoint <= 0b1111111) && mutf8Index > 1) || (codePoint < 0b11111111111 && mutf8Index > 2)
                            || (codePoint < 0b1111111111111111 && mutf8Index > 3)) {
                        // Overlong encoding.
                        codePoint = UNICODE_REPLACEMENT_CHAR;
                    }
                    
                    mutf8Index = mutf8Tofollow = 0;
                    
                    if (codePoint >= 0x80 && codePoint <= 0x9F) {
                        // Sequence decoded to a C1 control character which we ignore. They are
                        // not used nowadays and increases the risk of messing up the terminal state
                        // on binary input. XTerm does not allow them in utf-8:
                        // "It is not possible to use a C1 control obtained from decoding the
                        // UTF-8 text" - http://invisible-island.net/xterm/ctlseqs/ctlseqs.html
                    } else {
                        switch (Character.getType(codePoint)) {
                            case Character.UNASSIGNED:
                            case Character.SURROGATE:
                                codePoint = UNICODE_REPLACEMENT_CHAR;
                                break;
                            default:
                                break;
                        }
                        processCodePoint(codePoint);
                    }
                }
            } else {
                // Not a UTF-8 continuation byte so replace the entire sequence up to now with the replacement char:
                mutf8Index = mutf8Tofollow = 0;
                emitCodePoint(UNICODE_REPLACEMENT_CHAR);
                // The Unicode Standard Version 6.2 – Core Specification
                // (http://www.unicode.org/versions/Unicode6.2.0/ch03.pdf):
                // "If the converter encounters an ill-formed UTF-8 code unit sequence which starts with a valid first
                // byte, but which does not continue with valid successor bytes (see Table 3-7), it must not consume the
                // successor bytes as part of the ill-formed subsequence
                // whenever those successor bytes themselves constitute part of a well-formed UTF-8 code unit
                // subsequence."
                processByte(byteToProcess);
            }
        } else {
            if ((byteToProcess & 0b10000000) == 0) { // The leading bit is not set so it is a 7-bit ASCII character.
                processCodePoint(byteToProcess);
                return;
            } else if ((byteToProcess & 0b11100000) == 0b11000000) { // 110xxxxx, a two-byte sequence.
                mutf8Tofollow = 1;
            } else if ((byteToProcess & 0b11110000) == 0b11100000) { // 1110xxxx, a three-byte sequence.
                mutf8Tofollow = 2;
            } else if ((byteToProcess & 0b11111000) == 0b11110000) { // 11110xxx, a four-byte sequence.
                mutf8Tofollow = 3;
            } else {
                // Not a valid UTF-8 sequence start, signal invalid data:
                processCodePoint(UNICODE_REPLACEMENT_CHAR);
                return;
            }
            mutf8Inputbuffer[mutf8Index++] = byteToProcess;
        }
    }
    
    public void processCodePoint(int b) {
        switch (b) {
            case 0: // Null character (NUL, ^@). Do nothing.
                break;
            case 7: // Bell (BEL, ^G, \a). If in an OSC sequence, BEL may terminate a string; otherwise signal bell.
                if (mescapestate == ESC_OSC)
                {
                    doOsc(b);
                }
                else
                {
                    msession.onBell();
                }
                break;
            case 8: // Backspace (BS, ^H).
                if (mleftmargin == mcursorcol) {
                    // Jump to previous line if it was auto-wrapped.
                    int previousRow = mcursorrow - 1;
                    if (previousRow >= 0 && mscreen.getLineWrap(previousRow)) {
                        mscreen.clearLineWrap(previousRow);
                        setCursorRowCol(previousRow, mrightmargin - 1);
                    }
                } else {
                    setCursorCol(mcursorcol - 1);
                }
                break;
            case 9: // Horizontal tab (HT, \t) - move to next tab stop, but not past edge of screen
                // XXX: Should perhaps use color if writing to new cells. Try with
                // printf "\033[41m\tXX\033[0m\n"
                // The OSX Terminal.app colors the spaces from the tab red, but xterm does not.
                // Note that Terminal.app only colors on new cells, in e.g.
                // printf "\033[41m\t\r\033[42m\tXX\033[0m\n"
                // the first cells are created with a red background, but when tabbing over
                // them again with a green background they are not overwritten.
                mcursorcol = nextTabStop(1);
                break;
            case 10: // Line feed (LF, \n).
            case 11: // Vertical tab (VT, \v).
            case 12: // Form feed (FF, \f).
                doLinefeed();
                break;
            case 13: // Carriage return (CR, \r).
                setCursorCol(mleftmargin);
                break;
            case 14: // Shift Out (Ctrl-N, SO) → Switch to Alternate Character Set. This invokes the G1 character set.
                muselinedrawingusesg0 = false;
                break;
            case 15: // Shift In (Ctrl-O, SI) → Switch to Standard Character Set. This invokes the G0 character set.
                muselinedrawingusesg0 = true;
                break;
            case 24: // CAN.
            case 26: // SUB.
                if (mescapestate != ESC_NONE) {
                    // FIXME: What is this??
                    mescapestate = ESC_NONE;
                    emitCodePoint(127);
                }
                break;
            case 27: // ESC
                // Starts an escape sequence unless we're parsing a string
                if (mescapestate == ESC_P) {
                    // XXX: Ignore escape when reading device control sequence, since it may be part of string
                    // terminator.
                    return;
                } else if (mescapestate != ESC_OSC) {
                    startEscapeSequence();
                } else {
                    doOsc(b);
                }
                break;
            default:
                mcontinuesequence = false;
                switch (mescapestate) {
                    case ESC_NONE:
                        if (b >= 32)
                        {
                            emitCodePoint(b);
                        }
                        break;
                    case ESC:
                        doEsc(b);
                        break;
                    case ESC_POUND:
                        doEscPound(b);
                        break;
                    case ESC_SELECT_LEFT_PAREN: // Designate G0 Character Set (ISO 2022, VT100).
                        muselinedrawingg0 = (b == '0');
                        break;
                    case ESC_SELECT_RIGHT_PAREN: // Designate G1 Character Set (ISO 2022, VT100).
                        muselinedrawingg1 = (b == '0');
                        break;
                    case ESC_CSI:
                        doCsi(b);
                        break;
                    case ESC_CSI_EXCLAMATION:
                        if (b == 'p') { // Soft terminal reset (DECSTR, http://vt100.net/docs/vt510-rm/DECSTR).
                            reset();
                        } else {
                            unknownSequence(b);
                        }
                        break;
                    case ESC_CSI_QUESTIONMARK:
                        doCsiQuestionMark(b);
                        break;
                    case ESC_CSI_BIGGERTHAN:
                        doCsiBiggerThan(b);
                        break;
                    case ESC_CSI_DOLLAR:
                        boolean originMode = isDecsetInternalBitSet(DECSET_BIT_ORIGIN_MODE);
                        int effectiveTopMargin = originMode ? mtopmargin : 0;
                        int effectiveBottomMargin = originMode ? mbottommargin : mrows;
                        int effectiveLeftMargin = originMode ? mleftmargin : 0;
                        int effectiveRightMargin = originMode ? mrightmargin : mcolumns;
                        switch (b) {
                            case 'v': // ${CSI}${SRC_TOP}${SRC_LEFT}${SRC_BOTTOM}${SRC_RIGHT}${SRC_PAGE}${DST_TOP}${DST_LEFT}${DST_PAGE}$v"
                                // Copy rectangular area (DECCRA - http://vt100.net/docs/vt510-rm/DECCRA):
                                // "If Pbs is greater than Pts, or Pls is greater than Prs, the terminal ignores DECCRA.
                                // The coordinates of the rectangular area are affected by the setting of origin mode
                                // (DECOM).
                                // DECCRA is not affected by the page margins.
                                // The copied text takes on the line attributes of the destination area.
                                // If the value of Pt, Pl, Pb, or Pr exceeds the width or height of the active page,
                                // then the value
                                // is treated as the width or height of that page.
                                // If the destination area is partially off the page, then DECCRA clips the off-page
                                // data.
                                // DECCRA does not change the active cursor position."
                                int topSource = Math.min(getArg(0, 1, true) - 1 + effectiveTopMargin, mrows);
                                int leftSource = Math.min(getArg(1, 1, true) - 1 + effectiveLeftMargin, mcolumns);
                                // Inclusive, so do not subtract one:
                                int bottomSource = Math
                                        .min(Math.max(getArg(2, mrows, true) + effectiveTopMargin, topSource), mrows);
                                int rightSource =
                                        Math.min(Math.max(getArg(3, mcolumns, true) + effectiveLeftMargin, leftSource),
                                            mcolumns);
                                // int sourcePage = getArg(4, 1, true);
                                int destionationTop = Math.min(getArg(5, 1, true) - 1 + effectiveTopMargin, mrows);
                                int destinationLeft = Math.min(getArg(6, 1, true) - 1 + effectiveLeftMargin, mcolumns);
                                // int destinationPage = getArg(7, 1, true);
                                int heightToCopy = Math.min(mrows - destionationTop, bottomSource - topSource);
                                int widthToCopy = Math.min(mcolumns - destinationLeft, rightSource - leftSource);
                                mscreen.blockCopy(leftSource, topSource, widthToCopy, heightToCopy, destinationLeft,
                                        destionationTop);
                                break;
                            case '{': // ${CSI}${TOP}${LEFT}${BOTTOM}${RIGHT}${"
                                // Selective erase rectangular area (DECSERA -
                                // http://www.vt100.net/docs/vt510-rm/DECSERA).
                            case 'x': // ${CSI}${CHAR};${TOP}${LEFT}${BOTTOM}${RIGHT}$x"
                                // Fill rectangular area (DECFRA - http://www.vt100.net/docs/vt510-rm/DECFRA).
                            case 'z': // ${CSI}$${TOP}${LEFT}${BOTTOM}${RIGHT}$z"
                                // Erase rectangular area (DECERA - http://www.vt100.net/docs/vt510-rm/DECERA).
                                boolean erase = b != 'x';
                                boolean selective = b == '{';
                                // Only DECSERA keeps visual attributes, DECERA does not:
                                boolean keepVisualAttributes = erase && selective;
                                int argIndex = 0;
                                int fillChar = erase ? ' ' : getArg(argIndex++, -1, true);
                                // "Pch can be any value from 32 to 126 or from 160 to 255. If Pch is not in this range,
                                // then the
                                // terminal ignores the DECFRA command":
                                if ((fillChar >= 32 && fillChar <= 126) || (fillChar >= 160 && fillChar <= 255)) {
                                    // "If the value of Pt, Pl, Pb, or Pr exceeds the width or height of the active
                                    // page, the value
                                    // is treated as the width or height of that page."
                                    int top = Math.min(getArg(argIndex++, 1, true) + effectiveTopMargin,
                                            effectiveBottomMargin + 1);
                                    int left = Math.min(getArg(argIndex++, 1, true) + effectiveLeftMargin,
                                            effectiveRightMargin + 1);
                                    int bottom = Math.min(getArg(argIndex++, mrows, true) + effectiveTopMargin,
                                            effectiveBottomMargin);
                                    int right = Math.min(getArg(argIndex, mcolumns, true) + effectiveLeftMargin,
                                            effectiveRightMargin);
                                    long style = getStyle();
                                    for (int row = top - 1; row < bottom; row++)
                                    {
                                        for (int col = left - 1; col < right; col++)
                                        {
                                            if (!selective || (TextStyle.decodeEffect(mscreen.getStyleAt(row, col))
                                                & TextStyle.CHARACTER_ATTRIBUTE_PROTECTED) == 0)
                                            {
                                                mscreen.setChar(col, row, fillChar,
                                                    keepVisualAttributes ? mscreen.getStyleAt(row, col) : style);
                                            }
                                        }
                                    }
                                }
                                break;
                            case 'r': // "${CSI}${TOP}${LEFT}${BOTTOM}${RIGHT}${ATTRIBUTES}$r"
                                // Change attributes in rectangular area (DECCARA -
                                // http://vt100.net/docs/vt510-rm/DECCARA).
                            case 't': // "${CSI}${TOP}${LEFT}${BOTTOM}${RIGHT}${ATTRIBUTES}$t"
                                // Reverse attributes in rectangular area (DECRARA -
                                // http://www.vt100.net/docs/vt510-rm/DECRARA).
                                boolean reverse = b == 't';
                                // FIXME: "coordinates of the rectangular area are affected by the setting of origin
                                // mode (DECOM)".
                                int top = Math.min(getArg(0, 1, true) - 1, effectiveBottomMargin) + effectiveTopMargin;
                                int left = Math.min(getArg(1, 1, true) - 1, effectiveRightMargin) + effectiveLeftMargin;
                                int bottom = Math.min(getArg(2, mrows, true) + 1, effectiveBottomMargin - 1)
                                        + effectiveTopMargin;
                                int right = Math.min(getArg(3, mcolumns, true) + 1, effectiveRightMargin - 1)
                                        + effectiveLeftMargin;
                                if (margindex >= 4) {
                                    if (margindex >= margs.length)
                                    {
                                        margindex = margs.length - 1;
                                    }
                                    for (int i = 4; i <= margindex; i++) {
                                        int bits = 0;
                                        boolean setOrClear = true; // True if setting, false if clearing.
                                        switch (getArg(i, 0, false)) {
                                            case 0: // Attributes off (no bold, no underline, no blink, positive image).
                                                bits = (TextStyle.CHARACTER_ATTRIBUTE_BOLD
                                                        | TextStyle.CHARACTER_ATTRIBUTE_UNDERLINE
                                                        | TextStyle.CHARACTER_ATTRIBUTE_BLINK
                                                        | TextStyle.CHARACTER_ATTRIBUTE_INVERSE);
                                                if (!reverse)
                                                {
                                                    setOrClear = false;
                                                }
                                                break;
                                            case 1: // Bold.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_BOLD;
                                                break;
                                            case 4: // Underline.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_UNDERLINE;
                                                break;
                                            case 5: // Blink.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_BLINK;
                                                break;
                                            case 7: // Negative image.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_INVERSE;
                                                break;
                                            case 22: // No bold.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_BOLD;
                                                setOrClear = false;
                                                break;
                                            case 24: // No underline.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_UNDERLINE;
                                                setOrClear = false;
                                                break;
                                            case 25: // No blink.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_BLINK;
                                                setOrClear = false;
                                                break;
                                            case 27: // Positive image.
                                                bits = TextStyle.CHARACTER_ATTRIBUTE_INVERSE;
                                                setOrClear = false;
                                                break;
                                            default:
                                                break;
                                        }
                                        if (reverse && !setOrClear) {
                                            // Reverse attributes in rectangular area ignores non-(1,4,5,7) bits.
                                        } else {
                                            mscreen.setOrClearEffect(bits, setOrClear, reverse,
                                                    isDecsetInternalBitSet(DECSET_BIT_RECTANGULAR_CHANGEATTRIBUTE),
                                                    effectiveLeftMargin, effectiveRightMargin, top, left, bottom,
                                                    right);
                                        }
                                    }
                                } else {
                                    // Do nothing.
                                }
                                break;
                            default:
                                unknownSequence(b);
                        }
                        break;
                    case ESC_CSI_DOUBLE_QUOTE:
                        if (b == 'q') {
                            // http://www.vt100.net/docs/vt510-rm/DECSCA
                            int arg = getArg0(0);
                            if (arg == 0 || arg == 2) {
                                // DECSED and DECSEL can erase characters.
                                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_PROTECTED;
                            } else if (arg == 1) {
                                // DECSED and DECSEL cannot erase characters.
                                meffect |= TextStyle.CHARACTER_ATTRIBUTE_PROTECTED;
                            } else {
                                unknownSequence(b);
                            }
                        } else {
                            unknownSequence(b);
                        }
                        break;
                    case ESC_CSI_SINGLE_QUOTE:
                        if (b == '}') { // Insert Ps Column(s) (default = 1) (DECIC), VT420 and up.
                            int columnsAfterCursor = mrightmargin - mcursorcol;
                            int columnsToInsert = Math.min(getArg0(1), columnsAfterCursor);
                            int columnsToMove = columnsAfterCursor - columnsToInsert;
                            mscreen.blockCopy(mcursorcol, 0, columnsToMove, mrows, mcursorcol + columnsToInsert, 0);
                            blockClear(mcursorcol, 0, columnsToInsert, mrows);
                        } else if (b == '~') { // Delete Ps Column(s) (default = 1) (DECDC), VT420 and up.
                            int columnsAfterCursor = mrightmargin - mcursorcol;
                            int columnsToDelete = Math.min(getArg0(1), columnsAfterCursor);
                            int columnsToMove = columnsAfterCursor - columnsToDelete;
                            mscreen.blockCopy(mcursorcol + columnsToDelete, 0, columnsToMove, mrows, mcursorcol, 0);
                            blockClear(mcursorrow + columnsToMove, 0, columnsToDelete, mrows);
                        } else {
                            unknownSequence(b);
                        }
                        break;
                    case ESC_PERCENT:
                        break;
                    case ESC_OSC:
                        doOsc(b);
                        break;
                    case ESC_OSC_ESC:
                        doOscEsc(b);
                        break;
                    case ESC_P:
                        doDeviceControl(b);
                        break;
                    case ESC_CSI_QUESTIONMARK_ARG_DOLLAR:
                        if (b == 'p') {
                            // Request DEC private mode (DECRQM).
                            int mode = getArg0(0);
                            int value;
                            if (mode == 47 || mode == 1047 || mode == 1049) {
                                // This state is carried by mscreen pointer.
                                value = (mscreen == maltbuffer) ? 1 : 2;
                            } else {
                                int internalBit = mapDecSetBitToInternalBit(mode);
                                if (internalBit == -1) {
                                    value = isDecsetInternalBitSet(internalBit) ? 1 : 2; // 1=set, 2=reset.
                                } else {
                                    Log.e(EmulatorDebug.LOG_TAG,
                                            "Got DECRQM for unrecognized private DEC mode=" + mode);
                                    value = 0; // 0=not recognized, 3=permanently set, 4=permanently reset
                                }
                            }
                            msession.write(String.format(Locale.US, "\033[?%d;%d$y", mode, value));
                        } else {
                            unknownSequence(b);
                        }
                        break;
                    case ESC_CSI_ARGS_SPACE:
                        int arg = getArg0(0);
                        switch (b) {
                            case 'q': // "${CSI}${STYLE} q" - set cursor style
                                switch (arg) {
                                    case 0: // Blinking block.
                                    case 1: // Blinking block.
                                    case 2: // Steady block.
                                        mcursorstyle = CURSOR_STYLE_BLOCK;
                                        break;
                                    case 3: // Blinking underline.
                                    case 4: // Steady underline.
                                        mcursorstyle = CURSOR_STYLE_UNDERLINE;
                                        break;
                                    case 5: // Blinking bar (xterm addition).
                                    case 6: // Steady bar (xterm addition).
                                        mcursorstyle = CURSOR_STYLE_BAR;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case 't':
                            case 'u':
                                // Set margin-bell volume - ignore.
                                break;
                            default:
                                unknownSequence(b);
                        }
                        break;
                    case ESC_CSI_ARGS_ASTERIX:
                        int attributeChangeExtent = getArg0(0);
                        if (b == 'x' && (attributeChangeExtent >= 0 && attributeChangeExtent <= 2)) {
                            // Select attribute change extent (DECSACE - http://www.vt100.net/docs/vt510-rm/DECSACE).
                            setDecsetinternalBit(DECSET_BIT_RECTANGULAR_CHANGEATTRIBUTE, attributeChangeExtent == 2);
                        } else {
                            unknownSequence(b);
                        }
                        break;
                    default:
                        unknownSequence(b);
                        break;
                }
                if (!mcontinuesequence)
                {
                    mescapestate = ESC_NONE;
                }
                break;
        }
    }
    
    /** When in {@link #ESC_P} ("device control") sequence. */
    private void doDeviceControl(int b) {
        switch (b) {
            case (byte) '\\': // End of ESC \ string Terminator
            {
                String dcs = moscordevicecontrolargs.toString();
                // DCS $ q P t ST. Request Status String (DECRQSS)
                if (dcs.startsWith("$q")) {
                    if (dcs.equals("$q\"p")) {
                        // DECSCL, conformance level, http://www.vt100.net/docs/vt510-rm/DECSCL:
                        String csiString = "64;1\"p";
                        msession.write("\033P1$r" + csiString + "\033\\");
                    } else {
                        finishSequenceAndLogError("Unrecognized DECRQSS string: '" + dcs + "'");
                    }
                } else if (dcs.startsWith("+q")) {
                    // Request Termcap/Terminfo String. The string following the "q" is a list of names encoded in
                    // hexadecimal (2 digits per character) separated by ; which correspond to termcap or terminfo key
                    // names.
                    // Two special features are also recognized, which are not key names: Co for termcap colors (or
                    // colors
                    // for terminfo colors), and TN for termcap name (or name for terminfo name).
                    // xterm responds with DCS 1 + r P t ST for valid requests, adding to P t an = , and the value of
                    // the
                    // corresponding string that xterm would send, or DCS 0 + r P t ST for invalid requests. The strings
                    // are
                    // encoded in hexadecimal (2 digits per character).
                    // Example:
                    // :kr=\EOC: ks=\E[?1h\E=: ku=\EOA: le=^H:mb=\E[5m:md=\E[1m:\
                    // where
                    // kd=down-arrow key
                    // kl=left-arrow key
                    // kr=right-arrow key
                    // ku=up-arrow key
                    // #2=key_shome, "shifted home"
                    // #4=key_sleft, "shift arrow left"
                    // %i=key_sright, "shift arrow right"
                    // *7=key_send, "shifted end"
                    // k1=F1 function key
                    
                    // Example: Request for ku is "ESC P + q 6 b 7 5 ESC \", where 6b7d=ku in hexadecimal.
                    // Xterm response in normal cursor mode:
                    // "<27> P 1 + r 6 b 7 5 = 1 B 5 B 4 1" where 0x1B 0x5B 0x41 = 27 91 65 = ESC [ A
                    // Xterm response in application cursor mode:
                    // "<27> P 1 + r 6 b 7 5 = 1 B 5 B 4 1" where 0x1B 0x4F 0x41 = 27 91 65 = ESC 0 A
                    
                    // #4 is "shift arrow left":
                    // *** Device Control (DCS) for '#4'- 'ESC P + q 23 34 ESC \'
                    // Response: <27> P 1 + r 2 3 3 4 = 1 B 5 B 3 1 3 B 3 2 4 4 <27> \
                    // where 0x1B 0x5B 0x31 0x3B 0x32 0x44 = ESC [ 1 ; 2 D
                    // which we find in: TermKeyListener.java: KEY_MAP.put(KEYMOD_SHIFT | KEYCODE_DPAD_LEFT,
                    // "\033[1;2D");
                    
                    // See http://h30097.www3.hp.com/docs/base_doc/DOCUMENTATION/V40G_HTML/MAN/MAN4/0178____.HTM for
                    // what to
                    // respond, as well as http://www.freebsd.org/cgi/man.cgi?query=termcap&sektion=5#CAPABILITIES for
                    // the meaning of e.g. "ku", "kd", "kr", "kl"
                    
                    for (String part : dcs.substring(2).split(";")) {
                        if (part.length() % 2 == 0) {
                            StringBuilder transBuffer = new StringBuilder();
                            for (int i = 0; i < part.length(); i += 2) {
                                char c = (char) Long.decode("0x" + part.charAt(i) + "" + part.charAt(i + 1))
                                        .longValue();
                                transBuffer.append(c);
                            }
                            String trans = transBuffer.toString();
                            String responseValue;
                            switch (trans) {
                                case "Co":
                                case "colors":
                                    responseValue = "256"; // Number of colors.
                                    break;
                                case "TN":
                                case "name":
                                    responseValue = "xterm";
                                    break;
                                default:
                                    responseValue = KeyHandler.getCodeFromTermcap(trans,
                                            isDecsetInternalBitSet(DECSET_BIT_APPLICATION_CURSOR_KEYS),
                                            isDecsetInternalBitSet(DECSET_BIT_APPLICATION_KEYPAD));
                                    break;
                            }
                            if (responseValue == null) {
                                switch (trans) {
                                    case "%1": // Help key - ignore
                                    case "&8": // Undo key - ignore.
                                        break;
                                    default:
                                        Log.w(EmulatorDebug.LOG_TAG,
                                                "Unhandled termcap/terminfo name: '" + trans + "'");
                                }
                                // Respond with invalid request:
                                msession.write("\033P0+r" + part + "\033\\");
                            } else {
                                StringBuilder hexEncoded = new StringBuilder();
                                for (int j = 0; j < responseValue.length(); j++) {
                                    hexEncoded.append(String.format("%02X", (int) responseValue.charAt(j)));
                                }
                                msession.write("\033P1+r" + part + "=" + hexEncoded + "\033\\");
                            }
                        } else {
                            Log.e(EmulatorDebug.LOG_TAG, "Invalid device termcap/terminfo name of odd length: " + part);
                        }
                    }
                } else {
                    if (LOG_ESCAPE_SEQUENCES)
                    {
                        Log.e(EmulatorDebug.LOG_TAG, "Unrecognized device control string: " + dcs);
                    }
                }
                finishSequence();
            }
                break;
            default:
                if (moscordevicecontrolargs.length() > MAX_OSC_STRING_LENGTH) {
                    // Too long.
                    moscordevicecontrolargs.setLength(0);
                    finishSequence();
                } else {
                    moscordevicecontrolargs.appendCodePoint(b);
                    continueSequence(mescapestate);
                }
        }
    }
    
    private int nextTabStop(int numTabs) {
        for (int i = mcursorcol + 1; i < mcolumns; i++)
        {
            if (mtabstop[i] && --numTabs == 0)
            {
                return Math.min(i, mrightmargin);
            }
        }
        return mrightmargin - 1;
    }
    
    /** Process byte while in the {@link #ESC_CSI_QUESTIONMARK} escape state. */
    private void doCsiQuestionMark(int b) {
        switch (b) {
            case 'J': // Selective erase in display (DECSED) - http://www.vt100.net/docs/vt510-rm/DECSED.
            case 'K': // Selective erase in line (DECSEL) - http://vt100.net/docs/vt510-rm/DECSEL.
                mabouttoautowrap = false;
                int fillChar = ' ';
                int startCol = -1;
                int startRow = -1;
                int endCol = -1;
                int endRow = -1;
                boolean justRow = (b == 'K');
                switch (getArg0(0)) {
                    case 0: // Erase from the active position to the end, inclusive (default).
                        startCol = mcursorcol;
                        startRow = mcursorrow;
                        endCol = mcolumns;
                        endRow = justRow ? (mcursorrow + 1) : mrows;
                        break;
                    case 1: // Erase from start to the active position, inclusive.
                        startCol = 0;
                        startRow = justRow ? mcursorrow : 0;
                        endCol = mcursorcol + 1;
                        endRow = mcursorrow + 1;
                        break;
                    case 2: // Erase all of the display/line.
                        startCol = 0;
                        startRow = justRow ? mcursorrow : 0;
                        endCol = mcolumns;
                        endRow = justRow ? (mcursorrow + 1) : mrows;
                        break;
                    default:
                        unknownSequence(b);
                        break;
                }
                long style = getStyle();
                for (int row = startRow; row < endRow; row++) {
                    for (int col = startCol; col < endCol; col++) {
                        if ((TextStyle.decodeEffect(mscreen.getStyleAt(row, col))
                                & TextStyle.CHARACTER_ATTRIBUTE_PROTECTED) == 0)
                        {
                            mscreen.setChar(col, row, fillChar, style);
                        }
                    }
                }
                break;
            case 'h':
            case 'l':
                if (margindex >= margs.length)
                {
                    margindex = margs.length - 1;
                }
                for (int i = 0; i <= margindex; i++)
                {
                    doDecSetOrReset(b == 'h', margs[i]);
                }
                break;
            case 'n': // Device Status Report (DSR, DEC-specific).
                switch (getArg0(-1)) {
                    case 6:
                        // Extended Cursor Position (DECXCPR - http://www.vt100.net/docs/vt510-rm/DECXCPR). Page=1.
                        msession.write(String.format(Locale.US, "\033[?%d;%d;1R", mcursorrow + 1, mcursorcol + 1));
                        break;
                    default:
                        finishSequence();
                        return;
                }
                break;
            case 'r':
            case 's':
                if (margindex >= margs.length)
                {
                    margindex = margs.length - 1;
                }
                for (int i = 0; i <= margindex; i++) {
                    int externalBit = margs[i];
                    int internalBit = mapDecSetBitToInternalBit(externalBit);
                    if (internalBit == -1) {
                        Log.w(EmulatorDebug.LOG_TAG, "Ignoring request to save/recall decset bit=" + externalBit);
                    } else {
                        if (b == 's') {
                            msaveddecsetflags |= internalBit;
                        } else {
                            doDecSetOrReset((msaveddecsetflags & internalBit) != 0, externalBit);
                        }
                    }
                }
                break;
            case '$':
                continueSequence(ESC_CSI_QUESTIONMARK_ARG_DOLLAR);
                return;
            default:
                parseArg(b);
        }
    }
    
    public void doDecSetOrReset(boolean setting, int externalBit) {
        int internalBit = mapDecSetBitToInternalBit(externalBit);
        if (internalBit != -1) {
            setDecsetinternalBit(internalBit, setting);
        }
        switch (externalBit) {
            case 1: // Application Cursor Keys (DECCKM).
                break;
            case 3: // Set: 132 column mode (. Reset: 80 column mode. ANSI name: DECCOLM.
                // We don't actually set/reset 132 cols, but we do want the side effects
                // (FIXME: Should only do this if the 95 DECSET bit (DECNCSM) is set, and if changing value?):
                // Sets the left, right, top and bottom scrolling margins to their default positions, which is important
                // for
                // the "reset" utility to really reset the terminal:
                mleftmargin = mtopmargin = 0;
                mbottommargin = mrows;
                mrightmargin = mcolumns;
                // "DECCOLM resets vertical split screen mode (DECLRMM) to unavailable":
                setDecsetinternalBit(DECSET_BIT_LEFTRIGHT_MARGIN_MODE, false);
                // "Erases all data in page memory":
                blockClear(0, 0, mcolumns, mrows);
                setCursorRowCol(0, 0);
                break;
            case 4: // DECSCLM-Scrolling Mode. Ignore.
                break;
            case 5: // Reverse video. No action.
                break;
            case 6: // Set: Origin Mode. Reset: Normal Cursor Mode. Ansi name: DECOM.
                if (setting)
                {
                    setCursorPosition(0, 0);
                }
                break;
            case 7: // Wrap-around bit, not specific action.
            case 8: // Auto-repeat Keys (DECARM). Do not implement.
            case 9: // X10 mouse reporting - outdated. Do not implement.
            case 12: // Control cursor blinking - ignore.
            case 25: // Hide/show cursor - no action needed, renderer will check with isShowingCursor().
            case 40: // Allow 80 => 132 Mode, ignore.
            case 45: // TODO: Reverse wrap-around. Implement???
            case 66: // Application keypad (DECNKM).
                break;
            case 69: // Left and right margin mode (DECLRMM).
                if (!setting) {
                    mleftmargin = 0;
                    mrightmargin = mcolumns;
                }
                break;
            case 1000:
            case 1001:
            case 1002:
            case 1003:
            case 1004:
            case 1005: // UTF-8 mouse mode, ignore.
            case 1006: // SGR Mouse Mode
            case 1015:
            case 1034: // Interpret "meta" key, sets eighth bit.
                break;
            case 1048: // Set: Save cursor as in DECSC. Reset: Restore cursor as in DECRC.
                if (setting)
                {
                    saveCursor();
                }
                else
                {
                    restoreCursor();
                }
                break;
            case 47:
            case 1047:
            case 1049: {
                // Set: Save cursor as in DECSC and use Alternate Screen Buffer, clearing it first.
                // Reset: Use Normal Screen Buffer and restore cursor as in DECRC.
                TerminalBuffer newScreen = setting ? maltbuffer : mmainbuffer;
                if (newScreen != mscreen) {
                    boolean resized = !(newScreen.mcolumns == mcolumns && newScreen.screenRows == mrows);
                    if (setting)
                    {
                        saveCursor();
                    }
                    mscreen = newScreen;
                    if (!setting) {
                        int col = msavedstatemain.msavedcursorcol;
                        int row = msavedstatemain.msavedcursorrow;
                        restoreCursor();
                        if (resized) {
                            // Restore cursor position _not_ clipped to current screen (let resizeScreen() handle that):
                            mcursorcol = col;
                            mcursorrow = row;
                        }
                    }
                    // Check if buffer size needs to be updated:
                    if (resized)
                    {
                        resizeScreen();
                    }
                    // Clear new screen if alt buffer:
                    if (newScreen == maltbuffer)
                    {
                        newScreen.blockSet(0, 0, mcolumns, mrows, ' ', getStyle());
                    }
                }
                break;
            }
            case 2004:
                // Bracketed paste mode - setting bit is enough.
                break;
            default:
                unknownParameter(externalBit);
                break;
        }
    }
    
    private void doCsiBiggerThan(int b) {
        switch (b) {
            case 'c': // "${CSI}>c" or "${CSI}>c". Secondary Device Attributes (DA2).
                // Originally this was used for the terminal to respond with "identification code, firmware version
                // level,
                // and hardware options" (http://vt100.net/docs/vt510-rm/DA2), with the first "41" meaning the VT420
                // terminal type. This is not used anymore, but the second version level field has been changed by xterm
                // to mean it's release number ("patch numbers" listed at
                // http://invisible-island.net/xterm/xterm.log.html),
                // and some applications use it as a feature check:
                // * tmux used to have a "xterm won't reach version 500 for a while so set that as the upper limit"
                // check,
                // and then check "xterm_version > 270" if rectangular area operations such as DECCRA could be used.
                // * vim checks xterm version number >140 for "Request termcap/terminfo string" functionality >276 for
                // SGR
                // mouse report.
                // The third number is a keyboard identifier not used nowadays.
                msession.write("\033[>41;320;0c");
                break;
            case 'm':
                // https://bugs.launchpad.net/gnome-terminal/+bug/96676/comments/25
                // Depending on the first number parameter, this can set one of the xterm resources
                // modifyKeyboard, modifyCursorKeys, modifyFunctionKeys and modifyOtherKeys.
                // http://invisible-island.net/xterm/manpage/xterm.html#RESOURCES
                
                // * modifyKeyboard (parameter=1):
                // Normally xterm makes a special case regarding modifiers (shift, control, etc.) to handle special
                // keyboard
                // layouts (legacy and vt220). This is done to provide compatible keyboards for DEC VT220 and related
                // terminals that implement user-defined keys (UDK).
                // The bits of the resource value selectively enable modification of the given category when these
                // keyboards
                // are selected. The default is "0":
                // (0) The legacy/vt220 keyboards interpret only the Control-modifier when constructing numbered
                // function-keys. Other special keys are not modified.
                // (1) allows modification of the numeric keypad
                // (2) allows modification of the editing keypad
                // (4) allows modification of function-keys, overrides use of Shift-modifier for UDK.
                // (8) allows modification of other special keys
                
                // * modifyCursorKeys (parameter=2):
                // Tells how to handle the special case where Control-, Shift-, Alt- or Meta-modifiers are used to add a
                // parameter to the escape sequence returned by a cursor-key. The default is "2".
                // - Set it to -1 to disable it.
                // - Set it to 0 to use the old/obsolete behavior.
                // - Set it to 1 to prefix modified sequences with CSI.
                // - Set it to 2 to force the modifier to be the second parameter if it would otherwise be the first.
                // - Set it to 3 to mark the sequence with a ">" to hint that it is private.
                
                // * modifyFunctionKeys (parameter=3):
                // Tells how to handle the special case where Control-, Shift-, Alt- or Meta-modifiers are used to add a
                // parameter to the escape sequence returned by a (numbered) function-
                // key. The default is "2". The resource values are similar to modifyCursorKeys:
                // Set it to -1 to permit the user to use shift- and control-modifiers to construct function-key strings
                // using the normal encoding scheme.
                // - Set it to 0 to use the old/obsolete behavior.
                // - Set it to 1 to prefix modified sequences with CSI.
                // - Set it to 2 to force the modifier to be the second parameter if it would otherwise be the first.
                // - Set it to 3 to mark the sequence with a ">" to hint that it is private.
                // If modifyFunctionKeys is zero, xterm uses Control- and Shift-modifiers to allow the user to construct
                // numbered function-keys beyond the set provided by the keyboard:
                // (Control) adds the value given by the ctrlFKeys resource.
                // (Shift) adds twice the value given by the ctrlFKeys resource.
                // (Control/Shift) adds three times the value given by the ctrlFKeys resource.
                //
                // As a special case, legacy (when oldFunctionKeys is true) or vt220 (when sunKeyboard is true)
                // keyboards interpret only the Control-modifier when constructing numbered function-keys.
                // This is done to provide compatible keyboards for DEC VT220 and related terminals that
                // implement user-defined keys (UDK).
                
                // * modifyOtherKeys (parameter=4):
                // Like modifyCursorKeys, tells xterm to construct an escape sequence for other keys (such as "2") when
                // modified by Control-, Alt- or Meta-modifiers. This feature does not apply to function keys and
                // well-defined keys such as ESC or the control keys. The default is "0".
                // (0) disables this feature.
                // (1) enables this feature for keys except for those with well-known behavior, e.g., Tab, Backarrow and
                // some special control character cases, e.g., Control-Space to make a NUL.
                // (2) enables this feature for keys including the exceptions listed.
                Log.e(EmulatorDebug.LOG_TAG, "(ignored) CSI > MODIFY RESOURCE: " + getArg0(-1) + " to " + getArg1(-1));
                break;
            default:
                parseArg(b);
                break;
        }
    }
    
    private void startEscapeSequence() {
        mescapestate = ESC;
        margindex = 0;
        Arrays.fill(margs, -1);
    }
    
    private void doLinefeed() {
        boolean belowScrollingRegion = mcursorrow >= mbottommargin;
        int newCursorRow = mcursorrow + 1;
        if (belowScrollingRegion) {
            // Move down (but not scroll) as long as we are above the last row.
            if (mcursorrow != mrows - 1) {
                setCursorRow(newCursorRow);
            }
        } else {
            if (newCursorRow == mbottommargin) {
                scrollDownOneLine();
                newCursorRow = mbottommargin - 1;
            }
            setCursorRow(newCursorRow);
        }
    }
    
    private void continueSequence(int state) {
        mescapestate = state;
        mcontinuesequence = true;
    }
    
    private void doEscPound(int b) {
        switch (b) {
            case '8': // Esc # 8 - DEC screen alignment test - fill screen with E's.
                mscreen.blockSet(0, 0, mcolumns, mrows, 'E', getStyle());
                break;
            default:
                unknownSequence(b);
                break;
        }
    }
    
    /** Encountering a character in the {@link #ESC} state. */
    private void doEsc(int b) {
        switch (b) {
            case '#':
                continueSequence(ESC_POUND);
                break;
            case '(':
                continueSequence(ESC_SELECT_LEFT_PAREN);
                break;
            case ')':
                continueSequence(ESC_SELECT_RIGHT_PAREN);
                break;
            case '6': // Back index (http://www.vt100.net/docs/vt510-rm/DECBI). Move left, insert blank column if start.
                if (mcursorcol > mleftmargin) {
                    mcursorcol--;
                } else {
                    int rows = mbottommargin - mtopmargin;
                    mscreen.blockCopy(mleftmargin, mtopmargin, mrightmargin - mleftmargin - 1, rows, mleftmargin + 1,
                        mtopmargin);
                    mscreen.blockSet(mleftmargin, mtopmargin, 1, rows, ' ',
                            TextStyle.encode(mforecolor, mbackcolor, 0));
                }
                break;
            case '7': // DECSC save cursor - http://www.vt100.net/docs/vt510-rm/DECSC
                saveCursor();
                break;
            case '8': // DECRC restore cursor - http://www.vt100.net/docs/vt510-rm/DECRC
                restoreCursor();
                break;
            case '9': // Forward Index (http://www.vt100.net/docs/vt510-rm/DECFI). Move right, insert blank column if
                if (mcursorcol < mrightmargin - 1) {
                    mcursorcol++;
                } else {
                    int rows = mbottommargin - mtopmargin;
                    mscreen.blockCopy(mleftmargin + 1, mtopmargin, mrightmargin - mleftmargin - 1, rows, mleftmargin,
                        mtopmargin);
                    mscreen.blockSet(mrightmargin - 1, mtopmargin, 1, rows, ' ',
                            TextStyle.encode(mforecolor, mbackcolor, 0));
                }
                break;
            case 'c': // RIS - Reset to Initial State (http://vt100.net/docs/vt510-rm/RIS).
                reset();
                blockClear(0, 0, mcolumns, mrows);
                setCursorPosition(0, 0);
                break;
            case 'D': // INDEX
                doLinefeed();
                break;
            case 'E': // Next line (http://www.vt100.net/docs/vt510-rm/NEL).
                setCursorCol(isDecsetInternalBitSet(DECSET_BIT_ORIGIN_MODE) ? mleftmargin : 0);
                doLinefeed();
                break;
            case 'F': // Cursor to lower-left corner of screen
                setCursorRowCol(0, mbottommargin - 1);
                break;
            case 'H': // Tab set
                mtabstop[mcursorcol] = true;
                break;
            case 'M': // "${ESC}M" - reverse index (RI).
                // http://www.vt100.net/docs/vt100-ug/chapter3.html: "Move the active position to the same horizontal
                // position on the preceding line. If the active position is at the top margin, a scroll down is
                // performed".
                if (mcursorrow <= mtopmargin) {
                    mscreen.blockCopy(0, mtopmargin, mcolumns, mbottommargin - (mtopmargin + 1), 0, mtopmargin + 1);
                    blockClear(0, mtopmargin, mcolumns);
                } else {
                    mcursorrow--;
                }
                break;
            case 'N': // SS2, ignore.
            case '0': // SS3, ignore.
                break;
            case 'P': // Device control string
                moscordevicecontrolargs.setLength(0);
                continueSequence(ESC_P);
                break;
            case '[':
                continueSequence(ESC_CSI);
                break;
            case '=': // DECKPAM
                setDecsetinternalBit(DECSET_BIT_APPLICATION_KEYPAD, true);
                break;
            case ']': // OSC
                moscordevicecontrolargs.setLength(0);
                continueSequence(ESC_OSC);
                break;
            case '>': // DECKPNM
                setDecsetinternalBit(DECSET_BIT_APPLICATION_KEYPAD, false);
                break;
            default:
                unknownSequence(b);
                break;
        }
    }
    
    /** DECSC save cursor - http://www.vt100.net/docs/vt510-rm/DECSC . See {@link #restoreCursor()}. */
    private void saveCursor() {
        SavedScreenState state = (mscreen == mmainbuffer) ? msavedstatemain : msavedstatealt;
        state.msavedcursorrow = mcursorrow;
        state.msavedcursorcol = mcursorcol;
        state.msavedeffect = meffect;
        state.msavedforecolor = mforecolor;
        state.msavedbackcolor = mbackcolor;
        state.msaveddecflags = mcurrentdecsetflags;
        state.muselinedrawingg0 = muselinedrawingg0;
        state.muselinedrawingg1 = muselinedrawingg1;
        state.muselinedrawingusesg0 = muselinedrawingusesg0;
    }
    
    /** DECRS restore cursor - http://www.vt100.net/docs/vt510-rm/DECRC. See {@link #saveCursor()}. */
    private void restoreCursor() {
        SavedScreenState state = (mscreen == mmainbuffer) ? msavedstatemain : msavedstatealt;
        setCursorRowCol(state.msavedcursorrow, state.msavedcursorcol);
        meffect = state.msavedeffect;
        mforecolor = state.msavedforecolor;
        mbackcolor = state.msavedbackcolor;
        int mask = (DECSET_BIT_AUTOWRAP | DECSET_BIT_ORIGIN_MODE);
        mcurrentdecsetflags = (mcurrentdecsetflags & ~mask) | (state.msaveddecflags & mask);
        muselinedrawingg0 = state.muselinedrawingg0;
        muselinedrawingg1 = state.muselinedrawingg1;
        muselinedrawingusesg0 = state.muselinedrawingusesg0;
    }
    
    /** Following a CSI - Control Sequence Introducer, "\033[". {@link #ESC_CSI}. */
    private void doCsi(int b) {
        switch (b) {
            case '!':
                continueSequence(ESC_CSI_EXCLAMATION);
                break;
            case '"':
                continueSequence(ESC_CSI_DOUBLE_QUOTE);
                break;
            case '\'':
                continueSequence(ESC_CSI_SINGLE_QUOTE);
                break;
            case '$':
                continueSequence(ESC_CSI_DOLLAR);
                break;
            case '*':
                continueSequence(ESC_CSI_ARGS_ASTERIX);
                break;
            case '@': {
                // "CSI{n}@" - Insert ${n} space characters (ICH) - http://www.vt100.net/docs/vt510-rm/ICH.
                mabouttoautowrap = false;
                int columnsAfterCursor = mcolumns - mcursorcol;
                int spacesToInsert = Math.min(getArg0(1), columnsAfterCursor);
                int charsToMove = columnsAfterCursor - spacesToInsert;
                mscreen.blockCopy(mcursorcol, mcursorrow, charsToMove, 1, mcursorcol + spacesToInsert, mcursorrow);
                blockClear(mcursorcol, mcursorrow, spacesToInsert);
            }
                break;
            case 'A': // "CSI${n}A" - Cursor up (CUU) ${n} rows.
                setCursorRow(Math.max(mtopmargin, mcursorrow - getArg0(1)));
                break;
            case 'B': // "CSI${n}B" - Cursor down (CUD) ${n} rows.
                setCursorRow(Math.min(mbottommargin - 1, mcursorrow + getArg0(1)));
                break;
            case 'C': // "CSI${n}C" - Cursor forward (CUF).
            case 'a': // "CSI${n}a" - Horizontal position relative (HPR). From ISO-6428/ECMA-48.
                setCursorCol(Math.min(mrightmargin - 1, mcursorcol + getArg0(1)));
                break;
            case 'D': // "CSI${n}D" - Cursor backward (CUB) ${n} columns.
                setCursorCol(Math.max(mleftmargin, mcursorcol - getArg0(1)));
                break;
            case 'E': // "CSI{n}E - Cursor Next Line (CNL). From ISO-6428/ECMA-48.
                setCursorPosition(0, mcursorrow + getArg0(1));
                break;
            case 'F': // "CSI{n}F - Cursor Previous Line (CPL). From ISO-6428/ECMA-48.
                setCursorPosition(0, mcursorrow - getArg0(1));
                break;
            case 'G': // "CSI${n}G" - Cursor horizontal absolute (CHA) to column ${n}.
                setCursorCol(Math.min(Math.max(1, getArg0(1)), mcolumns) - 1);
                break;
            case 'H': // "${CSI}${ROW};${COLUMN}H" - Cursor position (CUP).
            case 'f': // "${CSI}${ROW};${COLUMN}f" - Horizontal and Vertical Position (HVP).
                setCursorPosition(getArg1(1) - 1, getArg0(1) - 1);
                break;
            case 'I': // Cursor Horizontal Forward Tabulation (CHT). Move the active position n tabs forward.
                setCursorCol(nextTabStop(getArg0(1)));
                break;
            case 'J': // "${CSI}${0,1,2,3}J" - Erase in Display (ED)
                // ED ignores the scrolling margins.
                switch (getArg0(0)) {
                    case 0: // Erase from the active position to the end of the screen, inclusive (default).
                        blockClear(mcursorcol, mcursorrow, mcolumns - mcursorcol);
                        blockClear(0, mcursorrow + 1, mcolumns, mrows - (mcursorrow + 1));
                        break;
                    case 1: // Erase from start of the screen to the active position, inclusive.
                        blockClear(0, 0, mcolumns, mcursorrow);
                        blockClear(0, mcursorrow, mcursorcol + 1);
                        break;
                    case 2: // Erase all of the display - all lines are erased, changed to single-width, and the cursor
                        // move..
                        blockClear(0, 0, mcolumns, mrows);
                        break;
                    case 3: // Delete all lines saved in the scrollback buffer (xterm etc)
                        mmainbuffer.clearTranscript();
                        break;
                    default:
                        unknownSequence(b);
                        return;
                }
                mabouttoautowrap = false;
                break;
            case 'K': // "CSI{n}K" - Erase in line (EL).
                switch (getArg0(0)) {
                    case 0: // Erase from the cursor to the end of the line, inclusive (default)
                        blockClear(mcursorcol, mcursorrow, mcolumns - mcursorcol);
                        break;
                    case 1: // Erase from the start of the screen to the cursor, inclusive.
                        blockClear(0, mcursorrow, mcursorcol + 1);
                        break;
                    case 2: // Erase all of the line.
                        blockClear(0, mcursorrow, mcolumns);
                        break;
                    default:
                        unknownSequence(b);
                        return;
                }
                mabouttoautowrap = false;
                break;
            case 'L': // "${CSI}{N}L" - insert ${N} lines (IL).
            {
                int linesAfterCursor = mbottommargin - mcursorrow;
                int linesToInsert = Math.min(getArg0(1), linesAfterCursor);
                int linesToMove = linesAfterCursor - linesToInsert;
                mscreen.blockCopy(0, mcursorrow, mcolumns, linesToMove, 0, mcursorrow + linesToInsert);
                blockClear(0, mcursorrow, mcolumns, linesToInsert);
            }
                break;
            case 'M': // "${CSI}${N}M" - delete N lines (DL).
            {
                mabouttoautowrap = false;
                int linesAfterCursor = mbottommargin - mcursorrow;
                int linesToDelete = Math.min(getArg0(1), linesAfterCursor);
                int linesToMove = linesAfterCursor - linesToDelete;
                mscreen.blockCopy(0, mcursorrow + linesToDelete, mcolumns, linesToMove, 0, mcursorrow);
                blockClear(0, mcursorrow + linesToMove, mcolumns, linesToDelete);
            }
                break;
            case 'P': // "${CSI}{N}P" - delete ${N} characters (DCH).
            {
                // http://www.vt100.net/docs/vt510-rm/DCH: "If ${N} is greater than the number of characters between the
                // cursor and the right margin, then DCH only deletes the remaining characters.
                // As characters are deleted, the remaining characters between the cursor and right margin move to the
                // left.
                // Character attributes move with the characters. The terminal adds blank spaces with no visual
                // character
                // attributes at the right margin. DCH has no effect outside the scrolling margins."
                mabouttoautowrap = false;
                int cellsAfterCursor = mcolumns - mcursorcol;
                int cellsToDelete = Math.min(getArg0(1), cellsAfterCursor);
                int cellsToMove = cellsAfterCursor - cellsToDelete;
                mscreen.blockCopy(mcursorcol + cellsToDelete, mcursorrow, cellsToMove, 1, mcursorcol, mcursorrow);
                blockClear(mcursorcol + cellsToMove, mcursorrow, cellsToDelete);
            }
                break;
            case 'S': { // "${CSI}${N}S" - scroll up ${N} lines (default = 1) (SU).
                final int linesToScroll = getArg0(1);
                for (int i = 0; i < linesToScroll; i++)
                {
                    scrollDownOneLine();
                }
                break;
            }
            case 'T':
                if (margindex == 0) {
                    // "${CSI}${N}T" - Scroll down N lines (default = 1) (SD).
                    // http://vt100.net/docs/vt510-rm/SD: "N is the number of lines to move the user window up in page
                    // memory. N new lines appear at the top of the display. N old lines disappear at the bottom of the
                    // display. You cannot pan past the top margin of the current page".
                    final int linesToScrollArg = getArg0(1);
                    final int linesBetweenTopAndBottomMargins = mbottommargin - mtopmargin;
                    final int linesToScroll = Math.min(linesBetweenTopAndBottomMargins, linesToScrollArg);
                    mscreen.blockCopy(0, mtopmargin, mcolumns, linesBetweenTopAndBottomMargins - linesToScroll, 0,
                            mtopmargin + linesToScroll);
                    blockClear(0, mtopmargin, mcolumns, linesToScroll);
                } else {
                    // "${CSI}${func};${startx};${starty};${firstrow};${lastrow}T" - initiate highlight mouse tracking.
                    unimplementedSequence(b);
                }
                break;
            case 'X': // "${CSI}${N}X" - Erase ${N:=1} character(s) (ECH). FIXME: Clears character attributes?
                mabouttoautowrap = false;
                mscreen.blockSet(mcursorcol, mcursorrow, Math.min(getArg0(1), mcolumns - mcursorcol), 1, ' ',
                        getStyle());
                break;
            case 'Z': // Cursor Backward Tabulation (CBT). Move the active position n tabs backward.
                int numberOfTabs = getArg0(1);
                int newCol = mleftmargin;
                for (int i = mcursorcol - 1; i >= 0; i--)
                {
                    if (mtabstop[i]) {
                        if (--numberOfTabs == 0) {
                            newCol = Math.max(i, mleftmargin);
                            break;
                        }
                    }
                }
                mcursorcol = newCol;
                break;
            case '?': // Esc [ ? -- start of a private mode set
                continueSequence(ESC_CSI_QUESTIONMARK);
                break;
            case '>': // "Esc [ >" --
                continueSequence(ESC_CSI_BIGGERTHAN);
                break;
            case '`': // Horizontal position absolute (HPA - http://www.vt100.net/docs/vt510-rm/HPA).
                setCursorColRespectingOriginMode(getArg0(1) - 1);
                break;
            case 'b': // Repeat the preceding graphic character Ps times (REP).
                if (mlastemittedcodepoint == -1)
                {
                    break;
                }
                final int numRepeat = getArg0(1);
                for (int i = 0; i < numRepeat; i++)
                {
                    emitCodePoint(mlastemittedcodepoint);
                }
                break;
            case 'c': // Primary Device Attributes (http://www.vt100.net/docs/vt510-rm/DA1) if argument is missing or
                // The important part that may still be used by some (tmux stores this value but does not currently use
                // it)
                // is the first response parameter identifying the terminal service class, where we send 64 for "vt420".
                // This is followed by a list of attributes which is probably unused by applications. Send like xterm.
                if (getArg0(0) == 0)
                {
                    msession.write("\033[?64;1;2;6;9;15;18;21;22c");
                }
                break;
            case 'd': // ESC [ Pn d - Vert Position Absolute
                setCursorRow(Math.min(Math.max(1, getArg0(1)), mrows) - 1);
                break;
            case 'e': // Vertical Position Relative (VPR). From ISO-6429 (ECMA-48).
                setCursorPosition(mcursorcol, mcursorrow + getArg0(1));
                break;
            // case 'f': "${CSI}${ROW};${COLUMN}f" - Horizontal and Vertical Position (HVP). Grouped with case 'H'.
            case 'g': // Clear tab stop
                switch (getArg0(0)) {
                    case 0:
                        mtabstop[mcursorcol] = false;
                        break;
                    case 3:
                        for (int i = 0; i < mcolumns; i++) {
                            mtabstop[i] = false;
                        }
                        break;
                    default:
                        // Specified to have no effect.
                        break;
                }
                break;
            case 'h': // Set Mode
                doSetMode(true);
                break;
            case 'l': // Reset Mode
                doSetMode(false);
                break;
            case 'm': // Esc [ Pn m - character attributes. (can have up to 16 numerical arguments)
                selectGraphicRendition();
                break;
            case 'n': // Esc [ Pn n - ECMA-48 Status Report Commands
                // sendDeviceAttributes()
                switch (getArg0(0)) {
                    case 5: // Device status report (DSR):
                        // Answer is ESC [ 0 n (Terminal OK).
                        byte[] dsr = { (byte) 27, (byte) '[', (byte) '0', (byte) 'n' };
                        msession.write(dsr, 0, dsr.length);
                        break;
                    case 6: // Cursor position report (CPR):
                        // Answer is ESC [ y ; x R, where x,y is
                        // the cursor location.
                        msession.write(String.format(Locale.US, "\033[%d;%dR", mcursorrow + 1, mcursorcol + 1));
                        break;
                    default:
                        break;
                }
                break;
            case 'r': // "CSI${top};${bottom}r" - set top and bottom Margins (DECSTBM).
            {
                // https://vt100.net/docs/vt510-rm/DECSTBM.html
                // The top margin defaults to 1, the bottom margin defaults to mrows.
                // The escape sequence numbers top 1..23, but we number top 0..22.
                // The escape sequence numbers bottom 2..24, and so do we (because we use a zero based numbering
                // scheme, but we store the first line below the bottom-most scrolling line.
                // As a result, we adjust the top line by -1, but we leave the bottom line alone.
                // Also require that top + 2 <= bottom.
                mtopmargin = Math.max(0, Math.min(getArg0(1) - 1, mrows - 2));
                mbottommargin = Math.max(mtopmargin + 2, Math.min(getArg1(mrows), mrows));
                
                // DECSTBM moves the cursor to column 1, line 1 of the page respecting origin mode.
                setCursorPosition(0, 0);
            }
                break;
            case 's':
                if (isDecsetInternalBitSet(DECSET_BIT_LEFTRIGHT_MARGIN_MODE)) {
                    // Set left and right margins (DECSLRM - http://www.vt100.net/docs/vt510-rm/DECSLRM).
                    mleftmargin = Math.min(getArg0(1) - 1, mcolumns - 2);
                    mrightmargin = Math.max(mleftmargin + 1, Math.min(getArg1(mcolumns), mcolumns));
                    // DECSLRM moves the cursor to column 1, line 1 of the page.
                    setCursorPosition(0, 0);
                } else {
                    // Save cursor (ANSI.SYS), available only when DECLRMM is disabled.
                    saveCursor();
                }
                break;
            case 't': // Window manipulation (from dtterm, as well as extensions)
                switch (getArg0(0)) {
                    case 11: // Report xterm window state. If the xterm window is open (non-iconified), it returns CSI 1
                        msession.write("\033[1t");
                        break;
                    case 13: // Report xterm window position. Result is CSI 3 ; x ; y t
                        msession.write("\033[3;0;0t");
                        break;
                    case 14: // Report xterm window in pixels. Result is CSI 4 ; height ; width t
                        // We just report characters time 12 here.
                        msession.write(String.format(Locale.US, "\033[4;%d;%dt", mrows * 12, mcolumns * 12));
                        break;
                    case 18: // Report the size of the text area in characters. Result is CSI 8 ; height ; width t
                        msession.write(String.format(Locale.US, "\033[8;%d;%dt", mrows, mcolumns));
                        break;
                    case 19: // Report the size of the screen in characters. Result is CSI 9 ; height ; width t
                        // We report the same size as the view, since it's the view really isn't resizable from the
                        // shell.
                        msession.write(String.format(Locale.US, "\033[9;%d;%dt", mrows, mcolumns));
                        break;
                    case 20: // Report xterm windows icon label. Result is OSC L label ST. Disabled due to security
                        msession.write("\033]LIconLabel\033\\");
                        break;
                    case 21: // Report xterm windows title. Result is OSC l label ST. Disabled due to security concerns:
                        msession.write("\033]l\033\\");
                        break;
                    case 22:
                        // 22;0 -> Save xterm icon and window title on stack.
                        // 22;1 -> Save xterm icon title on stack.
                        // 22;2 -> Save xterm window title on stack.
                        mtitlestack.push(mtitle);
                        if (mtitlestack.size() > 20) {
                            // Limit size
                            mtitlestack.remove(0);
                        }
                        break;
                    case 23: // Like 22 above but restore from stack.
                        if (!mtitlestack.isEmpty())
                        {
                            setTitle(mtitlestack.pop());
                        }
                        break;
                    default:
                        // Ignore window manipulation.
                        break;
                }
                break;
            case 'u': // Restore cursor (ANSI.SYS).
                restoreCursor();
                break;
            case ' ':
                continueSequence(ESC_CSI_ARGS_SPACE);
                break;
            default:
                parseArg(b);
                break;
        }
    }
    
    /** Select Graphic Rendition (SGR) - see http://en.wikipedia.org/wiki/ANSI_escape_code#graphics. */
    private void selectGraphicRendition() {
        if (margindex >= margs.length)
        {
            margindex = margs.length - 1;
        }
        for (int i = 0; i <= margindex; i++) {
            int code = margs[i];
            if (code < 0) {
                if (margindex > 0) {
                    continue;
                } else {
                    code = 0;
                }
            }
            if (code == 0) { // reset
                mforecolor = TextStyle.COLOR_INDEX_FOREGROUND;
                mbackcolor = TextStyle.COLOR_INDEX_BACKGROUND;
                meffect = 0;
            } else if (code == 1) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_BOLD;
            } else if (code == 2) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_DIM;
            } else if (code == 3) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_ITALIC;
            } else if (code == 4) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_UNDERLINE;
            } else if (code == 5) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_BLINK;
            } else if (code == 7) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_INVERSE;
            } else if (code == 8) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_INVISIBLE;
            } else if (code == 9) {
                meffect |= TextStyle.CHARACTER_ATTRIBUTE_STRIKETHROUGH;
            } else if (code == 10) {
                // Exit alt charset (TERM=linux) - ignore.
            } else if (code == 11) {
                // Enter alt charset (TERM=linux) - ignore.
            } else if (code == 22) { // Normal color or intensity, neither bright, bold nor faint.
                meffect &= ~(TextStyle.CHARACTER_ATTRIBUTE_BOLD | TextStyle.CHARACTER_ATTRIBUTE_DIM);
            } else if (code == 23) { // not italic, but rarely used as such; clears standout with TERM=screen
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_ITALIC;
            } else if (code == 24) { // underline: none
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_UNDERLINE;
            } else if (code == 25) { // blink: none
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_BLINK;
            } else if (code == 27) { // image: positive
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_INVERSE;
            } else if (code == 28) {
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_INVISIBLE;
            } else if (code == 29) {
                meffect &= ~TextStyle.CHARACTER_ATTRIBUTE_STRIKETHROUGH;
            } else if (code >= 30 && code <= 37) {
                mforecolor = code - 30;
            } else if (code == 38 || code == 48) {
                // Extended set foreground(38)/background (48) color.
                // This is followed by either "2;$R;$G;$B" to set a 24-bit color or
                // "5;$INDEX" to set an indexed color.
                if (i + 2 > margindex)
                {
                    continue;
                }
                int firstArg = margs[i + 1];
                if (firstArg == 2) {
                    if (i + 4 > margindex) {
                        Log.w(EmulatorDebug.LOG_TAG, "Too few CSI" + code + ";2 RGB arguments");
                    } else {
                        int red = margs[i + 2];
                        int green = margs[i + 3];
                        int blue = margs[i + 4];
                        if (red < 0 || green < 0 || blue < 0 || red > 255 || green > 255 || blue > 255) {
                            finishSequenceAndLogError("Invalid RGB: " + red + "," + green + "," + blue);
                        } else {
                            int argbColor = 0xff000000 | (red << 16) | (green << 8) | blue;
                            if (code == 38) {
                                mforecolor = argbColor;
                            } else {
                                mbackcolor = argbColor;
                            }
                        }
                        i += 4; // "2;P_r;P_g;P_r"
                    }
                } else if (firstArg == 5) {
                    int color = margs[i + 2];
                    i += 2; // "5;P_s"
                    if (color >= 0 && color < TextStyle.NUM_INDEXED_COLORS) {
                        if (code == 38) {
                            mforecolor = color;
                        } else {
                            mbackcolor = color;
                        }
                    } else {
                        if (LOG_ESCAPE_SEQUENCES)
                        {
                            Log.w(EmulatorDebug.LOG_TAG, "Invalid color index: " + color);
                        }
                    }
                } else {
                    finishSequenceAndLogError("Invalid ISO-8613-3 SGR first argument: " + firstArg);
                }
            } else if (code == 39) { // Set default foreground color.
                mforecolor = TextStyle.COLOR_INDEX_FOREGROUND;
            } else if (code >= 40 && code <= 47) { // Set background color.
                mbackcolor = code - 40;
            } else if (code == 49) { // Set default background color.
                mbackcolor = TextStyle.COLOR_INDEX_BACKGROUND;
            } else if (code >= 90 && code <= 97) { // Bright foreground colors (aixterm codes).
                mforecolor = code - 90 + 8;
            } else if (code >= 100 && code <= 107) { // Bright background color (aixterm codes).
                mbackcolor = code - 100 + 8;
            } else {
                if (LOG_ESCAPE_SEQUENCES)
                {
                    Log.w(EmulatorDebug.LOG_TAG, String.format("SGR unknown code %d", code));
                }
            }
        }
    }
    
    private void doOsc(int b) {
        switch (b) {
            case 7: // Bell.
                doOscSetTextParameters("\007");
                break;
            case 27: // Escape.
                continueSequence(ESC_OSC_ESC);
                break;
            default:
                collectoscargs(b);
                break;
        }
    }
    
    private void doOscEsc(int b) {
        switch (b) {
            case '\\':
                doOscSetTextParameters("\033\\");
                break;
            default:
                // The ESC character was not followed by a \, so insert the ESC and
                // the current character in arg buffer.
                collectoscargs(27);
                collectoscargs(b);
                continueSequence(ESC_OSC);
                break;
        }
    }
    
    /** An Operating System Controls (OSC) Set Text Parameters. May come here from BEL or ST. */
    private void doOscSetTextParameters(String bellOrStringTerminator) {
        int value = -1;
        String textParameter = "";
        // Extract initial $value from initial "$value;..." string.
        for (int moscargtokenizerindex = 0; moscargtokenizerindex < moscordevicecontrolargs
                .length(); moscargtokenizerindex++) {
            char b = moscordevicecontrolargs.charAt(moscargtokenizerindex);
            if (b == ';') {
                textParameter = moscordevicecontrolargs.substring(moscargtokenizerindex + 1);
                break;
            } else if (b >= '0' && b <= '9') {
                value = ((value < 0) ? 0 : value * 10) + (b - '0');
            } else {
                unknownSequence(b);
                return;
            }
        }
        
        switch (value) {
            case 0: // Change icon name and window title to T.
            case 1: // Change icon name to T.
            case 2: // Change window title to T.
                setTitle(textParameter);
                break;
            case 4:
                // P s = 4 ; c ; spec → Change Color Number c to the color specified by spec. This can be a name or RGB
                // specification as per XParseColor. Any number of c name pairs may be given. The color numbers
                // correspond
                // to the ANSI colors 0-7, their bright versions 8-15, and if supported, the remainder of the 88-color
                // or
                // 256-color table.
                // If a "?" is given rather than a name or RGB specification, xterm replies with a control sequence of
                // the
                // same form which can be used to set the corresponding color. Because more than one pair of color
                // number
                // and specification can be given in one control sequence, xterm can make more than one reply.
                int colorIndex = -1;
                int parsingPairStart = -1;
                for (int i = 0;; i++) {
                    boolean endOfInput = i == textParameter.length();
                    char b = endOfInput ? ';' : textParameter.charAt(i);
                    if (b == ';') {
                        if (parsingPairStart < 0) {
                            parsingPairStart = i + 1;
                        } else {
                            if (colorIndex < 0 || colorIndex > 255) {
                                unknownSequence(b);
                                return;
                            } else {
                                mcolors.tryParseColor(colorIndex, textParameter.substring(parsingPairStart, i));
                                msession.onColorsChanged();
                                colorIndex = -1;
                                parsingPairStart = -1;
                            }
                        }
                    } else if (parsingPairStart >= 0) {
                        // We have passed a color index and are now going through color spec.
                    } else if (parsingPairStart < 0 && (b >= '0' && b <= '9')) {
                        colorIndex = ((colorIndex < 0) ? 0 : colorIndex * 10) + (b - '0');
                    } else {
                        unknownSequence(b);
                        return;
                    }
                    if (endOfInput)
                    {
                        break;
                    }
                }
                break;
            case 10: // Set foreground color.
            case 11: // Set background color.
            case 12: // Set cursor color.
                int specialIndex = TextStyle.COLOR_INDEX_FOREGROUND + (value - 10);
                int lastSemiIndex = 0;
                for (int charIndex = 0;; charIndex++) {
                    boolean endOfInput = charIndex == textParameter.length();
                    if (endOfInput || textParameter.charAt(charIndex) == ';') {
                        try {
                            String colorSpec = textParameter.substring(lastSemiIndex, charIndex);
                            if ("?".equals(colorSpec)) {
                                // Report current color in the same format xterm and gnome-terminal does.
                                int rgb = mcolors.mcurrentcolors[specialIndex];
                                int r = (65535 * ((rgb & 0x00FF0000) >> 16)) / 255;
                                int g = (65535 * ((rgb & 0x0000FF00) >> 8)) / 255;
                                int b = (65535 * ((rgb & 0x000000FF))) / 255;
                                msession.write("\033]" + value + ";rgb:" + String.format(Locale.US, "%04x", r) + "/"
                                        + String.format(Locale.US, "%04x", g) + "/"
                                        + String.format(Locale.US, "%04x", b) + bellOrStringTerminator);
                            } else {
                                mcolors.tryParseColor(specialIndex, colorSpec);
                                msession.onColorsChanged();
                            }
                            specialIndex++;
                            if (endOfInput || (specialIndex > TextStyle.COLOR_INDEX_CURSOR)
                                    || ++charIndex >= textParameter.length())
                            {
                                break;
                            }
                            lastSemiIndex = charIndex;
                        } catch (NumberFormatException e) {
                            // Ignore.
                        }
                    }
                }
                break;
            case 52: // Manipulate Selection Data. Skip the optional first selection parameter(s).
                int startIndex = textParameter.indexOf(";") + 1;
                try {
                    String clipboardText =
                            new String(Base64.decode(textParameter.substring(startIndex), 0), StandardCharsets.UTF_8);
                    msession.clipboardText(clipboardText);
                } catch (Exception e) {
                    Log.e(EmulatorDebug.LOG_TAG, "OSC Manipulate selection, invalid string '" + textParameter + "");
                }
                break;
            case 104:
                // "104;$c" → Reset Color Number $c. It is reset to the color specified by the corresponding X
                // resource. Any number of c parameters may be given. These parameters correspond to the ANSI colors
                // 0-7,
                // their bright versions 8-15, and if supported, the remainder of the 88-color or 256-color table. If no
                // parameters are given, the entire table will be reset.
                if (textParameter.isEmpty()) {
                    mcolors.reset();
                    msession.onColorsChanged();
                } else {
                    int lastIndex = 0;
                    for (int charIndex = 0;; charIndex++) {
                        boolean endOfInput = charIndex == textParameter.length();
                        if (endOfInput || textParameter.charAt(charIndex) == ';') {
                            try {
                                int colorToReset = Integer.parseInt(textParameter.substring(lastIndex, charIndex));
                                mcolors.reset(colorToReset);
                                msession.onColorsChanged();
                                if (endOfInput)
                                {
                                    break;
                                }
                                charIndex++;
                                lastIndex = charIndex;
                            } catch (NumberFormatException e) {
                                // Ignore.
                            }
                        }
                    }
                }
                break;
            case 110: // Reset foreground color.
            case 111: // Reset background color.
            case 112: // Reset cursor color.
                mcolors.reset(TextStyle.COLOR_INDEX_FOREGROUND + (value - 110));
                msession.onColorsChanged();
                break;
            case 119: // Reset highlight color.
                break;
            default:
                unknownParameter(value);
                break;
        }
        finishSequence();
    }
    
    private void blockClear(int sx, int sy, int w) {
        blockClear(sx, sy, w, 1);
    }
    
    private void blockClear(int sx, int sy, int w, int h) {
        mscreen.blockSet(sx, sy, w, h, ' ', getStyle());
    }
    
    private long getStyle() {
        return TextStyle.encode(mforecolor, mbackcolor, meffect);
    }
    
    /** "CSI P_m h" for set or "CSI P_m l" for reset ANSI mode. */
    private void doSetMode(boolean newValue) {
        int modeBit = getArg0(0);
        switch (modeBit) {
            case 4: // Set="Insert Mode". Reset="Replace Mode". (IRM).
                minsertmode = newValue;
                break;
            case 20: // Normal Linefeed (LNM).
                unknownParameter(modeBit);
                // http://www.vt100.net/docs/vt510-rm/LNM
                break;
            case 34:
                // Normal cursor visibility - when using TERM=screen, see
                // http://www.gnu.org/software/screen/manual/html_node/Control-Sequences.html
                break;
            default:
                unknownParameter(modeBit);
                break;
        }
    }
    
    /**
     * NOTE: The parameters of this function respect the {@link #DECSET_BIT_ORIGIN_MODE}. Use
     * {@link #setCursorRowCol(int, int)} for absolute pos.
     */
    private void setCursorPosition(int x, int y) {
        boolean originMode = isDecsetInternalBitSet(DECSET_BIT_ORIGIN_MODE);
        int effectiveTopMargin = originMode ? mtopmargin : 0;
        int effectiveBottomMargin = originMode ? mbottommargin : mrows;
        int effectiveLeftMargin = originMode ? mleftmargin : 0;
        int effectiveRightMargin = originMode ? mrightmargin : mcolumns;
        int newRow = Math.max(effectiveTopMargin, Math.min(effectiveTopMargin + y, effectiveBottomMargin - 1));
        int newCol = Math.max(effectiveLeftMargin, Math.min(effectiveLeftMargin + x, effectiveRightMargin - 1));
        setCursorRowCol(newRow, newCol);
    }
    
    private void scrollDownOneLine() {
        mscrollcounter++;
        if (mleftmargin != 0 || mrightmargin != mcolumns) {
            // Horizontal margin: Do not put anything into scroll history, just non-margin part of screen up.
            mscreen.blockCopy(mleftmargin, mtopmargin + 1, mrightmargin - mleftmargin, mbottommargin - mtopmargin - 1,
                mleftmargin, mtopmargin);
            // .. and blank bottom row between margins:
            mscreen.blockSet(mleftmargin, mbottommargin - 1, mrightmargin - mleftmargin, 1, ' ', meffect);
        } else {
            mscreen.scrollDownOneLine(mtopmargin, mbottommargin, getStyle());
        }
    }
    
    /** Process the next ASCII character of a parameter. */
    private void parseArg(int b) {
        if (b >= '0' && b <= '9') {
            if (margindex < margs.length) {
                int oldValue = margs[margindex];
                int thisDigit = b - '0';
                int value;
                if (oldValue >= 0) {
                    value = oldValue * 10 + thisDigit;
                } else {
                    value = thisDigit;
                }
                margs[margindex] = value;
            }
            continueSequence(mescapestate);
        } else if (b == ';') {
            if (margindex < margs.length) {
                margindex++;
            }
            continueSequence(mescapestate);
        } else {
            unknownSequence(b);
        }
    }
    
    private int getArg0(int defaultValue) {
        return getArg(0, defaultValue, true);
    }
    
    private int getArg1(int defaultValue) {
        return getArg(1, defaultValue, true);
    }
    
    private int getArg(int index, int defaultValue, boolean treatZeroAsDefault) {
        int result = margs[index];
        if (result < 0 || (result == 0 && treatZeroAsDefault)) {
            result = defaultValue;
        }
        return result;
    }
    
    private void collectoscargs(int b) {
        if (moscordevicecontrolargs.length() < MAX_OSC_STRING_LENGTH) {
            moscordevicecontrolargs.appendCodePoint(b);
            continueSequence(mescapestate);
        } else {
            unknownSequence(b);
        }
    }
    
    private void unimplementedSequence(int b) {
        logError("Unimplemented sequence char '" + (char) b + "' (U+" + String.format("%04x", b) + ")");
        finishSequence();
    }
    
    private void unknownSequence(int b) {
        logError("Unknown sequence char '" + (char) b + "' (numeric value=" + b + ")");
        finishSequence();
    }
    
    private void unknownParameter(int parameter) {
        logError("Unknown parameter: " + parameter);
        finishSequence();
    }
    
    private void logError(String errorType) {
        if (LOG_ESCAPE_SEQUENCES) {
            StringBuilder buf = new StringBuilder();
            buf.append(errorType);
            buf.append(", escapeState=");
            buf.append(mescapestate);
            boolean firstArg = true;
            if (margindex >= margs.length)
            {
                margindex = margs.length - 1;
            }
            for (int i = 0; i <= margindex; i++) {
                int value = margs[i];
                if (value >= 0) {
                    if (firstArg) {
                        firstArg = false;
                        buf.append(", args={");
                    } else {
                        buf.append(',');
                    }
                    buf.append(value);
                }
            }
            if (!firstArg)
            {
                buf.append('}');
            }
            finishSequenceAndLogError(buf.toString());
        }
    }
    
    private void finishSequenceAndLogError(String error) {
        if (LOG_ESCAPE_SEQUENCES)
        {
            Log.w(EmulatorDebug.LOG_TAG, error);
        }
        finishSequence();
    }
    
    private void finishSequence() {
        mescapestate = ESC_NONE;
    }
    
    /**
     * Send a Unicode code point to the screen.
     *
     * @param codePoint The code point of the character to display
     */
    private void emitCodePoint(int codePoint) {
        mlastemittedcodepoint = codePoint;
        if (muselinedrawingusesg0 ? muselinedrawingg0 : muselinedrawingg1) {
            // http://www.vt100.net/docs/vt102-ug/table5-15.html.
            switch (codePoint) {
                case '_':
                    codePoint = ' '; // Blank.
                    break;
                case '`':
                    codePoint = '◆'; // Diamond.
                    break;
                case '0':
                    codePoint = '█'; // Solid block;
                    break;
                case 'a':
                    codePoint = '▒'; // Checker board.
                    break;
                case 'b':
                    codePoint = '␉'; // Horizontal tab.
                    break;
                case 'c':
                    codePoint = '␌'; // Form feed.
                    break;
                case 'd':
                    codePoint = '\r'; // Carriage return.
                    break;
                case 'e':
                    codePoint = '␊'; // Linefeed.
                    break;
                case 'f':
                    codePoint = '°'; // Degree.
                    break;
                case 'g':
                    codePoint = '±'; // Plus-minus.
                    break;
                case 'h':
                    codePoint = '\n'; // Newline.
                    break;
                case 'i':
                    codePoint = '␋'; // Vertical tab.
                    break;
                case 'j':
                    codePoint = '┘'; // Lower right corner.
                    break;
                case 'k':
                    codePoint = '┐'; // Upper right corner.
                    break;
                case 'l':
                    codePoint = '┌'; // Upper left corner.
                    break;
                case 'm':
                    codePoint = '└'; // Left left corner.
                    break;
                case 'n':
                    codePoint = '┼'; // Crossing lines.
                    break;
                case 'o':
                    codePoint = '⎺'; // Horizontal line - scan 1.
                    break;
                case 'p':
                    codePoint = '⎻'; // Horizontal line - scan 3.
                    break;
                case 'q':
                    codePoint = '─'; // Horizontal line - scan 5.
                    break;
                case 'r':
                    codePoint = '⎼'; // Horizontal line - scan 7.
                    break;
                case 's':
                    codePoint = '⎽'; // Horizontal line - scan 9.
                    break;
                case 't':
                    codePoint = '├'; // T facing rightwards.
                    break;
                case 'u':
                    codePoint = '┤'; // T facing leftwards.
                    break;
                case 'v':
                    codePoint = '┴'; // T facing upwards.
                    break;
                case 'w':
                    codePoint = '┬'; // T facing downwards.
                    break;
                case 'x':
                    codePoint = '│'; // Vertical line.
                    break;
                case 'y':
                    codePoint = '≤'; // Less than or equal to.
                    break;
                case 'z':
                    codePoint = '≥'; // Greater than or equal to.
                    break;
                case '{':
                    codePoint = 'π'; // Pi.
                    break;
                case '|':
                    codePoint = '≠'; // Not equal to.
                    break;
                case '}':
                    codePoint = '£'; // UK pound.
                    break;
                case '~':
                    codePoint = '·'; // Centered dot.
                    break;
                default:
                    break;
            }
        }
        
        final boolean autoWrap = isDecsetInternalBitSet(DECSET_BIT_AUTOWRAP);
        final int displayWidth = WcWidth.width(codePoint);
        final boolean cursorInLastColumn = mcursorcol == mrightmargin - 1;
        
        if (autoWrap) {
            if (cursorInLastColumn && ((mabouttoautowrap && displayWidth == 1) || displayWidth == 2)) {
                mscreen.setLineWrap(mcursorrow);
                mcursorcol = mleftmargin;
                if (mcursorrow + 1 < mbottommargin) {
                    mcursorrow++;
                } else {
                    scrollDownOneLine();
                }
            }
        } else if (cursorInLastColumn && displayWidth == 2) {
            // The behaviour when a wide character is output with cursor in the last column when
            // autowrap is disabled is not obvious - it's ignored here.
            return;
        }
        
        if (minsertmode && displayWidth > 0) {
            // Move character to right one space.
            int destCol = mcursorcol + displayWidth;
            if (destCol < mrightmargin)
            {
                mscreen.blockCopy(mcursorcol, mcursorrow, mrightmargin - destCol, 1, destCol, mcursorrow);
            }
        }
        
        int offsetDueToCombiningChar = ((displayWidth <= 0 && mcursorcol > 0 && !mabouttoautowrap) ? 1 : 0);
        mscreen.setChar(mcursorcol - offsetDueToCombiningChar, mcursorrow, codePoint, getStyle());
        
        if (autoWrap && displayWidth > 0)
        {
            mabouttoautowrap = (mcursorcol == mrightmargin - displayWidth);
        }
        
        mcursorcol = Math.min(mcursorcol + displayWidth, mrightmargin - 1);
    }
    
    private void setCursorRow(int row) {
        mcursorrow = row;
        mabouttoautowrap = false;
    }
    
    private void setCursorCol(int col) {
        mcursorcol = col;
        mabouttoautowrap = false;
    }
    
    /** Set the cursor mode, but limit it to margins if {@link #DECSET_BIT_ORIGIN_MODE} is enabled. */
    private void setCursorColRespectingOriginMode(int col) {
        setCursorPosition(col, mcursorrow);
    }
    
    /** TODO: Better name, distinguished from {@link #setCursorPosition(int, int)} by not regarding origin mode. */
    private void setCursorRowCol(int row, int col) {
        mcursorrow = Math.max(0, Math.min(row, mrows - 1));
        mcursorcol = Math.max(0, Math.min(col, mcolumns - 1));
        mabouttoautowrap = false;
    }
    
    public int getScrollCounter() {
        return mscrollcounter;
    }
    
    public void clearScrollCounter() {
        mscrollcounter = 0;
    }
    
    /** Reset terminal state so user can interact with it regardless of present state. */
    /** 重置终端状态,使得用户可以和终端交互,regardless of present state*/
    public void reset() {
        mcursorstyle = CURSOR_STYLE_BLOCK;
        margindex = 0;
        mcontinuesequence = false;
        mescapestate = ESC_NONE;
        minsertmode = false;
        mtopmargin = mleftmargin = 0;
        mbottommargin = mrows;
        mrightmargin = mcolumns;
        mabouttoautowrap = false;
        mforecolor =
                msavedstatemain.msavedforecolor = msavedstatealt.msavedforecolor = TextStyle.COLOR_INDEX_FOREGROUND;
        mbackcolor =
                msavedstatemain.msavedbackcolor = msavedstatealt.msavedbackcolor = TextStyle.COLOR_INDEX_BACKGROUND;
        setDefaultTabStops();
        
        muselinedrawingg0 = muselinedrawingg1 = false;
        muselinedrawingusesg0 = true;
        
        msavedstatemain.msavedcursorrow =
                msavedstatemain.msavedcursorcol = msavedstatemain.msavedeffect = msavedstatemain.msaveddecflags = 0;
        msavedstatealt.msavedcursorrow =
                msavedstatealt.msavedcursorcol = msavedstatealt.msavedeffect = msavedstatealt.msaveddecflags = 0;
        mcurrentdecsetflags = 0;
        // Initial wrap-around is not accurate but makes terminal more useful, especially on a small screen:
        setDecsetinternalBit(DECSET_BIT_AUTOWRAP, true);
        setDecsetinternalBit(DECSET_BIT_SHOWING_CURSOR, true);
        msaveddecsetflags = msavedstatemain.msaveddecflags = msavedstatealt.msaveddecflags = mcurrentdecsetflags;
        
        // XXX: Should we set terminal driver back to IUTF8 with termios?
        mutf8Index = mutf8Tofollow = 0;
        
        mcolors.reset();
        msession.onColorsChanged();
    }
    
    public String getSelectedText(int x1, int y1, int x2, int y2) {
        return mscreen.getSelectedText(x1, y1, x2, y2);
    }
    
    /** Get the terminal session's title (null if not set). */
    public String getTitle() {
        return mtitle;
    }
    
    /** Change the terminal session's title. */
    private void setTitle(String newTitle) {
        String oldTitle = mtitle;
        mtitle = newTitle;
        if (!Objects.equals(oldTitle, newTitle)) {
            msession.titleChanged(oldTitle, newTitle);
        }
    }
    
    /** If DECSET 2004 is set, prefix paste with "\033[200~" and suffix with "\033[201~". */
    public void paste(String text) {
        // First: Always remove escape key and C1 control characters [0x80,0x9F]:
        text = text.replaceAll("(\u001B|[\u0080-\u009F])", "");
        // Second: Replace all newlines (\n) or CRLF (\r\n) with carriage returns (\r).
        text = text.replaceAll("\r?\n", "\r");
        
        // Then: Implement bracketed paste mode if enabled:
        boolean bracketed = isDecsetInternalBitSet(DECSET_BIT_BRACKETED_PASTE_MODE);
        if (bracketed)
        {
            msession.write("\033[200~");
        }
        msession.write(text);
        if (bracketed)
        {
            msession.write("\033[201~");
        }
    }
    
    /** http://www.vt100.net/docs/vt510-rm/DECSC */
    static final class SavedScreenState {
        /** Saved state of the cursor position, Used to implement the save/restore cursor position escape sequences. */
        int msavedcursorrow;
        int msavedcursorcol;
        int msavedeffect;
        int msavedforecolor;
        int msavedbackcolor;
        int msaveddecflags;
        boolean muselinedrawingg0;
        boolean muselinedrawingg1;
        boolean muselinedrawingusesg0 = true;
    }
    
    @Override
    public String toString() {
        return "TerminalEmulator[size=" + mscreen.mcolumns + "x" + mscreen.screenRows + ", margins={" + mtopmargin
                + "," + mrightmargin + "," + mbottommargin + "," + mleftmargin + "}]";
    }
    
}
