package com.termux.terminal;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;//Universally Unique Identifier 通用唯一标识码

/**
 * A terminal session, consisting of a process coupled to a terminal interface.
 * <p>
 * The subprocess will be executed by the constructor, and when the size is made known by a call to
 * {@link #updateSize(int, int)} terminal emulation will begin and threads will be spawned to handle the subprocess I/O.
 * All terminal emulation and callback methods will be performed on the main thread.
 * <p>
 * The child process may be exited forcefully by using the {@link #finishIfRunning()} method.
 * <p>
 * NOTE: The terminal session may outlive the EmulatorView, so be careful with callbacks!
 */

/**
 * @cn-annotator butub
 *  Session, 存在一个实现terminal interface 的进程 process
 *  构造器会运行一个子进程， 调用upateSize 获得Size后，启动模拟器， 并且创建新的线程去处理subprocess I/O
 *  在主线程中有所有的terminal 模拟和回调方法
 *  使用 finishIfRunning方法 可以强制退出子进程
 *  NOTE: 会话可能会在EmulatorView关闭后仍然存活，所以需要对于回调方法小心处理。
 */
public final class TerminalSession extends TerminalOutput {
    
    /** 改变Session 后需要回调的方法*/
    /** Callback to be invoked when a {@link TerminalSession} changes. */
    public interface SessionChangedCallback {
        void onTextChanged(TerminalSession changedSession);
        
        void onTitleChanged(TerminalSession changedSession);
        
        void onSessionFinished(TerminalSession finishedSession);
        
        void onClipboardText(TerminalSession session, String text);
        
        void onBell(TerminalSession session);
        
        void onColorsChanged(TerminalSession session);
        
    }
    
    private static FileDescriptor wrapFileDescriptor(int fileDescriptor) {
        FileDescriptor result = new FileDescriptor();
        try {
            Field descriptorField;
            try {
                descriptorField = FileDescriptor.class.getDeclaredField("descriptor");
            } catch (NoSuchFieldException e) {
                // For desktop java:
                descriptorField = FileDescriptor.class.getDeclaredField("fd");
            }
            descriptorField.setAccessible(true);
            descriptorField.set(result, fileDescriptor);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            Log.wtf(EmulatorDebug.LOG_TAG, "Error accessing FileDescriptor#descriptor private field", e);
            System.exit(1);
        }
        return result;
    }
    
    private static final int MSG_NEW_INPUT = 1;
    private static final int MSG_PROCESS_EXITED = 4;
    
    public final String mhandle = UUID.randomUUID().toString();
    
    TerminalEmulator memulator;
    
    /**
     * A queue written to from a separate thread when the process outputs, and read by main thread to process by
     * terminal emulator.
     * 一个用于与进程交互的io缓冲区, 通过线程监听进程的输出,写入到这个缓冲区,然后在main线程中处理
     */
    final ByteQueue mprocesstoterminalioqueue = new ByteQueue(4096);
    /**
     * A queue written to from the main thread due to user interaction, and read by another thread which forwards by
     * writing to the {@link #mterminalfiledescriptor}.
     * 一个用于与进程交互的io缓冲区, 通过线程监听用户输入,写入到进程的对应文件中
     */
    final ByteQueue mterminaltoprocessioqueue = new ByteQueue(4096);
    /** Buffer to write translate code points into utf8 before writing to mterminaltoprocessioqueue */
    private final byte[] mutf8Inputbuffer = new byte[5];
    
    /** Callback which gets notified when a session finishes or changes title. */
    final SessionChangedCallback mchangecallback;
    
    /** The pid of the shell process. 0 if not started and -1 if finished running. */
    int mshellpid;// shell 对应的进程的id, 0->未启动, -1->结束运行
    
    /** The exit status of the shell process. Only valid if ${@link #mshellpid} is -1. */
    int mshellexitstatus;
    
    /**
     * The file descriptor referencing the master half of a pseudo-terminal pair, resulting from calling
     * {@link Jni#createSubprocess(String, String, String[], String[], int[], int, int)}.
     */
    private int mterminalfiledescriptor;
    
    /** Set by the application for user identification of session, not by terminal. */
    public String msessionname;
    
    // pass:Handler 是异步通信的类，主要接受子线程发送的数据, 并用此数据配合主线程更新UI.
    @SuppressLint("HandlerLeak")
    final Handler mmainthreadhandler = new Handler() {
        // main Thread 消息处理器,todo, 显示到页面
        final byte[] mreceivebuffer = new byte[4 * 1024];
        
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_NEW_INPUT && isRunning()) {
                // 新的输入
                int bytesRead = mprocesstoterminalioqueue.read(mreceivebuffer, false);
                if (bytesRead > 0) {
                    memulator.append(mreceivebuffer, bytesRead);
                    notifyScreenUpdate();
                }
            } else if (msg.what == MSG_PROCESS_EXITED) {
                // 进程退出了
                int exitCode = (Integer) msg.obj;
                cleanupResources(exitCode);
                mchangecallback.onSessionFinished(TerminalSession.this);
                
                String exitDescription = "\r\n[Process completed";
                if (exitCode > 0) {
                    // Non-zero process exit.
                    exitDescription += " (code " + exitCode + ")";
                } else if (exitCode < 0) {
                    // Negated signal.
                    exitDescription += " (signal " + (-exitCode) + ")";
                }
                exitDescription += " - press Enter]";
                
                byte[] bytesToWrite = exitDescription.getBytes(StandardCharsets.UTF_8);
                memulator.append(bytesToWrite, bytesToWrite.length);
                notifyScreenUpdate();
            }
        }
    };
    
    private final String mshellpath;
    private final String mcwd;
    private final String[] margs;
    private final String[] menv;
    
    /** 一个Session
     *
     * @param shellPath, 运行路径, exetuablePath
     * @param cwd, change work dir
     * @param args, arguments,
     * @param env, 环境变量, env
     * @param changeCallback, 回调，TermuxService
     */
    public TerminalSession(String shellPath, String cwd, String[] args, String[] env,
            SessionChangedCallback changeCallback) {
        mchangecallback = changeCallback;
        
        this.mshellpath = shellPath;
        this.mcwd = cwd;
        this.margs = args;
        this.menv = env;
    }
    // Constructor
    
    /** Inform the attached pty of the new size and reflow or initialize the emulator.
     *
     * */
    public void updateSize(int columns, int rows) {
        // 在何处调用？
        if (memulator == null) {
            // 没有对应的模拟器,就创建一个
            initializeEmulator(columns, rows);
        } else {
            Jni.setPtyWindowSize(mterminalfiledescriptor, rows, columns);
            memulator.resize(columns, rows);
        }
    }
    
    /** The terminal title as set through escape sequences or null if none set. */
    public String getTitle() {
        return (memulator == null) ? null : memulator.getTitle();
    }
    
    /**
     * Set the terminal emulator's window size and start terminal emulation.
     * 设置terminal emulator size  启动模拟器
     * @param columns The number of columns in the terminal window.
     * @param rows    The number of rows in the terminal window.
     */
    public void initializeEmulator(int columns, int rows) {
        memulator = new TerminalEmulator(this, columns, rows, /* transcript= */2000);
        
        int[] processId = new int[1]; // 开启子进程
        // 注意JNI, 看起来蛮有趣的， 这个
        mterminalfiledescriptor = Jni.createSubprocess(mshellpath, mcwd, margs, menv, processId, rows, columns);
        mshellpid = processId[0];
        
        final FileDescriptor terminalFileDescriptorWrapped = wrapFileDescriptor(mterminalfiledescriptor);
        
        new Thread("TermSessionInputReader[pid=" + mshellpid + "]") {
            // 读线程
            @Override
            public void run() {
                // 注意： termainlFileDescriptorWrapped
                try (InputStream termIn = new FileInputStream(terminalFileDescriptorWrapped)) {
                    final byte[] buffer = new byte[4096];
                    while (true) {
                        int read = termIn.read(buffer);
                        if (read == -1)
                        {
                            return; // 不停地扫TermIn，然后写入Process 2 Termainl Byte Queue
                        }
                        if (!mprocesstoterminalioqueue.write(buffer, 0, read))
                        {
                            return;
                        }
                        mmainthreadhandler.sendEmptyMessage(MSG_NEW_INPUT);
                    }
                } catch (Exception e) {
                    // Ignore, just shutting down.
                }
            }
        }.start();
        
        new Thread("TermSessionOutputWriter[pid=" + mshellpid + "]") {
            // 写线程
            @Override
            public void run() {
                final byte[] buffer = new byte[4096];// Only a file Stream
                try (FileOutputStream termOut = new FileOutputStream(terminalFileDescriptorWrapped)) {
                    while (true) {
                        // 不停地去扫描 Terminal 2 process 的缓冲 Byte Queue
                        int bytesToWrite = mterminaltoprocessioqueue.read(buffer, true);
                        if (bytesToWrite == -1)
                        {
                            return;
                        }
                        termOut.write(buffer, 0, bytesToWrite);
                    }
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }.start();
        
        new Thread("TermSessionWaiter[pid=" + mshellpid + "]") {
            @Override
            public void run() {
                int processExitCode = Jni.waitFor(mshellpid);// Jni
                mmainthreadhandler.sendMessage(mmainthreadhandler.obtainMessage(MSG_PROCESS_EXITED, processExitCode));
            }
        }.start();
        
    }
    // end initEmulator
    
    /** Write data to the shell process. */
    /**
     *
     * @butub
     * 注意, 这里是输入termux的最终操作,往这个buff中写入字节,就可以显示需要的字符,或者删除,或者别的功能
     */
    @Override
    public void write(byte[] data, int offset, int count) {
        if (mshellpid > 0)
        {
            mterminaltoprocessioqueue.write(data, offset, count);
        }
    }
    
    /** Write the Unicode code point to the terminal encoded in UTF-8. */
    public void writeCodePoint(boolean prependEscape, int codePoint) {
        if (codePoint > 1114111 || (codePoint >= 0xD800 && codePoint <= 0xDFFF)) {
            // 1114111 (= 2**16 + 1024**2 - 1) is the highest code point, [0xD800,0xDFFF] is the surrogate range.
            throw new IllegalArgumentException("Invalid code point: " + codePoint);
        }
        
        int bufferPosition = 0;
        if (prependEscape)
        {
            mutf8Inputbuffer[bufferPosition++] = 27;
        }
        
        if (codePoint <= /* 7 bits */0b1111111) {
            mutf8Inputbuffer[bufferPosition++] = (byte) codePoint;
        } else if (codePoint <= /* 11 bits */0b11111111111) {
            /* 110xxxxx leading byte with leading 5 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b11000000 | (codePoint >> 6));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | (codePoint & 0b111111));
        } else if (codePoint <= /* 16 bits */0b1111111111111111) {
            /* 1110xxxx leading byte with leading 4 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b11100000 | (codePoint >> 12));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | ((codePoint >> 6) & 0b111111));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | (codePoint & 0b111111));
        } else { /* We have checked codePoint <= 1114111 above, so we have max 21 bits = 0b111111111111111111111 */
            /* 11110xxx leading byte with leading 3 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b11110000 | (codePoint >> 18));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | ((codePoint >> 12) & 0b111111));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | ((codePoint >> 6) & 0b111111));
            /* 10xxxxxx continuation byte with following 6 bits */
            mutf8Inputbuffer[bufferPosition++] = (byte) (0b10000000 | (codePoint & 0b111111));
        }
        write(mutf8Inputbuffer, 0, bufferPosition);
    }
    
    public TerminalEmulator getEmulator() {
        return memulator;
    }
    
    /** Notify the {@link #mchangecallback} that the screen has changed. */
    protected void notifyScreenUpdate() {
        mchangecallback.onTextChanged(this);
    }
    
    /** Reset state for terminal emulator state. */
    public void reset() {
        memulator.reset();
        notifyScreenUpdate();
    }
    
    /** Finish this terminal session by sending SIGKILL to the shell. */
    public void finishIfRunning() {
        if (isRunning()) {
            try {
                Os.kill(mshellpid, OsConstants.SIGKILL);
            } catch (ErrnoException e) {
                Log.w("termux", "Failed sending SIGKILL: " + e.getMessage());
            }
        }
    }
    
    /** Cleanup resources when the process exits. */
    void cleanupResources(int exitStatus) {
        synchronized (this) {
            mshellpid = -1;
            mshellexitstatus = exitStatus;
        }
        
        // Stop the reader and writer threads, and close the I/O streams
        mterminaltoprocessioqueue.close();
        mprocesstoterminalioqueue.close();
        Jni.close(mterminalfiledescriptor);
    }
    
    @Override
    public void titleChanged(String oldTitle, String newTitle) {
        mchangecallback.onTitleChanged(this);
    }
    
    public synchronized boolean isRunning() {
        return mshellpid != -1;
    }
    
    /** Only valid if not {@link #isRunning()}. */
    public synchronized int getExitStatus() {
        return mshellexitstatus;
    }
    
    @Override
    public void clipboardText(String text) {
        mchangecallback.onClipboardText(this, text);
    }
    
    @Override
    public void onBell() {
        mchangecallback.onBell(this);
    }
    
    @Override
    public void onColorsChanged() {
        mchangecallback.onColorsChanged(this);
    }
    
    public int getPid() {
        return mshellpid;
    }
    
}
