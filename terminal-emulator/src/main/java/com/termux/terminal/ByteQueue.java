package com.termux.terminal;

/** A circular byte buffer allowing one producer and one consumer thread. */

/**
 * @cn-annotator butub
 * 通过这个类实现 byte -> terminal 的具体实现
 */
final class ByteQueue {
    
    private final byte[] mbuffer;
    private int mhead;
    private int mstoredBytes;
    private boolean mopen = true;
    
    public ByteQueue(int size) {
        mbuffer = new byte[size];
    }
    
    public synchronized void close() {
        mopen = false;
        notify();
    }
    
    public synchronized int read(byte[] buffer, boolean block) {
        while (mstoredBytes == 0 && mopen) {
            if (block) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // Ignore.
                }
            } else {
                return 0;
            }
        }
        if (!mopen)
        {
            return -1;
        }
        
        int totalRead = 0;
        int bufferLength = mbuffer.length;
        boolean wasFull = bufferLength == mstoredBytes;
        int length = buffer.length;
        int offset = 0;
        while (length > 0 && mstoredBytes > 0) {
            int oneRun = Math.min(bufferLength - mhead, mstoredBytes);
            int bytesToCopy = Math.min(length, oneRun);
            System.arraycopy(mbuffer, mhead, buffer, offset, bytesToCopy);
            mhead += bytesToCopy;
            if (mhead >= bufferLength)
            {
                mhead = 0;
            }
            mstoredBytes -= bytesToCopy;
            length -= bytesToCopy;
            offset += bytesToCopy;
            totalRead += bytesToCopy;
        }
        if (wasFull)
        {
            notify();
        }
        return totalRead;
    }
    
    /**
     * Attempt to write the specified portion of the provided buffer to the queue.
     * <p/>
     * Returns whether the output was totally written, false if it was closed before.
     */
    public boolean write(byte[] buffer, int offset, int lengthToWrite) {
        if (lengthToWrite + offset > buffer.length) {
            throw new IllegalArgumentException("length + offset > buffer.length");
        } else if (lengthToWrite <= 0) {
            throw new IllegalArgumentException("length <= 0");
        }
        
        final int bufferLength = mbuffer.length;
        
        synchronized (this) {
            while (lengthToWrite > 0) {
                while (bufferLength == mstoredBytes && mopen) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
                if (!mopen)
                {
                    return false;
                }
                final boolean wasEmpty = mstoredBytes == 0;
                int bytesToWriteBeforeWaiting = Math.min(lengthToWrite, bufferLength - mstoredBytes);
                lengthToWrite -= bytesToWriteBeforeWaiting;
                
                while (bytesToWriteBeforeWaiting > 0) {
                    int tail = mhead + mstoredBytes;
                    int oneRun;
                    if (tail >= bufferLength) {
                        // Buffer: [.............]
                        // ________________H_______T
                        // =>
                        // Buffer: [.............]
                        // ___________T____H
                        // onRun= _____----_
                        tail = tail - bufferLength;
                        oneRun = mhead - tail;
                    } else {
                        oneRun = bufferLength - tail;
                    }
                    int bytesToCopy = Math.min(oneRun, bytesToWriteBeforeWaiting);
                    System.arraycopy(buffer, offset, mbuffer, tail, bytesToCopy);
                    offset += bytesToCopy;
                    bytesToWriteBeforeWaiting -= bytesToCopy;
                    mstoredBytes += bytesToCopy;
                }
                if (wasEmpty)
                {
                    notify();
                }
            }
        }
        return true;
    }
}
